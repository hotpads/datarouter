package com.hotpads.job.setting.cached.imp;

import com.hotpads.job.setting.cached.CachedSetting;
import com.hotpads.setting.ClusterSettingFinder;

public class BooleanCachedSetting extends CachedSetting<Boolean>{
	public BooleanCachedSetting(ClusterSettingFinder finder, String name, Boolean defaultValue){
		super(finder, name, defaultValue);
	}
	@Override
	protected Boolean reload(){
		return finder.getBoolean(name, defaultValue);
	}
}