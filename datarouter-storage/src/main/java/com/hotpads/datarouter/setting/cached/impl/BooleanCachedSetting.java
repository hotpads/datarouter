package com.hotpads.datarouter.setting.cached.impl;

import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.cached.CachedSetting;
import com.hotpads.datarouter.setting.type.BooleanSetting;
import com.hotpads.datarouter.util.core.DrBooleanTool;

public class BooleanCachedSetting extends CachedSetting<Boolean> implements BooleanSetting{

	public BooleanCachedSetting(SettingFinder finder, String name, Boolean defaultValue){
		super(finder, name, defaultValue);
	}

	@Override
	public boolean isValid(String value){
		return DrBooleanTool.isBoolean(value);
	}
}