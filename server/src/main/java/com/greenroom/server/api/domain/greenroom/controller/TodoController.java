package com.greenroom.server.api.domain.greenroom.controller;


import com.greenroom.server.api.domain.greenroom.dto.GradeUpDto;
import com.greenroom.server.api.domain.greenroom.dto.GreenroomRegisterResponseDto;
import com.greenroom.server.api.domain.greenroom.dto.TodoRequestDto;
import com.greenroom.server.api.domain.greenroom.service.TodoService;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TodoController{

    private final TodoService todoService;

@PostMapping("/greenroom/todo")
public ResponseEntity<ApiResponse> completeTodo(@RequestBody TodoRequestDto todoRequestDto,@AuthenticationPrincipal UserDetails userDetails) {

    String userEmail = userDetails.getUsername();
    Long greenroomId = todoRequestDto.getGreenroomId();
    ArrayList<Long> todoList = todoRequestDto.getActivityList();

    GradeUpDto result = todoService.completeTodo(greenroomId, todoList, userEmail);
    return ResponseEntity.ok(ApiResponse.success(result));

    }


}

