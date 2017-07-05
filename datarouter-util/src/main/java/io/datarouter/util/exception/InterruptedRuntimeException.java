package io.datarouter.util.exception;

@SuppressWarnings("serial")
public class InterruptedRuntimeException extends RuntimeException{

	public InterruptedRuntimeException(Throwable cause){
		super(cause);
	}

}
