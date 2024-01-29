package com.greenroom.server.api.domain.user.exception;


import com.greenroom.server.api.enums.ResponseCodeEnum;
import lombok.Getter;

@Getter
public class UserAlreadyExist extends RuntimeException{

    private ResponseCodeEnum responseCodeEnum = null;
    public UserAlreadyExist(String message) {
        super(message);
    }

    public UserAlreadyExist(ResponseCodeEnum code){
        super(code.getMessage());
        this.responseCodeEnum = code;
    }

    public UserAlreadyExist(ResponseCodeEnum code,String message){
        super(message);
        this.responseCodeEnum = code;
    }
}
