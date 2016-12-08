package com.hotpads.datarouter.setting.cached.impl;

import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.cached.CachedSetting;
import com.hotpads.util.core.Duration;

public class DurationCachedSetting extends CachedSetting<Duration>{

	public DurationCachedSetting(SettingFinder finder, String name, Duration defaultValue){
		super(finder, name, defaultValue);
	}

	@Override
	protected Duration parseStringValue(String stringValue){
		return new Duration(stringValue);
	}

	@Override
	public boolean isValid(String value){
		return Duration.isDuration(value);
	}

}
