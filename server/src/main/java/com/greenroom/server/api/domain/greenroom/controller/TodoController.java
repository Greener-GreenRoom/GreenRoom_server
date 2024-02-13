package com.greenroom.server.api.domain.greenroom.controller;


import com.greenroom.server.api.domain.greenroom.dto.TodoRequestDto;
import com.greenroom.server.api.domain.greenroom.entity.Todo;
import com.greenroom.server.api.domain.greenroom.service.TodoService;
import com.greenroom.server.api.enums.ResponseCodeEnum;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TodoController{

    private final TodoService todoService;

@PostMapping("/greenroom/todo")
public ResponseEntity<ApiResponse> completeTodo(@RequestBody TodoRequestDto todoRequestDto,@AuthenticationPrincipal UserDetails userDetails) {
    String userEmail = userDetails.getUsername();
    Long greenroom_id = todoRequestDto.getGreenroom_id();
    ArrayList<Long> todo_list = todoRequestDto.getTodo_list();

    try {
        int totalPoint = todoService.completeTodo(greenroom_id, todo_list, userEmail);
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("seed", totalPoint);
        return ResponseEntity.ok(ApiResponse.success(result));

    } catch (RuntimeException e) {

        return ResponseEntity.ok(ApiResponse.failed(ResponseCodeEnum.FAILED, e.getMessage()));
    }
}

}
