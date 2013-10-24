package com.hotpads.setting.cached.imp;

import com.hotpads.setting.cached.CachedSetting;
import com.hotpads.setting.cluster.ClusterSettingFinder;

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