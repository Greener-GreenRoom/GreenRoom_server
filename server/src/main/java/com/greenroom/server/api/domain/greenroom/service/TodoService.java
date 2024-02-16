package com.greenroom.server.api.domain.greenroom.service;

import com.greenroom.server.api.domain.greenroom.entity.Todo;
import com.greenroom.server.api.domain.greenroom.entity.TodoLog;
import com.greenroom.server.api.domain.greenroom.repository.TodoLogRepository;
import com.greenroom.server.api.domain.greenroom.repository.TodoRepository;
import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;


@Service
@RequiredArgsConstructor
@Slf4j
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final TodoLogRepository todoLogRepository;
    LocalDateTime today = LocalDateTime.now();

    @Transactional
    public int completeTodo(Long greenroomId, ArrayList<Long> activityList, String userEmail) throws UsernameNotFoundException{

        ArrayList<Todo> todoList = todoRepository.findAllByGreenRoom_GreenroomIdAndActivity_ActivityIdIn(greenroomId,activityList);

        int activityNum = todoList.size();

        User user = userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음."));


        for(Todo todo : todoList){
            ///todo 날짜 update
            todo.updateNextTodoDate(today.plusDays(todo.getDuration()));
            todo.updateLastUpdateDate(today);

            //todo Log 생성
            TodoLog todoLog = TodoLog.builder().todo(todo).build();
            todoLogRepository.save(todoLog);
        }

        //씨앗 개수 update
        user.updateTotalSeed(activityNum);
        user.updateWeeklySeed(activityNum);

        return activityNum;
    }



}
