package com.greenroom.server.api.domain.greenroom.controller;

import com.greenroom.server.api.domain.greenroom.dto.AdornmentRequestDto;
import com.greenroom.server.api.domain.greenroom.service.AdornmentService;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AdornmentController {

    private final AdornmentService adornmentService;
    @PutMapping("/greenrooms/{id}/adornment")
    public ResponseEntity<ApiResponse> adornGreenroom(@PathVariable(value = "id")Long greenroomId, @RequestBody AdornmentRequestDto adornmentRequestDto){
        adornmentService.adornGreenroom(greenroomId,adornmentRequestDto);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
