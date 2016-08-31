package com.hotpads.datarouter.storage.databean;

@SuppressWarnings("serial")
public class DatabeanVersioningException extends RuntimeException{

	public DatabeanVersioningException(){

	}

	public DatabeanVersioningException(Throwable throwable){
		super(throwable);
	}

}
