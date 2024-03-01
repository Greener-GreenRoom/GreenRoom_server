package com.greenroom.server.api.domain.user.controller;

import com.greenroom.server.api.domain.user.dto.UserDto;
import com.greenroom.server.api.security.service.CustomUserDetailService;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final CustomUserDetailService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@RequestBody UserDto userDto){
        userService.save(userDto);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteUser(@RequestBody UserDeleteDto userDeleteDto){
        userService.deleteUser(userDeleteDto.getUserEmail());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse> printMyInfo(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(ApiResponse.success(user.getUsername()));
    }

    @GetMapping("/grade/level")
    public ResponseEntity<ApiResponse> getUserLevel(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(ApiResponse.success(userService.getUserLevel(userDetails.getUsername())));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDeleteDto{
        public String userEmail;
    }
}