package com.hotpads.job.setting.cached.imp;

import com.hotpads.job.setting.ClusterSettingFinder;
import com.hotpads.job.setting.cached.CachedSetting;

public class StringCachedSetting extends CachedSetting<String>{

	public StringCachedSetting(ClusterSettingFinder finder, String name, String defaultValue){
		super(finder, name, defaultValue);
	}

	@Override
	protected String reload(){
		if(defaultValue == null){ return finder.getString(name, null); }
		return finder.getString(name, defaultValue);
	}

}