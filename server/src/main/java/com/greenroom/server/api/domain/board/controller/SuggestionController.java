package com.greenroom.server.api.domain.board.controller;

import com.greenroom.server.api.domain.board.dto.SuggestionRequest;
import com.greenroom.server.api.domain.board.service.SuggestionService;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/suggestion")
public class SuggestionController {


    private final SuggestionService suggestionService;

    @PostMapping(value = "",consumes = {MediaType.APPLICATION_JSON_VALUE,MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse> addSuggestion(
            @AuthenticationPrincipal User user,
            @RequestPart SuggestionRequest suggestionRequest,
            @RequestPart MultipartFile plantImageFile) throws IOException {

        suggestionService.createSuggestion(user,suggestionRequest,plantImageFile);

        return ResponseEntity.ok(ApiResponse.success());
    }
}
