package com.hotpads.datarouter.setting.cached.impl;

import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.cached.CachedSetting;
import com.hotpads.datarouter.util.core.DrBooleanTool;

public class BooleanCachedSetting extends CachedSetting<Boolean>{

	public BooleanCachedSetting(SettingFinder finder, String name, Boolean defaultValue){
		super(finder, name, defaultValue);
	}

	@Override
	protected Boolean parseStringValue(String stringValue){
		return DrBooleanTool.isTrue(stringValue);
	}

	@Override
	public boolean isValid(String value){
		return DrBooleanTool.isBoolean(value);
	}
}