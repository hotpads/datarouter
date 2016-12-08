package com.hotpads.datarouter.setting.cached.impl;

import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.cached.CachedSetting;

public class StringCachedSetting extends CachedSetting<String>{

	public StringCachedSetting(SettingFinder finder, String name, String defaultValue){
		super(finder, name, defaultValue);
	}

	@Override
	public String parseStringValue(String stringValue){
		return stringValue;
	}

	@Override
	public boolean isValid(String value){
		return true;
	}

}