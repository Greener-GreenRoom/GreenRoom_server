package com.greenroom.server.api.domain.greenroom.service;

import com.greenroom.server.api.domain.greenroom.dto.*;
import com.greenroom.server.api.domain.greenroom.entity.*;
import com.greenroom.server.api.domain.greenroom.repository.ActivityRepository;
import com.greenroom.server.api.domain.greenroom.repository.GreenRoomRepository;
import com.greenroom.server.api.domain.greenroom.repository.TodoLogRepository;
import com.greenroom.server.api.domain.greenroom.repository.TodoRepository;
import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.repository.UserRepository;
import com.greenroom.server.api.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final TodoLogRepository todoLogRepository;
    private final GreenRoomRepository greenRoomRepository;
    private final ActivityRepository activityRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    public GradeUpDto completeTodo(Long greenroomId, ArrayList<Long> activityList, String userEmail) throws UsernameNotFoundException{

        //요청을 보낸 activity에 해당하는 todo가져오기
        List<Todo> todoList = todoRepository.findAllByGreenRoom_GreenroomIdAndActivity_ActivityIdIn(greenroomId,activityList).stream().filter(t->t.getUseYn()&&t.getNextTodoDate()!=null).toList();

        User user = userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음."));

        LocalDateTime today = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        //완료된 미션 개수
        int activityNum=0;

        for(Todo todo : todoList){
            if(todo.getNextTodoDate()!=null){ //주기 등록을 마친 경우에만
                activityNum+=1;
                ///todo 날짜 update
                todo.updateNextTodoDate(today.plusDays(todo.getDuration()));
                todo.updateLastUpdateDate(today);

                //todo Log 생성
                TodoLog todoLog = TodoLog.builder().todo(todo).build();
                todoLogRepository.save(todoLog);
            }
        }

        //씨앗 개수 update. 미션 하나 당 2점씩 추가.
        user.updateTotalSeed(activityNum*2);
        user.updateWeeklySeed(activityNum*2);

        Grade beforeGrade = user.getGrade();
        //level 조정
        applicationEventPublisher.publishEvent(user);
        Grade afterGrade = user.getGrade();

        return new GradeUpDto(user.getGrade().getLevel(),activityNum*2,!beforeGrade.equals(afterGrade));
    }

    public void createTodo(TodoCreationDto todoCreationDto){

        GreenRoom greenRoom = greenRoomRepository.findById(todoCreationDto.getGreenRoomId()).orElseThrow(()->new IllegalArgumentException("해당 그린룸을 찾을 수 없음."));
        Activity activity = activityRepository.findById(todoCreationDto.getActivityId()).orElseThrow(()->new IllegalArgumentException("해당 activity를 찾을 수 없음."));

        LocalDateTime nextTodoDate ;
        if(todoCreationDto.getFirstStartDate()!=null&&todoCreationDto.getDuration()!=null){
            nextTodoDate = todoCreationDto.getFirstStartDate().plusDays(todoCreationDto.getDuration());
        }
        else{nextTodoDate=null;}

        //todo 생성
        Todo todo = Todo
                .builder()
                .greenRoom(greenRoom)
                .useYn(true)
                .firstStartDate(todoCreationDto.getFirstStartDate())
                .lastUpdateDate(todoCreationDto.getFirstStartDate())
                .nextTodoDate(nextTodoDate)
                .duration(todoCreationDto.getDuration())
                .activity(activity)
                .build();
        todoRepository.save(todo);
    }

    //활성화된 주기 가져오기
    public List<ManagementCycleDto> getActiveTodoCycle(Long greenroomId){
        //현재 활성화된 주기만 가져오기
        ArrayList<Todo> todoArrayList = todoRepository.findTodoByGreenRoom_GreenroomIdAndUseYn(greenroomId,true);
        return todoArrayList.stream().map(ManagementCycleDto::from).toList();
    }


    //주기 변경하기
    public ManagementCycleDto modifyTodoCycle(Long greenroomId, TodoModifyingRequestDto todoModifyingRequestDto){

        if(todoModifyingRequestDto.getDuration()==null||todoModifyingRequestDto.getLastUpdateDate()==null||todoModifyingRequestDto.getActivityId()==null){
            throw new IllegalArgumentException("요청 값에 null을 허용하지 않음");
        }

        Todo todo = todoRepository.findByGreenRoomGreenroomIdAndActivity_ActivityId(greenroomId, todoModifyingRequestDto.getActivityId()).orElseThrow(()->new IllegalArgumentException("해당 그린룸의 todo를 찾을 수 없음."));

        //1. 주기를 변경한다
        todo.updateDuration(todoModifyingRequestDto.getDuration());

        //2. 마지막 수행 날짜를 변경한다
        LocalDateTime lastUpdateDate = LocalDate.parse(todoModifyingRequestDto.getLastUpdateDate()).atStartOfDay();
        todo.updateLastUpdateDate(lastUpdateDate);

        //3. 다음 수행 날짜를 변경한다
        todo.updateNextTodoDate(todo.getLastUpdateDate().plusDays(todo.getDuration()));

        return ManagementCycleDto.from(todo);
    }

    //주기 비활성화/활성화
    public void modifyTodoState(Long greenroomId, HashMap<String,String> patchRequest){


        ArrayList<Long> activityIdList = new ArrayList<>(patchRequest.keySet().stream().map(Long::valueOf).toList());

        //수정을 요구한 모든 activity에 대해서 수정 작업 진행
        for(Long activityId : activityIdList){

            String action = patchRequest.get(String.valueOf(activityId));

            Todo todo =  todoRepository.findByGreenRoomGreenroomIdAndActivity_ActivityId(greenroomId,activityId).orElse(null);

            //해당 할 일에 대해서 주기 활성화를 요청한 경우
            if(Objects.equals(action, "activate")){

                //처음으로 활성화 하는 경우.
                if(todo==null){
                    //주기는 설정하지 않은 채 todo 생성.
                    createTodo(new TodoCreationDto(greenroomId,null,null,activityId));
                }
                //비활성화 했던 것을 다시 활성화 하는 경우 ->  상태만 update
                else{todo.updateUseYn(true);}

            }

            // 할 일에 대해서 주기 비활성화를 요청한 경우
            else if (Objects.equals(action, "deactivate")) {
                //비활성화 할 주기가 있으면 비활성화. 비활성화 할 주기가 없으면 pass
                if(todo!=null){todo.updateUseYn(false);}
            }

        }

    }

}
