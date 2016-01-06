package com.hotpads.util.core.exception;

@SuppressWarnings("serial")
public class IncorrectPasswordException extends InvalidCredentialsException {

	public IncorrectPasswordException(String message){
		super(message);
	}
	
}