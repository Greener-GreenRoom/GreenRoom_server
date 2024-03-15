package com.greenroom.server.api.domain.board.exception;

import com.greenroom.server.api.enums.ResponseCodeEnum;
import lombok.Getter;

import static com.greenroom.server.api.enums.ResponseCodeEnum.ALREADY_EXIST;

@Getter
public class AlreadyExistSuggestion extends RuntimeException{

    private ResponseCodeEnum responseCodeEnum = ALREADY_EXIST;
    public AlreadyExistSuggestion(String message) {
        super(message);
    }

    public AlreadyExistSuggestion(ResponseCodeEnum code){
        super(code.getMessage());
        this.responseCodeEnum = code;
    }

    public AlreadyExistSuggestion(ResponseCodeEnum code, String message){
        super(message);
        this.responseCodeEnum = code;
    }
}
