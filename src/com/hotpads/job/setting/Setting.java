package com.hotpads.job.setting;


public interface Setting<T>{

	String getName();
	T getDefaultValue();
	T getValue();
	
	boolean getHasCustomValue();
	boolean getHasRedundantCustomValue();
}
