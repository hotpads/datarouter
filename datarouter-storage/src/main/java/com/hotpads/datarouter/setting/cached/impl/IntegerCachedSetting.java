package com.hotpads.datarouter.setting.cached.impl;

import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.cached.CachedSetting;

public class IntegerCachedSetting extends CachedSetting<Integer>{
	public IntegerCachedSetting(SettingFinder finder, String name, Integer defaultValue){
		super(finder, name, defaultValue);
	}

	@Override
	protected Integer reload(){
		return finder.getInteger(name, defaultValue);
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