package com.greenroom.server.api.security.controller;

import com.greenroom.server.api.enums.ResponseCodeEnum;
import com.greenroom.server.api.exception.CustomException;
import com.greenroom.server.api.security.exception.AllTokenExpiredException;
import com.greenroom.server.api.security.exception.NotFoundTokens;
import com.greenroom.server.api.security.exception.OtherOAuth2Exception;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler({
            BadCredentialsException.class
    })
    public ApiResponse invalidCredential(Exception e) {

        log.warn("{} : {}", e.getClass(), e.getMessage());
        log.warn("{}", e.getStackTrace()[0]);

        return ApiResponse.failed(ResponseCodeEnum.INVALID_CREDENTIALS);
    }

    @ExceptionHandler({
            OtherOAuth2Exception.class,
            AllTokenExpiredException.class,
            NotFoundTokens.class
    })
    public ApiResponse authExceptionResponse(CustomException e) {
        return ApiResponse.failed(e.getResponseCodeEnum()).message(e.getMessage());
    }
}
