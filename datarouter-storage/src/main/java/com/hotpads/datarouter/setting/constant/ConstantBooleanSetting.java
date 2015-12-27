package com.hotpads.datarouter.setting.constant;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrObjectTool;

public class ConstantBooleanSetting implements Setting<Boolean>{

	private final boolean value;

	public ConstantBooleanSetting(boolean value){
		this.value = value;
	}

	@Override
	public String getName(){
		return getClass().getSimpleName();
	}

	@Override
	public Boolean getDefaultValue(){
		return value;
	}

	@Override
	public Boolean getValue(){
		return value;
	}

	@Override
	public boolean getHasCustomValue(){
		return getValue() != null;
	}

	@Override
	public boolean getHasRedundantCustomValue(){
		return DrObjectTool.equals(getDefaultValue(), getValue());
	}
	
	@Override
	public boolean isValid(String value){
		return DrBooleanTool.isBoolean(value);
	}
	
}
