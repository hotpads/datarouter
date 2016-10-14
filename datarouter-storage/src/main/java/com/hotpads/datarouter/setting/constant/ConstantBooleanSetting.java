package com.hotpads.datarouter.setting.constant;

public class ConstantBooleanSetting extends ConstantSetting<Boolean>{

	private final boolean value;

	public ConstantBooleanSetting(boolean value){
		this.value = value;
	}

	@Override
	public Boolean getValue(){
		return value;
	}

}
