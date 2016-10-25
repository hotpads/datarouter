package com.hotpads.datarouter.setting.constant;

public class ConstantBooleanSetting extends ConstantSetting<Boolean>{

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
