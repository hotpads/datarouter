package com.hotpads.setting;


public interface Setting<T>{

	String getName();
	T getDefaultValue();
	T getValue();
	
	boolean getHasCustomValue();
	boolean getHasRedundantCustomValue();
	boolean isValid(String value);

}
