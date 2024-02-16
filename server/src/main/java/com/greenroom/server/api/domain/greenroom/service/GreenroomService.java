package com.greenroom.server.api.domain.greenroom.service;

import com.greenroom.server.api.domain.greenroom.dto.GreenRoomListDto;
import com.greenroom.server.api.domain.greenroom.dto.GreenroomInfoDto;
import com.greenroom.server.api.domain.greenroom.dto.GreenroomRegistrationDto;
import com.greenroom.server.api.domain.greenroom.dto.GreenroomResponseDto;
import com.greenroom.server.api.domain.greenroom.entity.*;
import com.greenroom.server.api.domain.greenroom.enums.ItemType;
import com.greenroom.server.api.domain.greenroom.repository.*;
import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.repository.UserRepository;
import com.greenroom.server.api.utils.ImageUploader.GreenroomImageUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final ActivityRepository activityRepository;
    private final TodoRepository todoRepository;
    private final GreenroomImageUploader greenroomImageUploader;

    LocalDateTime today = LocalDateTime.now();

    ////greenroom 등록 + todo에 물주기 등록 + 식물 키우는 횟수 count 증가 + greenroom 꾸미기 item 등록

    public Long registerGreenRoom(GreenroomRegistrationDto greenroomRegistrationDto, String userEmail, MultipartFile imgFile) throws RuntimeException, IOException {

        String shape = greenroomRegistrationDto.getShape();
        String name = greenroomRegistrationDto.getName();
        Long plantId = greenroomRegistrationDto.getPlantId();
        Integer wateringDuration = greenroomRegistrationDto.getWateringDuration();
        int lastWatering =greenroomRegistrationDto.getLastWatering();

        String imageUrl = null;
        Plant plant = null;
        User user = null;
        Activity activity = null;
        Item item = null ;


        //id값으로 plant 찾기
        plant = plantRepository.findById(plantId).orElseThrow(()->new RuntimeException("해당 식물을 찾을 수 없음"));

        // email로 user 찾기
        user = userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없습니다."));

        activity = activityRepository.findById(1L).orElseThrow(()->new RuntimeException("해당 activity 없음"));

        ///item 이름으로 item 찾기
        item = itemRepository.findItemByItemName(shape).orElseThrow(()->new RuntimeException("해당 식물 모양을 찾을 수 없음"));

        try
        {
            // s3에 이미지 업로드
            imageUrl = greenroomImageUploader.uploadGreenroomImage(imgFile);
        }
        catch (IOException e){
            throw new IOException("S3 업로드 실패");
        }

        //datetime 설정
        LocalDateTime firstStartDate = today.minusDays(lastWatering);
        LocalDateTime nextTodoDate = firstStartDate.plusDays(wateringDuration);

        //그린룸 추가
        GreenRoom greenRoom =
                GreenRoom.builder()
                .plant(plant)
                .user(user)
                .name(name)
                .pictureUrl(imageUrl)
                .build();
        GreenRoom savedGreenRoom = greenRoomRepository.save(greenRoom);

        //todo 생성
        Todo todo = Todo
                .builder()
                .greenRoom(savedGreenRoom)
                .useYn(true)
                .firstStartDate(firstStartDate)
                .nextTodoDate(nextTodoDate)
                .duration(wateringDuration)
                .activity(activity)
                .build();
        todoRepository.save(todo);


        ////해당 식물을 키우는 횟수 증가
        assert plant != null;
        plant.updatePlantCount();

        //////생성된 그린룸의 아이템 추가
        Adornment adornment = Adornment.builder()
                .item(item)
                .greenRoom(savedGreenRoom).build();
        adornmentRepository.save(adornment);

        return savedGreenRoom.getGreenroomId();

    }

    public ArrayList<GreenroomResponseDto> getAllGreenroomInfo(String userEmail) throws RuntimeException{
        User user = userRepository.findByEmail(userEmail).orElseThrow(()->new RuntimeException("해당 user를 찾을 수 없음"));

        ArrayList<GreenRoom> greenRooms = greenRoomRepository.findGreenRoomByUser(user);
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


    public ArrayList<GreenRoomListDto> getGreenroomList(String userEmail) throws RuntimeException{
        ArrayList<GreenRoomListDto> result = new ArrayList<>();
        User user = userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음"));

        ArrayList<GreenRoom> greenRooms = greenRoomRepository.findGreenRoomByUser(user);
        if(greenRooms.isEmpty()){return null;}

        Map<GreenRoom, List<Adornment>> adornments = adornmentRepository.findAdornmentByGreenRoom_UserAndItem_ItemType(user,ItemType.SHAPE).stream().collect(Collectors.groupingBy(Adornment::getGreenRoom));
        Map<GreenRoom, List<Todo>> todos = todoRepository.findTodoByGreenRoom_User(user).stream().collect(Collectors.groupingBy(Todo::getGreenRoom));


        Predicate<Todo> isToday = t->t.getNextTodoDate().toLocalDate().isEqual(today.toLocalDate());
        Predicate<Todo> isPassed = t->t.getNextTodoDate().toLocalDate().isBefore(today.toLocalDate());


        for (GreenRoom greenRoom:greenRooms){
            String shape = adornments.get(greenRoom).get(0).getItem().getItemName();
            int todoNums = (int) todos.get(greenRoom).stream().filter(isToday.or(isPassed)).count();
            result.add(new GreenRoomListDto(greenRoom.getGreenroomId(),greenRoom.getName(),shape,todoNums));
        }

        return  result;
    }


    public GreenroomResponseDto getSpecificGreenroomInfo(Long greenroomId) throws RuntimeException{
        GreenRoom greenRoom = greenRoomRepository.findById(greenroomId).orElseThrow(()->new RuntimeException("해당 greenroom 없음."));
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
