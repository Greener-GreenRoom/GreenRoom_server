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
import org.springframework.data.domain.PageRequest;
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
    private final ItemRepository itemRepository;
    private final AdornmentRepository adornmentRepository;
    private final PlantRepository plantRepository;
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final GreenroomImageUploader greenroomImageUploader;

    private final ApplicationEventPublisher applicationEventPublisher;

    ////greenroom 등록 + todo에 물주기 등록(레벨업) + 식물 키우는 횟수 count 증가 + greenroom 꾸미기 item 등록

    public GradeUpResponseDto registerGreenRoom(GreenroomRegistrationDto greenroomRegistrationDto, String userEmail, MultipartFile imgFile) throws IllegalArgumentException, IOException,UsernameNotFoundException {

        String shape = greenroomRegistrationDto.getShape();
        String name = greenroomRegistrationDto.getName();
        Long plantId = greenroomRegistrationDto.getPlantId();
        Integer wateringDuration = greenroomRegistrationDto.getWateringDuration();
        String lastWatering = greenroomRegistrationDto.getLastWatering();
        LocalDateTime firstStartDate = LocalDate.parse(lastWatering).atStartOfDay();
        String imageUrl;


        Plant plant = plantId==null? null: plantRepository.findById(plantId).orElseThrow(() -> new IllegalArgumentException("해당 greenroom을 찾을 수 없습니다."));

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

        Grade beforeGrade = user.getGrade();
        //물주기 주기 등록 event 발생
        applicationEventPublisher.publishEvent(new TodoCreationDto(savedGreenRoom.getGreenroomId(),firstStartDate,wateringDuration,1L));
        Grade afterGrade = user.getGrade();

        //꾸미기 아이템 등록 event 발생
        applicationEventPublisher.publishEvent(new OneAdornmentCreationDto(shape,savedGreenRoom));

        ////해당 식물을 키우는 횟수 증가
        if (plant!=null){plant.updatePlantCount();}

        return new GradeUpResponseDto(savedGreenRoom.getGreenroomId(),user.getGrade().getLevel(),1,!beforeGrade.equals(afterGrade));

    }

        public ArrayList<GreenroomResponseDto> getAllGreenroomInfo(String userEmail,String sort,String range) throws UsernameNotFoundException{
            User user = userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음"));


            ArrayList<GreenRoomStatus> greenroomStatus = new ArrayList<>();
            if (Objects.equals(range, "all")){greenroomStatus.add(GreenRoomStatus.ENABLED);greenroomStatus.add(GreenRoomStatus.DISABLED);}
            else if(Objects.equals(range, "disabled")){greenroomStatus.add(GreenRoomStatus.DISABLED);}
            else if (Objects.equals(range, "enabled")) {greenroomStatus.add(GreenRoomStatus.ENABLED);}

            ArrayList<GreenRoom> greenRooms = new ArrayList<>();
            if(Objects.equals(sort, "asc")) {greenRooms = greenRoomRepository.findGreenRoomByUserAndStatusIn(user,greenroomStatus,Sort.by(Sort.Order.asc("createDate")));
            }
            else if(Objects.equals(sort, "desc")){ greenRooms = greenRoomRepository.findGreenRoomByUserAndStatusIn(user,greenroomStatus,Sort.by(Sort.Order.desc("createDate")));
            }


            if(greenRooms.isEmpty()){return null;}

            Map<GreenRoom, List<Adornment>> adornments = adornmentRepository.findAdornmentByGreenRoom_User(user).stream().collect(Collectors.groupingBy(Adornment::getGreenRoom));;
            Map<GreenRoom, List<Todo>> todos = todoRepository.findTodoByGreenRoom_User(user).stream().collect(Collectors.groupingBy(Todo::getGreenRoom));;

            ArrayList<GreenroomResponseDto> greenroomResponseDtoList = new ArrayList<>();

            for(GreenRoom greenRoom: greenRooms){
                HashMap<String,String> items = new HashMap<>();
                items.put("shape",null);
                items.put("hair_accessory",null);
                items.put("glasses",null);
                items.put("glass_accessory",null);
                items.put("shelf_accessory",null);

                for(Adornment adornment : adornments.get(greenRoom)){
                    items.replace(adornment.getItem().getItemType().name().toLowerCase(),adornment.getItem().getItemName());
                }

                HashMap<String, LocalDate> activityAndDate = new HashMap<>();
                activityAndDate.put("watering",null);
                activityAndDate.put("repot",null);
                activityAndDate.put("pruning",null);
                activityAndDate.put("nutrition",null);
                activityAndDate.put("ventilation",null);
                activityAndDate.put("spray",null);

                for(Todo todo: todos.get(greenRoom)){
                    activityAndDate.replace(todo.getActivity().getName().name().toLowerCase(), LocalDate.from(todo.getNextTodoDate()));
                }

                greenroomResponseDtoList.add(new GreenroomResponseDto(GreenroomInfoDto.from(greenRoom),items,activityAndDate));
            }
            return greenroomResponseDtoList;
        }


        public ArrayList<GreenRoomListDto> getGreenroomList(String userEmail) throws UsernameNotFoundException{
            ArrayList<GreenRoomListDto> result = new ArrayList<>();
            User user = userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음"));

            ArrayList<GreenRoom> greenRooms = greenRoomRepository.findGreenRoomByUser(user,Sort.by(Sort.Order.desc("createDate")));

            if(greenRooms.isEmpty()){return null;}

            Map<GreenRoom, List<Adornment>> adornments = adornmentRepository.findAdornmentByGreenRoom_UserAndItem_ItemType(user,ItemType.SHAPE).stream().collect(Collectors.groupingBy(Adornment::getGreenRoom));
            Map<GreenRoom, List<Todo>> todos = todoRepository.findTodoByGreenRoom_User(user).stream().collect(Collectors.groupingBy(Todo::getGreenRoom));

            LocalDateTime today = LocalDate.now(ZoneId.of("Asia/Seoul")).atStartOfDay();
            Predicate<Todo> isToday = t->t.getNextTodoDate().toLocalDate().isEqual(today.toLocalDate());
            Predicate<Todo> isPassed = t->t.getNextTodoDate().toLocalDate().isBefore(today.toLocalDate());


            for (GreenRoom greenRoom:greenRooms){
                String shape = adornments.get(greenRoom).get(0).getItem().getItemName();
                int todoNums = (int) todos.get(greenRoom).stream().filter(isToday.or(isPassed)).count();
                Long plantId = greenRoom.getPlant()== null? null:greenRoom.getPlant().getPlantId();
                String plantName = greenRoom.getPlant()==null?null:greenRoom.getPlant().getDistributionName();
                result.add(new GreenRoomListDto(greenRoom.getGreenroomId(),plantId,plantName,greenRoom.getName(),shape,todoNums));
            }

            return  result;
        }


        public GreenroomResponseDto getSpecificGreenroomInfo(Long greenroomId) throws IllegalArgumentException{
            GreenRoom greenRoom = greenRoomRepository.findById(greenroomId).orElseThrow(()->new IllegalArgumentException("해당 greenroom 없음."));
            ArrayList<Adornment> adornments= adornmentRepository.findAdornmentByGreenRoom_GreenroomId(greenroomId);
            ArrayList<Todo>todos=todoRepository.findTodoByGreenRoom_GreenroomId(greenroomId);

            HashMap<String,String> items = new HashMap<>();
            items.put("shape",null);
            items.put("hair_accessory",null);
            items.put("glasses",null);
            items.put("glass_accessory",null);
            items.put("shelf_accessory",null);

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