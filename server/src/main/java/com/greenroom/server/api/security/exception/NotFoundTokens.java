package com.greenroom.server.api.security.exception;

import com.greenroom.server.api.enums.ResponseCodeEnum;
import com.greenroom.server.api.exception.CustomException;
import lombok.Getter;

@Getter
public class NotFoundTokens extends CustomException {

    private ResponseCodeEnum responseCodeEnum = ResponseCodeEnum.TOKENS_NOT_FOUND;
    public NotFoundTokens(String message) {
        super(message);
    }

    public NotFoundTokens(ResponseCodeEnum code){
        super(code.getMessage());
        this.responseCodeEnum = code;
    }

    public NotFoundTokens(ResponseCodeEnum code,String message){
        super(message);
        this.responseCodeEnum = code;
    }
}
