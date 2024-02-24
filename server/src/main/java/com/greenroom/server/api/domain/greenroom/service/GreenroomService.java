package com.greenroom.server.api.domain.greenroom.service;

import com.greenroom.server.api.domain.greenroom.dto.*;
import com.greenroom.server.api.domain.greenroom.entity.*;
import com.greenroom.server.api.domain.greenroom.enums.GreenRoomStatus;
import com.greenroom.server.api.domain.greenroom.enums.ItemType;
import com.greenroom.server.api.domain.greenroom.repository.*;
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
        Plant plant = plantId==null? plantRepository.findByPlantCategory("dummy").orElseThrow(()->new IllegalArgumentException("dummy greenroom을 찾을 수 없음.")): plantRepository.findById(plantId).orElseThrow(() -> new IllegalArgumentException("해당 greenroom을 찾을 수 없습니다."));

        // email 로 user 찾기
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException("해당 user를 찾을 수 없습니다."));

        //imageFile null 값 허용
        if (imgFile.isEmpty()) {
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

        Grade beforeGrade = user.getGrade();
        //물주기 주기 등록 event 발생
        applicationEventPublisher.publishEvent(new TodoCreationDto(savedGreenRoom.getGreenroomId(),firstStartDate,wateringDuration,1L));
        Grade afterGrade = user.getGrade();

        //꾸미기 아이템 등록 event 발생
        applicationEventPublisher.publishEvent(new OneAdornmentCreationDto(shape,savedGreenRoom));


        return new GreenroomRegisterResponseDto(savedGreenRoom.getGreenroomId(),new GradeUpDto(user.getGrade().getLevel(),1,!beforeGrade.equals(afterGrade)));

    }

        public GreenroomResponseDtoWithUser getAllGreenroomInfo(String userEmail,String sort,String range) throws UsernameNotFoundException{
            User user = userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음"));

            ArrayList<GreenRoomStatus> greenroomStatus = new ArrayList<>();
            //전체 범위 가져오기
            if (Objects.equals(range, "all")){greenroomStatus.add(GreenRoomStatus.ENABLED);greenroomStatus.add(GreenRoomStatus.DISABLED);}
            //죽은 식물만 가져오기
            else if(Objects.equals(range, "disabled")){greenroomStatus.add(GreenRoomStatus.DISABLED);}
            //키우고 있는 식물만 가져오기
            else if (Objects.equals(range, "enabled")) {greenroomStatus.add(GreenRoomStatus.ENABLED);}

            ArrayList<GreenRoom> greenRooms = new ArrayList<>();

            //오래된 순으로 가져오기
            if(Objects.equals(sort, "asc")) {greenRooms = greenRoomRepository.findGreenRoomByUserAndStatusIn(user,greenroomStatus,Sort.by(Sort.Order.asc("createDate")));
            }
            //최신순으로 가져오기
            else if(Objects.equals(sort, "desc")){ greenRooms = greenRoomRepository.findGreenRoomByUserAndStatusIn(user,greenroomStatus,Sort.by(Sort.Order.desc("createDate")));
            }

            if(greenRooms.isEmpty()){return null;}

            //그린룸 별 갖고 있는 아이템 가져오기
            Map<GreenRoom, List<Adornment>> adornments = adornmentRepository.findAdornmentByGreenRoom_User(user).stream().collect(Collectors.groupingBy(Adornment::getGreenRoom));;

            ///그린룸 별 활성화된 주기 가져오기
            Map<GreenRoom, List<Todo>> todos = todoRepository.findTodoByGreenRoom_UserAndUseYn(user,true).stream().collect(Collectors.groupingBy(Todo::getGreenRoom));;

            ArrayList<GreenroomResponseDto> greenroomResponseDtoList = new ArrayList<>();

            for(GreenRoom greenRoom: greenRooms){
                HashMap<String,String> items = new HashMap<>();
                items.put("shape",null);
                items.put("hair_accessory",null);
                items.put("glasses",null);
                items.put("background_window",null);
                items.put("background_shelf",null);

                if (adornments.get(greenRoom)!=null){
                    for(Adornment adornment : adornments.get(greenRoom)){
                        items.replace(adornment.getItem().getItemType().name().toLowerCase(),adornment.getItem().getItemName());
                    }
                }


                HashMap<String, LocalDate> activityAndDate = new HashMap<>();
                activityAndDate.put("watering",null);
                activityAndDate.put("repot",null);
                activityAndDate.put("pruning",null);
                activityAndDate.put("nutrition",null);
                activityAndDate.put("ventilation",null);
                activityAndDate.put("spray",null);

                if(todos.get(greenRoom)!=null) {
                    for (Todo todo : todos.get(greenRoom)) {
                        activityAndDate.replace(todo.getActivity().getName().name().toLowerCase(), LocalDate.from(todo.getNextTodoDate()));
                    }
                }

                greenroomResponseDtoList.add(new GreenroomResponseDto(GreenroomInfoDto.from(greenRoom),items,activityAndDate));
            }
            return new GreenroomResponseDtoWithUser(UserDto.from(user),greenroomResponseDtoList);

        }


        public ArrayList<GreenRoomListDto> getGreenroomList(String userEmail,String sort) throws UsernameNotFoundException{
            ArrayList<GreenRoomListDto> result = new ArrayList<>();
            User user = userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음"));
            ArrayList<GreenRoom> greenRooms = new ArrayList<>() ;

            ///user가 현재 키우고 있는 식물만 오래된 순으로 가져오기
            if(Objects.equals(sort, "asc")){ greenRooms = greenRoomRepository.findGreenRoomByUserAndStatus(user,GreenRoomStatus.ENABLED,Sort.by(Sort.Order.asc("createDate"))); } // 오래된순
            /////user가 현재 키우고 있는 식물만 최신 순으로 가져오기
            else if(Objects.equals(sort, "desc")) {greenRooms = greenRoomRepository.findGreenRoomByUserAndStatus(user,GreenRoomStatus.ENABLED,Sort.by(Sort.Order.desc("createDate"))); } //최신순


            //없으면 null 즉시 반환
            if(greenRooms.isEmpty()){return null;}

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
                int todoNums = todos.get(greenRoom)!=null? (int) todos.get(greenRoom).stream().filter(isToday.or(isPassed)).count():0;
                result.add(new GreenRoomListDto(GreenroomInfoDto.from(greenRoom),shape,todoNums));
            }

            return  result;
        }


        public GreenroomResponseDto getSpecificGreenroomInfo(Long greenroomId) throws IllegalArgumentException{
            GreenRoom greenRoom = greenRoomRepository.findById(greenroomId).orElseThrow(()->new IllegalArgumentException("해당 greenroom 없음."));
            ArrayList<Adornment> adornments= adornmentRepository.findAdornmentByGreenRoom_GreenroomId(greenroomId);
            //활성화된 주기만 가져오기
            ArrayList<Todo> todos=todoRepository.findTodoByGreenRoom_GreenroomIdAndUseYn(greenroomId,true);

            HashMap<String,String> items = new HashMap<>();
            items.put("shape",null);
            items.put("hair_accessory",null);
            items.put("glasses",null);
            items.put("background_window",null);
            items.put("background_shelf",null);

            for(Adornment adornment:adornments){
                items.put(String.valueOf(adornment.getItem().getItemType()).toLowerCase(),adornment.getItem().getItemName());
            }

            HashMap<String, LocalDate> activityAndDate = new HashMap<>();
            activityAndDate.put("watering",null);
            activityAndDate.put("repot",null);
            activityAndDate.put("pruning",null);
            activityAndDate.put("nutrition",null);
            activityAndDate.put("ventilation",null);
            activityAndDate.put("spray",null);

            for(Todo todo :todos){
                activityAndDate.put(String.valueOf(todo.getActivity().getName()).toLowerCase(),todo.getNextTodoDate().toLocalDate());
            }

            return new GreenroomResponseDto(GreenroomInfoDto.from(greenRoom), items, activityAndDate);
        }
    }