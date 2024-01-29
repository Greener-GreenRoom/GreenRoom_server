package com.greenroom.server.api.enums;

import lombok.Getter;


@Getter
public enum ResponseCodeEnum {

	SUCCESS(0x0000, "success"),
	FAILED(-0x0001, "failed"),

	RESULT_NOT_FOUND(-0x1001, "not found"),
	ALREADY_EXIST(-0x2001, "already exists")
	;
	private final int code;

	private final String message;

	ResponseCodeEnum(int code, String message){
		this.code = code;
		this.message = message;
	}
}