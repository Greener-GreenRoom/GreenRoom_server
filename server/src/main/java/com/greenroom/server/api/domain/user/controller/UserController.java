package com.greenroom.server.api.domain.user.controller;

import com.greenroom.server.api.domain.user.dto.UserDto;
import com.greenroom.server.api.domain.user.dto.UserWithdrawalRequestDto;
import com.greenroom.server.api.security.service.CustomUserDetailService;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
    public ResponseEntity<ApiResponse> withdrawalUser(@AuthenticationPrincipal User user){
        userService.deleteUser(user.getUsername());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * TODO
     * admin 전용 메서드로 변경 예정
     */
    @DeleteMapping("/delete/pending")
    public ResponseEntity<ApiResponse> deleteAllUserInPending(){
        int deleteCount = userService.deleteAllUserInDeletePending();
        return ResponseEntity.ok(ApiResponse.success(deleteCount));
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse> printMyInfo(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(ApiResponse.success(user.getUsername()));
    }

    @PatchMapping(value = "",consumes = {MediaType.APPLICATION_JSON_VALUE,MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse> updateUserInfo(
            @AuthenticationPrincipal User user,
            @RequestPart UserDto.UpdateUserRequest userDto,
            @RequestPart(required = false) MultipartFile imageFile) throws IOException {

        return ResponseEntity.ok(
                ApiResponse.success(UserDto.toDto(userService.updateUser(userDto, user.getUsername(),imageFile)))
        );
    }

    @GetMapping("/grade/level")
    public ResponseEntity<ApiResponse> getUserLevel(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(ApiResponse.success(userService.getUserLevel(userDetails.getUsername())));
    }

    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse> checkAttendance(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(ApiResponse.success(userService.checkAttendance(userDetails.getUsername())));
    }
}