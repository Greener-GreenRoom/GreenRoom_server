package com.greenroom.server.api.domain.greenroom.controller;

import com.greenroom.server.api.domain.greenroom.dto.*;
import com.greenroom.server.api.domain.greenroom.service.GreenroomService;
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

@RestController
@Slf4j
@RequiredArgsConstructor
public class GreenRoomController {
    private final GreenroomService greenroomService;
    @PostMapping(value = "/greenroom",consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse> registerGreenRoom(@RequestPart(value = "request") GreenroomRegistrationDto greenroomRegistrationDto,@RequestPart(value ="imgFile",required =  false) MultipartFile imgFile, @AuthenticationPrincipal UserDetails userDetails) throws IOException,RuntimeException {

        String userEmail = userDetails.getUsername();

        GreenroomRegisterResponseDto result = greenroomService.registerGreenRoom(greenroomRegistrationDto,userEmail,imgFile);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/greenroom")
    public ResponseEntity<ApiResponse> getAllGreenroomInfo(@AuthenticationPrincipal UserDetails userDetails,@RequestParam(value = "sort",defaultValue ="desc") String sort, @RequestParam(value = "range", defaultValue ="all") String range){
        String userEmail = userDetails.getUsername();

        GreenroomResponseDtoWithUser greenroomResponseDtoWithUser = greenroomService.getAllGreenroomInfo(userEmail,sort,range);

        return ResponseEntity.ok(ApiResponse.success(greenroomResponseDtoWithUser));
    }

    @GetMapping("/greenroom-list")
    public ResponseEntity<ApiResponse> getGreenroomList(@AuthenticationPrincipal UserDetails userDetails,@RequestParam(value = "sort")String sort){
        String userEmail = userDetails.getUsername();

        ArrayList<GreenRoomListDto> greenRoomListDtos = greenroomService.getGreenroomList(userEmail,sort);

        return ResponseEntity.ok(ApiResponse.success(greenRoomListDtos));

    }

    @GetMapping("/greenroom/{id}")
    public ResponseEntity<ApiResponse> getSpecificGreenroomInfo(@PathVariable(value = "id")Long greenroomId){

        GreenroomResponseDto greenroomResponseDto =  greenroomService.getSpecificGreenroomInfo(greenroomId);
        return ResponseEntity.ok(ApiResponse.success(greenroomResponseDto));

    }
}
