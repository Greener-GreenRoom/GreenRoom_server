package com.greenroom.server.api.domain.user.exception;

import com.greenroom.server.api.enums.ResponseCodeEnum;
import com.greenroom.server.api.exception.CustomException;
import lombok.Getter;

import static com.greenroom.server.api.enums.ResponseCodeEnum.INVALID_FORMAT;
@Getter
public class InvalidNameException extends CustomException {

    private ResponseCodeEnum responseCodeEnum = INVALID_FORMAT;
    public InvalidNameException(String message) {
        super(message);
    }

    public InvalidNameException(ResponseCodeEnum code){
        super(code.getMessage());
        this.responseCodeEnum = code;
    }

    public InvalidNameException(ResponseCodeEnum code,String message){
        super(message);
        this.responseCodeEnum = code;
    }
}