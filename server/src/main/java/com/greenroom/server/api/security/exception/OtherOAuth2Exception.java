package com.greenroom.server.api.security.exception;

import com.greenroom.server.api.enums.ResponseCodeEnum;
import com.greenroom.server.api.exception.CustomException;
import lombok.Getter;
@Getter
public class OtherOAuth2Exception extends CustomException {

    private ResponseCodeEnum responseCodeEnum = ResponseCodeEnum.ALREADY_EXIST_OTHER_OAUTH;
    public OtherOAuth2Exception(String message) {
        super(message);
    }

    public OtherOAuth2Exception(ResponseCodeEnum code){
        super(code.getMessage());
        this.responseCodeEnum = code;
    }

    public OtherOAuth2Exception(ResponseCodeEnum code,String message){
        super(message);
        this.responseCodeEnum = code;
    }
}