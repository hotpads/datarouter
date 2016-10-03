package com.hotpads.datarouter.setting.constant;

public class ConstantIntegerSetting extends ConstantSetting<Integer>{

	private final int value;

	public ConstantIntegerSetting(int value){
		this.value = value;
	}

	@Override
	public Integer getValue(){
		return value;
	}

}
