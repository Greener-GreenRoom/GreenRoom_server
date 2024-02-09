package com.greenroom.server.api.exception;

import lombok.Getter;

@Getter
public class LoginException extends RuntimeException{

	public LoginException(String message){
		super(message);
	}
}
