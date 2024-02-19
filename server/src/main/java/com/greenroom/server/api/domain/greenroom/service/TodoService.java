package com.greenroom.server.api.domain.greenroom.service;

import com.greenroom.server.api.domain.greenroom.dto.GradeUpResponseDto;
import com.greenroom.server.api.domain.greenroom.dto.TodoCreationDto;
import com.greenroom.server.api.domain.greenroom.entity.*;
import com.greenroom.server.api.domain.greenroom.repository.ActivityRepository;
import com.greenroom.server.api.domain.greenroom.repository.GreenRoomRepository;
import com.greenroom.server.api.domain.greenroom.repository.TodoLogRepository;
import com.greenroom.server.api.domain.greenroom.repository.TodoRepository;
import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.repository.UserRepository;
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

    public GradeUpResponseDto completeTodo(Long greenroomId, ArrayList<Long> activityList, String userEmail) throws UsernameNotFoundException{

        ArrayList<Todo> todoList = todoRepository.findAllByGreenRoom_GreenroomIdAndActivity_ActivityIdIn(greenroomId,activityList);

        User user = userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음."));

        LocalDateTime today = LocalDate.now(ZoneId.of("Asia/Seoul")).atStartOfDay();

        for(Todo todo : todoList){

            ///todo 날짜 update
            todo.updateNextTodoDate(today.plusDays(todo.getDuration()));
            todo.updateLastUpdateDate(today);

            //todo Log 생성
            TodoLog todoLog = TodoLog.builder().todo(todo).build();
            todoLogRepository.save(todoLog);
        }

        int activityNum = todoList.size();
        //씨앗 개수 update
        user.updateTotalSeed(activityNum*3);
        user.updateWeeklySeed(activityNum*3);

        Grade beforeGrade = user.getGrade();

        //level 조정
        applicationEventPublisher.publishEvent(user);
        Grade afterGrade = user.getGrade();

        return new GradeUpResponseDto(greenroomId,user.getGrade().getLevel(),activityNum*3,!beforeGrade.equals(afterGrade));
    }

    public void createTodo(TodoCreationDto todoCreationDto){
        GreenRoom greenRoom = greenRoomRepository.findById(todoCreationDto.getGreenRoomId()).orElseThrow(()->new IllegalArgumentException("해당 그린룸을 찾을 수 없음."));
        Activity activity = activityRepository.findById(todoCreationDto.getActivityId()).orElseThrow(()->new IllegalArgumentException("해당 activity를 찾을 수 없음."));

        LocalDateTime nextTodoDate = todoCreationDto.getFirstStartDate().plusDays(todoCreationDto.getDuration());

        //todo 생성
        Todo todo = Todo
                .builder()
                .greenRoom(greenRoom)
                .useYn(true)
                .firstStartDate(todoCreationDto.getFirstStartDate())
                .nextTodoDate(nextTodoDate)
                .duration(todoCreationDto.getDuration())
                .activity(activity)
                .build();
        todoRepository.save(todo);

        //주기 설정 완료 시 포인트 1점씩 추가
        greenRoom.getUser().updateWeeklySeed(1);
        greenRoom.getUser().updateTotalSeed(1);

        // level 조정.
        applicationEventPublisher.publishEvent(greenRoom.getUser());

    }


}
