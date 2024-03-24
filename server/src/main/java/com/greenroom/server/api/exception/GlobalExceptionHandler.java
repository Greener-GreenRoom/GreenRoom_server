package com.greenroom.server.api.exception;

import com.greenroom.server.api.enums.ResponseCodeEnum;
import com.greenroom.server.api.utils.ApiResponse;
import io.jsonwebtoken.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.greenroom.server.api.enums.ResponseCodeEnum.FAILED;
import static com.greenroom.server.api.enums.ResponseCodeEnum.RESULT_NOT_FOUND;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            LoginException.class
    })
    public ApiResponse customException(CustomException e) {
        if (e.getResponseCodeEnum() != RESULT_NOT_FOUND) {
            log.warn("{} : {}", e.getClass(), e.getMessage());
            log.warn("{}", e.getStackTrace()[0]);
        }
        return ApiResponse.failed(e.getResponseCodeEnum()).message(e.getMessage());
    }

    @ExceptionHandler({
            UsernameNotFoundException.class
    })
    public ApiResponse notFoundUser(UsernameNotFoundException e) {

        log.warn("{} : {}", e.getClass(), e.getMessage());
        log.warn("{}", e.getStackTrace()[0]);

        return ApiResponse.failed(ResponseCodeEnum.RESULT_NOT_FOUND).message("해당 유저가 존재 하지 않습니다.");
    }
    @ExceptionHandler({
            HttpMediaTypeNotSupportedException.class,
            HttpMessageNotReadableException.class,
            IOException.class
    })
    public ApiResponse handlingException(Exception e) {
        log.error("{} : {}", e.getClass(), e.getMessage());
        log.error("{}", e.getStackTrace()[0]);
        return ApiResponse.failed(FAILED).message(e.getMessage());
    }
}
