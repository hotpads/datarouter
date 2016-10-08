package com.hotpads.datarouter.setting.constant;

import java.util.Objects;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.util.core.DrBooleanTool;

public abstract class ConstantSetting<T> implements Setting<T>{

	@Override
	public String getName(){
		return getClass().getSimpleName();
	}

	@Override
	public T getDefaultValue(){
		return getValue();
	}

	@Override
	public boolean getHasCustomValue(){
		return getValue() != null;
	}

	@Override
	public boolean getHasRedundantCustomValue(){
		return Objects.equals(getDefaultValue(), getValue());
	}

	@Override
	public boolean isValid(String value){
		return DrBooleanTool.isBoolean(value);
	}

}
