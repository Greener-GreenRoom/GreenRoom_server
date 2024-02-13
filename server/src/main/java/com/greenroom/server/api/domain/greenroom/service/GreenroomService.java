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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.function.Predicate;

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
    private final AdornmentService adornmentService;
    private final TodoService todoService;

    LocalDateTime today = LocalDateTime.now();

    ////greenroom 등록 + todo에 물주기 등록 + 식물 키우는 횟수 count 증가 + greenroom 꾸미기 item 등록

    public Long registerGreenRoom(GreenroomRegistrationDto greenroomRegistrationDto, String userEmail, MultipartFile imgFile) throws IllegalArgumentException, IOException {

        String shape = greenroomRegistrationDto.getShape();
        String name = greenroomRegistrationDto.getName();
        Long plant_id = greenroomRegistrationDto.getPlant_id();
        Integer wateringDuration = greenroomRegistrationDto.getWateringDuration();
        int lastWatering =greenroomRegistrationDto.getLastWatering();

        String imageUrl = null;
        Plant plant = null;
        User user = null;
        Activity activity = null;
        Item item = null ;

        try{
            //id값으로 plant 찾기
            plant = plantRepository.findById(plant_id).orElseThrow(()->new IllegalArgumentException("해당 식물을 찾을 수 없음"));

            // email로 user 찾기
            user = userRepository.findByEmail(userEmail).orElseThrow(()->new IllegalArgumentException("해당 user를 찾을 수 없습니다."));

            activity = activityRepository.findById(1L).orElseThrow(()->new IllegalArgumentException("해당 activity 없음"));

            ///item 이름으로 item 찾기
            item = itemRepository.findItemByItemName(shape).orElseThrow(()->new IllegalArgumentException("해당 식물 모양을 찾을 수 없음"));
        }

        catch (RuntimeException e){
            throw  new RuntimeException(e.getMessage());
        }

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

    public ArrayList<GreenroomResponseDto> getAllGreenroomInfo(String userEmail){
        User user = userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음"));

        ArrayList<GreenRoom> greenroom_list = greenRoomRepository.findGreenRoomByUser(user).orElseThrow(()-> new RuntimeException("해당 user의 greenroom 없음."));

        if (greenroom_list==null){return null;}

        ArrayList<GreenroomResponseDto> greenroomResponseDto_list = new ArrayList<GreenroomResponseDto>();

        for (GreenRoom greenRoom : greenroom_list){
            greenroomResponseDto_list.add(
                    new GreenroomResponseDto(
                            GreenroomInfoDto.from(greenRoom),
                            adornmentService.parseToAdornmentDto(greenRoom),
                            todoService.parseToGreenroomTodoDto(greenRoom)
                    )
            ) ;
        }
        return greenroomResponseDto_list;
    }

    public ArrayList<GreenRoomListDto> getGreenroomList(String userEmail){

        ArrayList<GreenRoomListDto> result = new ArrayList<>();
        User user = userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음"));

        ArrayList<GreenRoom> greenRooms = greenRoomRepository.findGreenRoomByUser(user).orElseThrow(()->new RuntimeException("해당 user의 greenroom을 찾을 수 없음."));

        if(greenRooms==null){
            return null;
        }

        Predicate<Todo> isToday = t->t.getNextTodoDate().toLocalDate().isEqual(today.toLocalDate());
        Predicate<Todo> isPassed = t->t.getNextTodoDate().toLocalDate().isBefore(today.toLocalDate());

        for(GreenRoom greenRoom:greenRooms){
            Adornment adornment = adornmentRepository.findAdornmentByGreenRoomAndItem_ItemType(greenRoom, ItemType.SHAPE).orElseThrow(()->new RuntimeException("해당 shape의 item을 찾을 수 없음"));
            ArrayList <Todo> todos = todoRepository.findTodoByGreenRoomAndUseYn(greenRoom,true).orElseThrow(()->new RuntimeException("해당 greenroom의 todo를 찾을 수 없음"));
            int numOfTodoToday = (int) todos.stream().filter(isToday.or(isPassed)).count();
            result.add(new GreenRoomListDto(greenRoom.getGreenroomId(),greenRoom.getName(),adornment.getItem().getItemName(),numOfTodoToday));
        }
        return result;
    }

    public GreenroomResponseDto getSpecificGreenroomInfo(Long greenroom_id){

        GreenRoom greenRoom = greenRoomRepository.findById(greenroom_id).orElseThrow(()->new RuntimeException("해당 greenroom을 찾을 수 없음."));
        return new GreenroomResponseDto(
                GreenroomInfoDto.from(greenRoom),
                adornmentService.parseToAdornmentDto(greenRoom),
                todoService.parseToGreenroomTodoDto(greenRoom)
        );
    }
}
