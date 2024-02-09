package com.greenroom.server.api.domain.user.controller;

import com.greenroom.server.api.domain.user.dto.UserDto;
import com.greenroom.server.api.domain.user.exception.UserAlreadyExist;
import com.greenroom.server.api.domain.user.service.UserService;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@RequestBody UserDto userDto){

        ApiResponse response = ApiResponse.success();
        try{
            userService.signUp(userDto);
        }catch (UserAlreadyExist e){
            response = ApiResponse.failed(e.getResponseCodeEnum(),userDto.getEmail());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse> printMyInfo(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(ApiResponse.success(user.getUsername()));
    }
}
