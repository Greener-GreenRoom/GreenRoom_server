package com.greenroom.server.api.enums;

import lombok.Getter;


@Getter
public enum ResponseCodeEnum {

    SUCCESS(0x0000, "success"),
    FAILED(-0x0001, "failed"),
    ERROR(-0x0002,"error"),

    RESULT_NOT_FOUND(-0x1001, "not found"),
    ALREADY_EXIST(-0x2001, "already exists"),
    ALREADY_EXIST_OTHER_OAUTH(-0x2002,"already exists in other oauth2"),
    ALL_TOKEN_WERE_EXPIRED(-0x2003,"all tokens were expired"),
    TOKENS_NOT_FOUND(-0x2004,"there were no tokens"),
    FAIL_DATA_PARSE(-0x3001, "fail data parsing"),
    INVALID_CREDENTIALS(-0x4001, "invalid credentials"),
    INVALID_FORMAT(-0x5001, "invalid format"),
    ;
    private final int code;

    private final String message;

    ResponseCodeEnum(int code, String message){
        this.code = code;
        this.message = message;
    }
}