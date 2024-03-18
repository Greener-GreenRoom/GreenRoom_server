package com.greenroom.server.api.domain.greenroom.controller;


import com.greenroom.server.api.domain.greenroom.dto.GradeUpDto;
import com.greenroom.server.api.domain.greenroom.dto.TodoModifyingRequestDto;
import com.greenroom.server.api.domain.greenroom.service.TodoService;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TodoController{

    private final TodoService todoService;

    //할 일 완료하기
    @PatchMapping("/greenrooms/{id}/todo")
    public ResponseEntity<ApiResponse> completeTodo(@RequestBody TodoRequestDto todoRequestDto, @AuthenticationPrincipal UserDetails userDetails, @PathVariable(value = "id")Long greenroomId) {

        String userEmail = userDetails.getUsername();
        ArrayList<Long> todoList = todoRequestDto.getActivityList();

        GradeUpDto result = todoService.completeTodo(greenroomId, todoList, userEmail);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    //활성화된 주기 가져오기
    @GetMapping("/greenrooms/{id}/todo")
    public ResponseEntity<ApiResponse> getActiveTodoCycle(@PathVariable(value ="id")Long greenroomId){
        return ResponseEntity.ok(ApiResponse.success(todoService.getActiveTodoCycle(greenroomId)));
    }

    //주기 수정하기
    @PatchMapping("/greenrooms/{id}/todo/cycle")
    public ResponseEntity<ApiResponse> modifyTodoCycle(@PathVariable(value ="id")Long greenroomId, @RequestBody TodoModifyingRequestDto todoModifyingRequestDto){
        return ResponseEntity.ok(ApiResponse.success(todoService.modifyTodoCycle(greenroomId, todoModifyingRequestDto)));
    }

    //주기 활성화/비활성화
    @PutMapping("/greenrooms/{id}/todo")
    public ResponseEntity<ApiResponse> modifyTodoState(@PathVariable(value ="id")Long greenroomId, @RequestBody HashMap<String,String> patchRequest){

        todoService.modifyTodoState(greenroomId,patchRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/todo/{year}/{month}")
    public ResponseEntity<ApiResponse> getTodoPerMonth(@AuthenticationPrincipal UserDetails userDetails,@PathVariable(value = "year")Integer year, @PathVariable(value = "month")Integer month,@RequestParam(value = "sort",required = false,defaultValue = "asc")String sort, @RequestParam(value = "target",required = false)Long greenroomId,@RequestParam(value = "type",required = false)Long activityId){

        return ResponseEntity.ok(ApiResponse.success(todoService.getTodoPerMonth(userDetails.getUsername(), year,month,sort,greenroomId,activityId)));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TodoRequestDto{
        public ArrayList<Long> activityList;
    }

}

