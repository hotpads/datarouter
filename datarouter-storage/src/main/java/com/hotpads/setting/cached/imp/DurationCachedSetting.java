package com.hotpads.setting.cached.imp;

import com.hotpads.setting.cached.CachedSetting;
import com.hotpads.setting.cluster.SettingFinder;

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
