package com.greenroom.server.api.domain.board.exception;

import com.greenroom.server.api.enums.ResponseCodeEnum;
import lombok.Getter;

import static com.greenroom.server.api.enums.ResponseCodeEnum.ALREADY_EXIST;

@Getter
public class AlreadyExistPlant extends RuntimeException{

    private ResponseCodeEnum responseCodeEnum = ALREADY_EXIST;
    public AlreadyExistPlant(String message) {
        super(message);
    }

    public AlreadyExistPlant(ResponseCodeEnum code){
        super(code.getMessage());
        this.responseCodeEnum = code;
    }

    public AlreadyExistPlant(ResponseCodeEnum code,String message){
        super(message);
        this.responseCodeEnum = code;
    }
}
