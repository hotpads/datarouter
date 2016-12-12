package com.hotpads.datarouter.setting.constant;

import com.hotpads.datarouter.setting.type.IntegerSetting;

public class ConstantIntegerSetting extends ConstantSetting<Integer> implements IntegerSetting{

	private final int value;

	public ConstantIntegerSetting(int value){
		this.value = value;
	}

	@Override
	public Integer getValue(){
		return value;
	}

}
