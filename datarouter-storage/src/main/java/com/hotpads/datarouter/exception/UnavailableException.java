package com.hotpads.datarouter.exception;

public class UnavailableException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public UnavailableException() {
		super();
	}

	public UnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnavailableException(String message) {
		super(message);
	}

	public UnavailableException(Throwable cause) {
		super(cause);
	}

}
