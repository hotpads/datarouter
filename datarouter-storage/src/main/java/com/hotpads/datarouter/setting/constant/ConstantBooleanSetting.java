package com.hotpads.datarouter.setting.constant;

import com.hotpads.datarouter.setting.type.BooleanSetting;

public class ConstantBooleanSetting extends ConstantSetting<Boolean> implements BooleanSetting{

	private final boolean value;

	public static final ConstantBooleanSetting FALSE = new ConstantBooleanSetting(false);
	public static final ConstantBooleanSetting TRUE = new ConstantBooleanSetting(true);

	public ConstantBooleanSetting(boolean value){
		this.value = value;
	}

	@Override
	public Boolean getValue(){
		return value;
	}

}
