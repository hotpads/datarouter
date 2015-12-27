package com.hotpads.datarouter.setting.cached.impl;

import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.cached.CachedSetting;

public class StringCachedSetting extends CachedSetting<String>{

	public StringCachedSetting(SettingFinder finder, String name, String defaultValue){
		super(finder, name, defaultValue);
	}

	@Override
	protected String reload(){
		if(defaultValue == null){ return finder.getString(name, null); }
		return finder.getString(name, defaultValue);
	}
	
	@Override
	public boolean isValid(String value){
		return true;
	}

}