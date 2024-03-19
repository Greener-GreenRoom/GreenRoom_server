package com.greenroom.server.api.domain.user.exception;


import com.greenroom.server.api.enums.ResponseCodeEnum;
import com.greenroom.server.api.exception.CustomException;
import lombok.Getter;

import static com.greenroom.server.api.enums.ResponseCodeEnum.*;

@Getter
public class UserAlreadyExist extends CustomException {

    private ResponseCodeEnum responseCodeEnum = ALREADY_EXIST;
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
