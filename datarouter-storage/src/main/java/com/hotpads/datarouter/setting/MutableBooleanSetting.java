package com.hotpads.datarouter.setting;

import java.util.concurrent.atomic.AtomicBoolean;

public class MutableBooleanSetting implements Setting<Boolean>{

	private final AtomicBoolean value;

	public MutableBooleanSetting(AtomicBoolean value){
		this.value = value;
	}

	@Override
	public String getName(){
		return null;
	}

	@Override
	public Boolean getDefaultValue(){
		return false;
	}

	@Override
	public Boolean getValue(){
		return value.get();
	}

	@Override
	public boolean getHasCustomValue(){
		return false;
	}

	@Override
	public boolean getHasRedundantCustomValue(){
		return false;
	}

	@Override
	public boolean isValid(String value){
		return true;
	}

}
