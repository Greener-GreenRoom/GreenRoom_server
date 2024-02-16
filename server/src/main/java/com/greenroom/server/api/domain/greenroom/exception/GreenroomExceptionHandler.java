package com.greenroom.server.api.domain.greenroom.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greenroom.server.api.enums.ResponseCodeEnum;
import com.greenroom.server.api.exception.CustomException;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

import static com.greenroom.server.api.enums.ResponseCodeEnum.FAILED;
import static com.greenroom.server.api.enums.ResponseCodeEnum.FAIL_DATA_PARSE;

@Slf4j
@RestControllerAdvice
public class GreenroomExceptionHandler {

    @ExceptionHandler({RuntimeException.class, UsernameNotFoundException.class, IOException.class})
    public ApiResponse RuntimeException(Exception e) {
        log.error("{} : {}", e.getClass(), e.getMessage());
        log.error("{}", e.getStackTrace()[0]);
        return ApiResponse.failed(FAILED).message(e.getMessage());
    }
}
