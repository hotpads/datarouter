package com.hotpads.job;

@SuppressWarnings("serial")
public class JobInterruptedException extends RuntimeException{

	public JobInterruptedException(){
	}

	public JobInterruptedException(String msg){
		super(msg);
	}

	public JobInterruptedException(Exception ex){
		super(ex);
	}

	public JobInterruptedException(String cause, Exception ex){
		super(cause, ex);
	}

}