package com.hotpads.datarouter.setting.cached.impl;

import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.cached.CachedSetting;
import com.hotpads.datarouter.setting.type.StringSetting;

public class StringCachedSetting extends CachedSetting<String> implements StringSetting{

	public StringCachedSetting(SettingFinder finder, String name, String defaultValue){
		super(finder, name, defaultValue);
	}

	@Override
	public boolean isValid(String value){
		return true;
	}

}