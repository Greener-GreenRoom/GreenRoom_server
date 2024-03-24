package com.greenroom.server.api.domain.user.controller;

import com.greenroom.server.api.domain.user.dto.MyPageDto;
import com.greenroom.server.api.domain.user.service.UserService;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/myPage")
public class MyPageController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse> myPageMain(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(ApiResponse.success(userService.getMyPageInfo(user.getUsername())));
    }

    @GetMapping("/grade")
    public ResponseEntity<ApiResponse> gradeInfo(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(ApiResponse.success(userService.getMyGradeInfo(user.getUsername())));
    }

    @PostMapping("/profile/name")
    public ResponseEntity<ApiResponse> validateName(@AuthenticationPrincipal User user,@RequestBody MyPageDto.MyPageProfileNameValidateDto dto){
        return ResponseEntity.ok(ApiResponse.success(userService.checkNameValid(dto.getName())));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getMyProfile(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(ApiResponse.success(userService.getMyProfile(user.getUsername())));
    }

    @GetMapping("/alarm")
    public ResponseEntity<ApiResponse> getMyAlarm(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(ApiResponse.success(userService.getMyAlarm(user.getUsername())));
    }

    @PatchMapping("/alarm")
    public ResponseEntity<ApiResponse> setMyAlarm(@AuthenticationPrincipal User user, MyPageDto.MyPageAlarm dto){
        userService.setMyAlarm(user.getUsername(),dto);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
