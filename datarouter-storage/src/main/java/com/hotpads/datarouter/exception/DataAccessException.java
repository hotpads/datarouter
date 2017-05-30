package com.hotpads.datarouter.exception;

@SuppressWarnings("serial")
public class DataAccessException extends RuntimeException{

	public DataAccessException(){
	}

	public DataAccessException(String message, Throwable cause){
		super(message, cause);
	}

	public DataAccessException(String message){
		super(message);
	}

	public DataAccessException(Throwable cause){
		super(cause);
	}

}
