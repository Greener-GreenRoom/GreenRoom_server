package com.greenroom.server.api.domain.greenroom.controller;

import com.greenroom.server.api.domain.greenroom.dto.DiaryPostRequestDto;
import com.greenroom.server.api.domain.greenroom.service.DiaryService;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping("/diary")
    public ResponseEntity<ApiResponse> postDiary(@RequestPart(value = "request")DiaryPostRequestDto diaryPostRequestDto,@RequestPart(value = "imgFile",required = false)MultipartFile imgFile) throws IOException {

        diaryService.postDiary(diaryPostRequestDto,imgFile);
        return ResponseEntity.ok(ApiResponse.success());

    }

    @DeleteMapping("/diary/{id}")
    public ResponseEntity<ApiResponse> deleteDiary(@PathVariable(value = "id") Long diaryId){
        diaryService.deleteDiary(diaryId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/diary/{year}/{month}")
    public ResponseEntity<ApiResponse> getDiary(@AuthenticationPrincipal UserDetails userDetails, @PathVariable(value = "year")Integer year, @PathVariable(value ="month")Integer month, @RequestParam(value = "target",required = false)Long greenroomId,@RequestParam(value = "sort",required = false)String sort){
        return ResponseEntity.ok(ApiResponse.success(diaryService.getDiaryList(userDetails.getUsername(),year,month,greenroomId,sort)));
    }
}
