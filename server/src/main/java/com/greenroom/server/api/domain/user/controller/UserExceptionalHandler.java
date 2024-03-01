package com.greenroom.server.api.domain.user.controller;

import com.greenroom.server.api.domain.user.exception.UserAlreadyExist;
import com.greenroom.server.api.exception.CustomException;
import com.greenroom.server.api.exception.LoginException;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.greenroom.server.api.enums.ResponseCodeEnum.FAILED;
import static com.greenroom.server.api.enums.ResponseCodeEnum.RESULT_NOT_FOUND;

@Slf4j
@RestControllerAdvice
public class UserExceptionalHandler {

    @ExceptionHandler({
            UserAlreadyExist.class
    })
    public ApiResponse customException(CustomException e) {
        if (e.getResponseCodeEnum() != RESULT_NOT_FOUND) {
            log.warn("{} : {}", e.getClass(), e.getMessage());
            log.warn("{}", e.getStackTrace()[0]);
        }
        return ApiResponse.failed(e.getResponseCodeEnum()).message(e.getMessage());
    }
}
