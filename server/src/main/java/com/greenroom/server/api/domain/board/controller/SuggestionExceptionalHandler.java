package com.greenroom.server.api.domain.board.controller;

import com.greenroom.server.api.domain.board.exception.AlreadyExistPlant;
import com.greenroom.server.api.domain.board.exception.AlreadyExistSuggestion;
import com.greenroom.server.api.domain.user.exception.UserAlreadyExist;
import com.greenroom.server.api.exception.CustomException;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class SuggestionExceptionalHandler {

    @ExceptionHandler({
            AlreadyExistPlant.class,
            AlreadyExistSuggestion.class
    })
    public ApiResponse customException(CustomException e) {
        return ApiResponse.failed(e.getResponseCodeEnum()).message(e.getMessage());
    }
}
