package com.hotpads.datarouter.setting.cached.impl;

import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.cached.CachedSetting;

public class LongCachedSetting extends CachedSetting<Long>{
	public LongCachedSetting(SettingFinder finder, String name, Long defaultValue){
		super(finder, name, defaultValue);
	}

	@Override
	protected Long reload(){
		return finder.getLong(name, defaultValue);
	}
	
	@Override
	public boolean isValid(String value){
		//Is there a better way to know if a string is parsable to Long?
		try{
			Long.parseLong(value);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}
}