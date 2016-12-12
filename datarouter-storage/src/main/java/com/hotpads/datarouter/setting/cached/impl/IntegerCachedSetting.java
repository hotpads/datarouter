package com.hotpads.datarouter.setting.cached.impl;

import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.cached.CachedSetting;
import com.hotpads.datarouter.setting.type.IntegerSetting;

public class IntegerCachedSetting extends CachedSetting<Integer> implements IntegerSetting{

	public IntegerCachedSetting(SettingFinder finder, String name, Integer defaultValue){
		super(finder, name, defaultValue);
	}

	@Override
	public boolean isValid(String value){
		//Is there a better way to know if a string is parsable to Integer?
		try{
			Integer.parseInt(value);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}
}