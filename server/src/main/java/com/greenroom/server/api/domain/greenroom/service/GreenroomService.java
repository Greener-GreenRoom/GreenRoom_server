package com.greenroom.server.api.domain.greenroom.service;

import com.greenroom.server.api.domain.greenroom.dto.*;
import com.greenroom.server.api.domain.greenroom.entity.*;
import com.greenroom.server.api.domain.greenroom.enums.GreenRoomStatus;
import com.greenroom.server.api.domain.greenroom.enums.ItemType;
import com.greenroom.server.api.domain.greenroom.enums.LevelIncreasingCause;
import com.greenroom.server.api.domain.greenroom.repository.*;
import com.greenroom.server.api.domain.user.dto.UserBaseInfoDto;
import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.repository.UserRepository;
import com.greenroom.server.api.utils.ImageUploader.GreenroomImageUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class GreenroomService {
    private final GreenRoomRepository greenRoomRepository;
    private final AdornmentRepository adornmentRepository;
    private final PlantRepository plantRepository;
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final StoryLikeRepository storyLikeRepository;
    private final DiaryRepository diaryRepository;
    private final StoryRepository storyRepository;
    private final GuestbookRepository guestbookRepository;
    private final TodoLogRepository todoLogRepository;
    private final GreenroomImageUploader greenroomImageUploader;

    private final ApplicationEventPublisher applicationEventPublisher;

    ////greenroom 등록 + todo에 물주기 등록(레벨업) + 식물 키우는 횟수 count 증가 + greenroom 꾸미기 item 등록

    public GreenroomRegisterResponseDto registerGreenRoom(GreenroomRegistrationDto greenroomRegistrationDto, String userEmail, MultipartFile imgFile) throws IllegalArgumentException, IOException,UsernameNotFoundException {

        String shape = greenroomRegistrationDto.getShape();
        String name = greenroomRegistrationDto.getName();
        Long plantId = greenroomRegistrationDto.getPlantId();
        Integer wateringDuration = greenroomRegistrationDto.getWateringDuration();
        String lastWatering = greenroomRegistrationDto.getLastWatering();
        LocalDateTime firstStartDate = LocalDate.parse(lastWatering).atStartOfDay();
        String imageUrl;

        //plant id가 null일 경우, 더미 plant 데이터로 생성
        Plant plant = plantId==null? plantRepository.findByPlantCategory("dummy").orElseThrow(()->new IllegalArgumentException("dummy 식물을 찾을 수 없음.")): plantRepository.findById(plantId).orElseThrow(() -> new IllegalArgumentException("해당 식물을 찾을 수 없습니다."));

        // email 로 user 찾기
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException("해당 user를 찾을 수 없습니다."));

        //imageFile null 값 허용
        if (imgFile==null) {
            imageUrl = null;
        } else {
            try {   // s3에 이미지 업로드
                imageUrl = greenroomImageUploader.uploadGreenroomImage(imgFile);
            } catch (IOException e) {
                throw new IOException("S3 업로드 실패");
            }
        }

        //그린룸 추가
        GreenRoom greenRoom = GreenRoom.builder()
                .plant(plant)
                .user(user)
                .name(name)
                .pictureUrl(imageUrl)
                .build();
        GreenRoom savedGreenRoom = greenRoomRepository.save(greenRoom);

        //식물 키우는 횟수 증가
        plant.updatePlantCount();

        //물주기 주기 등록 event 발생
        applicationEventPublisher.publishEvent(new TodoCreationDto(savedGreenRoom.getGreenroomId(),firstStartDate,wateringDuration,1L));

        //꾸미기 아이템 등록 event 발생
        applicationEventPublisher.publishEvent(new OneAdornmentCreationDto(shape,savedGreenRoom));

        HashMap<String,Integer> pointUp= new HashMap<>();

        //처음 식물 등록했을 경우 점수 +1점 ( 식물 모두 삭제했다 등록하는 경우는 점수 x)
        if(greenRoomRepository.findGreenRoomByUser(user).size()==1){
            greenRoom.getUser().updateWeeklySeed(1);
            greenRoom.getUser().updateTotalSeed(1);

            Grade beforeGrade = user.getGrade();
            // level 조정.
            applicationEventPublisher.publishEvent(greenRoom.getUser());
            Grade afterGrade = user.getGrade();

            pointUp.put(LevelIncreasingCause.FIRST_GREENROOM_REGISTRATION.toString().toLowerCase(),1);
            return new GreenroomRegisterResponseDto(savedGreenRoom.getGreenroomId(),new GradeUpDto(user.getGrade().getLevel(),pointUp,!beforeGrade.equals(afterGrade)));

        }

        return new GreenroomRegisterResponseDto(savedGreenRoom.getGreenroomId(),new GradeUpDto(user.getGrade().getLevel(),pointUp,false));

    }

        public GreenroomAllResponseDto getAllGreenroomInfo(String userEmail, String sort, String filter,Integer offset) throws UsernameNotFoundException{
            User user = userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음"));

            ArrayList<GreenRoomStatus> greenroomStatus = new ArrayList<>();
            //전체 범위 가져오기
            if (filter==null){greenroomStatus.add(GreenRoomStatus.ENABLED);greenroomStatus.add(GreenRoomStatus.DISABLED);}
            //죽은 식물만 가져오기
            else if(Objects.equals(filter, "disabled")){greenroomStatus.add(GreenRoomStatus.DISABLED);}
            //키우고 있는 식물만 가져오기
            else if (Objects.equals(filter, "enabled")) {greenroomStatus.add(GreenRoomStatus.ENABLED);}

            List<GreenRoom> greenRooms = new ArrayList<>();

            //오래된 순으로 가져오기
            if(Objects.equals(sort, "asc")) {greenRooms = greenRoomRepository.findGreenRoomByUserAndStatusIn(user,greenroomStatus,Sort.by(Sort.Order.asc("createDate")));
            }
            //최신순으로 가져오기
            else if(Objects.equals(sort, "desc")){ greenRooms = greenRoomRepository.findGreenRoomByUserAndStatusIn(user,greenroomStatus,Sort.by(Sort.Order.desc("createDate")));
            }
            //기본 순으로 가져오기
            else if(sort==null) {greenRooms = greenRoomRepository.findGreenRoomByUserAndStatusIn(user,greenroomStatus,Sort.unsorted());}

            if(greenRooms.isEmpty()){return null;}

            if(offset!=null){
                offset = offset>greenRooms.size()?greenRooms.size():offset;
                greenRooms= greenRooms.subList(0,offset);
            }


            //그린룸 별 갖고 있는 아이템 가져오기
            Map<GreenRoom, List<Adornment>> adornments = adornmentRepository.findAdornmentByGreenRoom_User(user).stream().collect(Collectors.groupingBy(Adornment::getGreenRoom));;

            ///그린룸 별 활성화된 주기 가져오기
            Map<GreenRoom, List<Todo>> todos = todoRepository.findTodoByGreenRoom_UserAndUseYn(user,true).stream().collect(Collectors.groupingBy(Todo::getGreenRoom));;

            ArrayList<GreenroomResponseDto> greenroomResponseDtoList = new ArrayList<>();


            for(GreenRoom greenRoom: greenRooms){

                ArrayList<GreenroomItemDto> greenroomItemDtos = new ArrayList<>();
                ArrayList<GreenroomTodoDto> greenroomTodoDtos = new ArrayList<>();

                if (adornments.get(greenRoom)!=null){
                    for(Adornment adornment : adornments.get(greenRoom)){
                        greenroomItemDtos.add(new GreenroomItemDto(adornment.getItem().getItemType().name().toLowerCase(),adornment.getItem().getItemName()));
                    }
                }

                if(todos.get(greenRoom)!=null) {
                    for (Todo todo : todos.get(greenRoom)) {
                        if(todo.getNextTodoDate()!=null) {
                            greenroomTodoDtos.add(new GreenroomTodoDto(todo.getActivity().getName().name().toLowerCase(),todo.getNextTodoDate().toLocalDate()));
                        }
                    }
                }

                greenroomResponseDtoList.add(new GreenroomResponseDto(GreenroomInfoDto.from(greenRoom),greenroomItemDtos,greenroomTodoDtos));
            }
            return new GreenroomAllResponseDto(UserBaseInfoDto.from(user),greenroomResponseDtoList);

        }


        public ArrayList<GreenRoomListResponseDto> getGreenroomList(String userEmail, String sort,Integer offset) throws UsernameNotFoundException{
            ArrayList<GreenRoomListResponseDto> result = new ArrayList<>();
            User user = userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음"));
            List<GreenRoom> greenRooms = new ArrayList<>() ;


            ///user가 현재 키우고 있는 식물만 오래된 순으로 가져오기
            if(Objects.equals(sort, "asc")){ greenRooms = greenRoomRepository.findGreenRoomByUserAndStatus(user,GreenRoomStatus.ENABLED,Sort.by(Sort.Order.asc("createDate"))); } // 오래된순
            /////user가 현재 키우고 있는 식물만 최신 순으로 가져오기
            else if(Objects.equals(sort, "desc")) {greenRooms = greenRoomRepository.findGreenRoomByUserAndStatus(user,GreenRoomStatus.ENABLED,Sort.by(Sort.Order.desc("createDate"))); } //최신순
            //offset 사용 안할 경우 id값 기준
            else if(sort==null){
                greenRooms = greenRoomRepository.findGreenRoomByUserAndStatus(user,GreenRoomStatus.ENABLED,Sort.unsorted());
            }

            //없으면 null 즉시 반환
            if(greenRooms.isEmpty()){return null;}

            if(offset!=null){
                offset = offset>greenRooms.size()?greenRooms.size():offset;  //out of index 방지
                greenRooms=greenRooms.subList(0,offset);
            }


            Map<GreenRoom, List<Adornment>> adornments = adornmentRepository.findAdornmentByGreenRoom_UserAndItem_ItemType(user,ItemType.SHAPE).stream().collect(Collectors.groupingBy(Adornment::getGreenRoom));

            //활성화된 주기만 가져오기
            Map<GreenRoom, List<Todo>> todos = todoRepository.findTodoByGreenRoom_UserAndUseYn(user,true).stream().collect(Collectors.groupingBy(Todo::getGreenRoom));

            LocalDateTime today = LocalDate.now(ZoneId.of("Asia/Seoul")).atStartOfDay();
            Predicate<Todo> isToday = t->t.getNextTodoDate().toLocalDate().isEqual(today.toLocalDate());
            Predicate<Todo> isPassed = t->t.getNextTodoDate().toLocalDate().isBefore(today.toLocalDate());


            //현재 키우고 있는 식물을 기준으로 todo와 shape(식물 형태) 반환
            for (GreenRoom greenRoom:greenRooms){
                String shape = adornments.get(greenRoom)!=null? adornments.get(greenRoom).get(0).getItem().getItemName():null;

                //활성화된 주기 ->  오늘 해야하거나 이미 지난 할 일의 개수
                int todoNums = todos.get(greenRoom)!=null? (int) todos.get(greenRoom).stream().filter(t->t.getNextTodoDate()!=null).filter(isToday.or(isPassed)).count():0;
                result.add(new GreenRoomListResponseDto(GreenroomInfoDto.from(greenRoom),shape,todoNums));
            }
            return  result;
        }


        public GreenroomResponseDto getSpecificGreenroomInfo(Long greenroomId) throws IllegalArgumentException{
            GreenRoom greenRoom = greenRoomRepository.findById(greenroomId).orElseThrow(()->new IllegalArgumentException("해당 greenroom 없음."));
            ArrayList<Adornment> adornments= adornmentRepository.findAdornmentByGreenRoom_GreenroomId(greenroomId);

            //활성화된 주기만 가져오기
            ArrayList<Todo> todos=todoRepository.findTodoByGreenRoom_GreenroomIdAndUseYn(greenroomId,true);

            ArrayList<GreenroomItemDto> greenroomItemDtos = new ArrayList<>();
            ArrayList<GreenroomTodoDto> greenroomTodoDtos = new ArrayList<>();


            for(Adornment adornment:adornments){
               greenroomItemDtos.add(new GreenroomItemDto(String.valueOf(adornment.getItem().getItemType()).toLowerCase(),adornment.getItem().getItemName()));
            }

            for(Todo todo :todos){
                if(todo.getNextTodoDate()!=null) {
                    greenroomTodoDtos.add(new GreenroomTodoDto(String.valueOf(todo.getActivity().getName()).toLowerCase(), todo.getNextTodoDate().toLocalDate()));
                }
            }

            return new GreenroomResponseDto(GreenroomInfoDto.from(greenRoom), greenroomItemDtos,greenroomTodoDtos);
        }
    public GreenroomDetailResponseDto getGreenroomDetails(Long greenroomID){
        GreenRoom greenRoom = greenRoomRepository.findById(greenroomID).orElseThrow(()->new IllegalArgumentException("해당 그린룸 없음."));

        //그린룸의 현재 활성화된 주기만 가져오기
        ArrayList<Todo> todoArrayList = todoRepository.findTodoByGreenRoom_GreenroomIdAndUseYn(greenroomID,true);
        ArrayList<ManagementCycleDto> managementCycleDtoArrayList = new ArrayList<>();
        for(Todo todo : todoArrayList){
            managementCycleDtoArrayList.add(ManagementCycleDto.from(todo));
        }

        //그린룸 plant의 키우는 방법 정보 생성
        GrowthInfoDto growthInfoDto = GrowthInfoDto.from(greenRoom.getPlant());

        //그린룸 item 정보 생성
        ArrayList<Adornment> adornments = adornmentRepository.findAdornmentByGreenRoom_GreenroomId(greenroomID);

        ArrayList<GreenroomItemDto> greenroomItemDtos = new ArrayList<>();

        for(Adornment adornment :adornments){
           greenroomItemDtos.add( new GreenroomItemDto(adornment.getItem().getItemType().toString().toLowerCase(),adornment.getItem().getItemName()));
        }

        return new GreenroomDetailResponseDto(GreenroomInfoDto.from(greenRoom),greenroomItemDtos,managementCycleDtoArrayList,growthInfoDto);

    }

    public GreenroomInfoDto modifyGreenroom(Long greenroomId ,MultipartFile imgFile,ArrayList<PatchRequestDto> patchRequest) throws IOException {

        GreenRoom greenRoom = greenRoomRepository.findById(greenroomId).orElseThrow(()->new IllegalArgumentException("해당 그린룸 없음."));


        for(PatchRequestDto patchRequestDto: patchRequest){
            String object = patchRequestDto.getObject();

            if(Objects.equals(object, "name")){
                //이름 변경&등록하는 경우
                if(Objects.equals(patchRequestDto.getOp(), "update")){
                    greenRoom.updateName(patchRequestDto.getValue());
                }
                //이름을 삭제하는 경우
                else if(Objects.equals(patchRequestDto.getOp(), "remove")) {
                    greenRoom.updateName(null);
                }
            }
            else if(Objects.equals(object, "plant")){
                //식물 교체 또는 새로 등록을 원하는 경우
                if(Objects.equals(patchRequestDto.getOp(), "update")){
                    Plant plant= plantRepository.findById(Long.valueOf(patchRequestDto.getValue())).orElseThrow(()->new IllegalArgumentException("해당 식물 없음."));
                    greenRoom.updatePlant(plant);
                }
                //삭제를 원하는 경우
                else if(Objects.equals(patchRequestDto.getOp(), "remove")) {
                    Plant plant = plantRepository.findByPlantCategory("dummy").orElseThrow(()->new IllegalArgumentException("해당 식물 없음."));
                    greenRoom.updatePlant(plant);
                }

            }
            else if(Objects.equals(object, "image")){
                //이미지를 새로 등록&수정하는 경우
                if(Objects.equals(patchRequestDto.getOp(), "update")){
                    try {
                        //새로운 이미지 저장
                        greenRoom.updatePictureUrl(greenroomImageUploader.uploadGreenroomImage(imgFile));

                        //기존에 s3에 저장된 이미지가 있으면 삭제
                        if (greenRoom.getPictureUrl() != null) {
                            greenroomImageUploader.deleteGreenroomImage(greenRoom.getPictureUrl());
                        }
                    } catch (IOException e) {
                        throw new IOException("S3 업로드 실패");
                    }
                }
                //삭제를 원하는 경우
                else if(Objects.equals(patchRequestDto.getOp(), "remove")) {
                    if (greenRoom.getPictureUrl() != null) {
                        greenroomImageUploader.deleteGreenroomImage(greenRoom.getPictureUrl());
                    }
                    greenRoom.updatePictureUrl(null);
                }

            }
            else if(Objects.equals(object, "status")){
                //상태를 변경하는 경우
               if(Objects.equals(patchRequestDto.getOp(), "update")){
                   //식물 비활성화
                   if (Objects.equals(patchRequestDto.getValue(), "disabled")){
                       //그린룸 상태 disabled로 변경
                       greenRoom.updateStatus(GreenRoomStatus.DISABLED);
                       //주기 알림 중단
                       ArrayList<Todo> todoArrayList =  todoRepository.findTodoByGreenRoom_GreenroomIdAndUseYn(greenroomId, true);
                       todoArrayList.forEach(t->t.updateUseYn(false));
                   }

                   //죽었던 식물을 다시 살림(사용하지 않을 예정)
                   else if(Objects.equals(patchRequestDto.getValue(), "enabled")){
                       greenRoom.updateStatus(GreenRoomStatus.ENABLED);
                       ArrayList<Todo> todoArrayList =  todoRepository.findTodoByGreenRoom_GreenroomIdAndUseYn(greenroomId, false);
                       todoArrayList.forEach(t->t.updateUseYn(true));
                   }
               }
               //update 외의 op가 들어오면 exception 반환
               else {throw new IllegalArgumentException("허용되지 않는 operation");}
            }

            else if(Objects.equals(object, "memo")){
                //메모를 수정&등록 하는 경우
                if(Objects.equals(patchRequestDto.getOp(), "update")) {
                    greenRoom.updateMemo(patchRequestDto.getValue());
                }
                //메모를 삭제하는 경우
                else if(Objects.equals(patchRequestDto.getOp(), "remove")) {
                    greenRoom.updateMemo(null);
                }
            }
        }

        return GreenroomInfoDto.from(greenRoom);
    }

    public Boolean checkDuplicateName(String userEmail, String name){
        User user = userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음."));

        //user가 현재 키우고 있는 식물 또는 키웠던 식물 중에 해당 이름을 가진 그린룸이 있는지 확인
        return !greenRoomRepository.findGreenRoomByNameAndUser(name,user).isEmpty();
        //이름이 중복이면 true 반환, 중복이 아니면 false 반환

    }

    public void deleteGreenroom(Long greenroomId){

        //1  storyLike 삭제, todo_log 삭제
        //2. story, todo,  adornment, diary, guestbook 삭제
        //3. greenroom 삭제

        //storyLikeRepository.deleteStoryLikeByGreenroom(greenroomId);  추후 기능
        //storyRepository.deleteStoryByGreenRoom(greenroomId); 추후 기능
        //guestbookRepository.deleteGuestbookByGreenRoom(greenroomId); 추후 기능

//        todoLogRepository.deleteTodoLogByGreenroom(greenroomId);
//        todoRepository.deleteTodoByGreenroom(greenroomId);
//        adornmentRepository.deleteAdornmentByGreenRoom(greenroomId);
//        diaryRepository.deleteDiaryByGreenRoom(greenroomId);
//        greenRoomRepository.deleteById(greenroomId);

        //삭제 대신 상태만 'deleted'로 변경
        GreenRoom greenRoom =  greenRoomRepository.findById(greenroomId).orElseThrow(()->new IllegalArgumentException("해당 그린룸을 찾을 수 없음."));
        greenRoom.updateStatus(GreenRoomStatus.DELETED);



    }


    }