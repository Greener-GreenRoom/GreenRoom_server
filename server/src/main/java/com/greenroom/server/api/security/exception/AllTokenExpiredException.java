package com.greenroom.server.api.security.exception;

import com.greenroom.server.api.enums.ResponseCodeEnum;
import com.greenroom.server.api.exception.CustomException;
import lombok.Getter;
@Getter
public class AllTokenExpiredException extends CustomException {

    private ResponseCodeEnum responseCodeEnum = ResponseCodeEnum.ALL_TOKEN_WERE_EXPIRED;
    public AllTokenExpiredException(String message) {
        super(message);
    }

    public AllTokenExpiredException(ResponseCodeEnum code){
        super(code.getMessage());
        this.responseCodeEnum = code;
    }

    public AllTokenExpiredException(ResponseCodeEnum code,String message){
        super(message);
        this.responseCodeEnum = code;
    }
}
