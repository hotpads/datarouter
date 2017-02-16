package com.hotpads.datarouter.util;

import java.util.ArrayList;
import java.util.List;

public class Validated<T>{
	private List<String> errors = new ArrayList<>();
	private T value;

	public Validated(){
	}

	public Validated(T value){
		set(value);
	}

	public Validated(T value, String errorMessage){
		set(value);
		addError(errorMessage);
	}

	public T get(){
		return value;
	}

	public void set(T value){
		this.value = value;
	}

	public Validated<T> addError(String errorMessage){
		if(errorMessage != null && !errorMessage.isEmpty()){
			errors.add(errorMessage);
		}
		return this;
	}

	public boolean isValid(){
		return errors.isEmpty();
	}

	public boolean hasErrors(){
		return !errors.isEmpty();
	}

	public String getErrorMessage(){
		return isValid() ? null : String.join(";", errors);
	}

	public List<String> getErrors(){
		return errors;
	}
}
