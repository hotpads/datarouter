package com.hotpads.datarouter.exception;

@SuppressWarnings("serial")
public class DuplicateEntrySqlException extends RuntimeException{

	public DuplicateEntrySqlException(Throwable cause){
		super(cause);
	}
	
}
