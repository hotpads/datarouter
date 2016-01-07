package com.hotpads.datarouter.setting.cached.impl;

import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.cached.CachedSetting;

public class DurationCachedSetting extends CachedSetting<Duration>{

	public DurationCachedSetting(SettingFinder finder, String name, Duration defaultValue){
		super(finder, name, defaultValue);
	}

	@Override
	protected Duration reload(){
		return finder.getDuration(name, defaultValue);
	}
	
	@Override
	public boolean isValid(String value){
		return Duration.isDuration(value);
	}

}
