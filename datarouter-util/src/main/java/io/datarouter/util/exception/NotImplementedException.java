package io.datarouter.util.exception;

@SuppressWarnings("serial")
public class NotImplementedException extends UnsupportedOperationException{

	public NotImplementedException(String message){
		super(message);
	}

	public NotImplementedException(){
	}

}
