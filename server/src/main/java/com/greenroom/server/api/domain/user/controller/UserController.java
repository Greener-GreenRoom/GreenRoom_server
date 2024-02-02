package com.greenroom.server.api.domain.user.controller;

import com.greenroom.server.api.domain.user.dto.UserDto;
import com.greenroom.server.api.domain.user.exception.UserAlreadyExist;
import com.greenroom.server.api.domain.user.service.UserService;
import com.greenroom.server.api.utils.ResponseWithData;
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
    public ResponseEntity<ResponseWithData> signup(@RequestBody UserDto userDto){

        ResponseWithData response = ResponseWithData.success();
        try{
            userService.signUp(userDto);
        }catch (UserAlreadyExist e){
            response = ResponseWithData.failed(e.getResponseCodeEnum(),userDto.getEmail());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<ResponseWithData> printMyInfo(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(ResponseWithData.success(user.getUsername()));
    }
}
