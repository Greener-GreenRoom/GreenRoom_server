package com.greenroom.server.api.domain.greenroom.controller;

import com.greenroom.server.api.domain.greenroom.dto.*;
import com.greenroom.server.api.domain.greenroom.service.GreenroomService;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class GreenRoomController {
    private final GreenroomService greenroomService;


    //그린룸 등록하기
    @PostMapping(value = "/greenrooms",consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse> registerGreenRoom(@RequestPart(value = "request") GreenroomRegistrationDto greenroomRegistrationDto,@RequestPart(value ="imgFile",required =  false) MultipartFile imgFile, @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        String userEmail = userDetails.getUsername();

        GreenroomRegisterResponseDto result = greenroomService.registerGreenRoom(greenroomRegistrationDto,userEmail,imgFile);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    //그린룸 컬렉션 조회하기
    @GetMapping("/greenrooms")
    public ResponseEntity<ApiResponse> getAllGreenroomInfo(@AuthenticationPrincipal UserDetails userDetails,@RequestParam(value = "sort",required = false) String sort, @RequestParam(value = "filter",required = false) String filter,@RequestParam(value = "offset",required = false)Integer offset){
        String userEmail = userDetails.getUsername();

        GreenroomAllResponseDto greenroomAllResponseDto = greenroomService.getAllGreenroomInfo(userEmail,sort,filter,offset);

        return ResponseEntity.ok(ApiResponse.success(greenroomAllResponseDto));
    }


    //user의 그린룸 리스트 조회하기
    @GetMapping("/greenrooms/simple")
    public ResponseEntity<ApiResponse> getGreenroomList(@AuthenticationPrincipal UserDetails userDetails,@RequestParam(value = "sort",required = false)String sort,@RequestParam(value = "offset", required = false)Integer offset){
        String userEmail = userDetails.getUsername();

        return ResponseEntity.ok(ApiResponse.success(greenroomService.getGreenroomList(userEmail,sort,offset)));

    }

    //특정 그린룸 정보 조회하기
    @GetMapping("/greenrooms/{id}")
    public ResponseEntity<ApiResponse> getSpecificGreenroomInfo(@PathVariable(value = "id")Long greenroomId){

        GreenroomResponseDto greenroomResponseDto =  greenroomService.getSpecificGreenroomInfo(greenroomId);
        return ResponseEntity.ok(ApiResponse.success(greenroomResponseDto));

    }
    //특정 그린룸 상세 정보 조회하기
    @GetMapping("/greenrooms/{id}/details")
    public ResponseEntity<ApiResponse> getGreenroomDetails(@PathVariable(value = "id") Long greenroomId){

        return ResponseEntity.ok(ApiResponse.success(greenroomService.getGreenroomDetails(greenroomId)));

    }

    //그린룸 편집하기
    @PatchMapping(value = "/greenrooms/{id}",consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse> modifyGreenroom(@PathVariable(value = "id") Long greenroomId, @RequestPart(value ="imgFile",required =  false) MultipartFile imgFile, @RequestPart(value ="request") ArrayList<PatchRequestDto> patchRequestDtos) throws IOException {

        return ResponseEntity.ok(ApiResponse.success(greenroomService.modifyGreenroom(greenroomId,imgFile, patchRequestDtos)));
    }

    //그린룸 이름 중복 확인
    @GetMapping("/greenrooms/duplicate")
    public ResponseEntity<ApiResponse> checkDuplicateName(@AuthenticationPrincipal UserDetails userDetails,@RequestParam(value = "name")String name){

        String userEmail = userDetails.getUsername();
        return ResponseEntity.ok(ApiResponse.success(greenroomService.checkDuplicateName(userEmail,name)));

    }

    //그린룸 삭제
    @DeleteMapping("/greenrooms/{id}")
    public ResponseEntity<ApiResponse> deleteGreenroom(@PathVariable(value = "id")Long greenroomId){
        greenroomService.deleteGreenroom(greenroomId);
        return ResponseEntity.ok(ApiResponse.success());
    }

}

