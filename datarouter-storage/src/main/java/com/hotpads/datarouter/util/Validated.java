package com.hotpads.datarouter.util;

public class Validated<T>{
	private String errorMessage;
	private T value;

	public T get(){
		return value;
	}

	public Validated(){
	}

	public Validated(T value){
		set(value);
	}

	public Validated(T value, String errorMessage){
		set(value);
		addError(errorMessage);
	}

	public void set(T value){
		this.value = value;
	}

	public Validated<T> addError(String errorMessage){
		if(errorMessage == null){
			return this;
		}
		if(this.errorMessage == null){
			this.errorMessage = errorMessage;
		}else{
			this.errorMessage += ";" + errorMessage;
		}
		return this;
	}

	public boolean isValid(){
		return errorMessage == null;
	}

	public boolean hasErrors(){
		return errorMessage != null;
	}

	public String getErrorMessage(){
		return errorMessage;
	}
}
