package com.greenroom.server.api.domain.greenroom.service;

import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final TodoLogRepository todoLogRepository;
    LocalDateTime today = LocalDateTime.now();

    @Transactional
    public int completeTodo(Long greenroom_id, ArrayList<Long> activity_list, String userEmail){


        int gap = 6-activity_list.size();

        for(int i=0;i<gap;i++){
            activity_list.add(null);
        }

        ArrayList<Todo> todo_list =  todoRepository.findByGreenroomAndActivity(greenroom_id,activity_list).orElseThrow(()->new RuntimeException("조건에 맞는 todo를 찾을 수 없음."));
        if (todo_list.isEmpty()) { throw new RuntimeException("해당 greenroom 또는 해당 greenroom의 할 일이 존재하지 않음.") ; }
        int activity_num = todo_list.size();

        User user = userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음."));


        for(Todo todo : todo_list){
            ///todo 날짜 update
            todo.updateNextTodoDate(today.plusDays(todo.getDuration()));
            todo.updateLastUpdateDate(today);

            //todo Log 생성
            TodoLog todoLog = TodoLog.builder().todo(todo).build();
            todoLogRepository.save(todoLog);
        }

        //씨앗 개수 update
        user.updateTotalSeed(activity_num);
        user.updateWeeklySeed(activity_num);

        return activity_num;
    }

    public HashMap<String, LocalDate> parseToGreenroomTodoDto(GreenRoom greenRoom){
       ArrayList<Todo> todos =  todoRepository.findTodoByGreenRoomAndUseYn(greenRoom,true).orElseThrow(()->new RuntimeException("해당 그린룸에 대한 할 일을 찾을 수 없음"));
        HashMap<String, LocalDate> activityAndDate = new HashMap<>();
        activityAndDate.put("watering",null);
        activityAndDate.put("repot",null);
        activityAndDate.put("pruning",null);
        activityAndDate.put("nutrition",null);
        activityAndDate.put("ventilation",null);
        activityAndDate.put("spray",null);

       for(Todo todo :todos){
           activityAndDate.replace(todo.getActivity().getName().name().toLowerCase(), LocalDate.from(todo.getNextTodoDate()));
       }
       return activityAndDate;

    }



}
