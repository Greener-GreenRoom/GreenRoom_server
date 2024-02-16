package com.greenroom.server.api.domain.greenroom.controller;

import com.greenroom.server.api.domain.greenroom.dto.GreenRoomListDto;
import com.greenroom.server.api.domain.greenroom.dto.GreenroomRegistrationDto;
import com.greenroom.server.api.domain.greenroom.dto.GreenroomResponseDto;
import com.greenroom.server.api.domain.greenroom.service.GreenroomService;
import com.greenroom.server.api.enums.ResponseCodeEnum;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@Slf4j
@RequiredArgsConstructor
public class GreenRoomController {
    private final GreenroomService greenroomService;
    @PostMapping(value = "/greenroom",consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse> registerGreenRoom(@RequestPart(value = "request") GreenroomRegistrationDto greenroomRegistrationDto,@RequestPart(value ="imgFile",required =  false) MultipartFile imgFile, @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        String userEmail = userDetails.getUsername();

        Long greenroomId = greenroomService.registerGreenRoom(greenroomRegistrationDto,userEmail,imgFile);
        HashMap<String,Object> result = new HashMap<String,Object>();
        result.put("greenroom_id",greenroomId);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/greenroom")
    public ResponseEntity<ApiResponse> getAllGreenroomInfo(@AuthenticationPrincipal UserDetails userDetails){
        String userEmail = userDetails.getUsername();

        ArrayList<GreenroomResponseDto> greenroomResponseDtos = greenroomService.getAllGreenroomInfo(userEmail);

        return ResponseEntity.ok(ApiResponse.success(greenroomResponseDtos));
    }

    @GetMapping("/greenroom-list")
    public ResponseEntity<ApiResponse> getGreenroomList(@AuthenticationPrincipal UserDetails userDetails){
        String userEmail = userDetails.getUsername();

        ArrayList<GreenRoomListDto> greenRoomListDtos = greenroomService.getGreenroomList(userEmail);

        return ResponseEntity.ok(ApiResponse.success(greenRoomListDtos));

    }

    @GetMapping("/greenroom/{id}")
    public ResponseEntity<ApiResponse> getSpecificGreenroomInfo(@PathVariable(value = "id")Long greenroomId){

        GreenroomResponseDto greenroomResponseDto =  greenroomService.getSpecificGreenroomInfo(greenroomId);
        return ResponseEntity.ok(ApiResponse.success(greenroomResponseDto));

    }
}
