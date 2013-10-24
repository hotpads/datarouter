package com.hotpads.setting.cached.imp;

import com.hotpads.setting.cached.CachedSetting;
import com.hotpads.setting.cluster.ClusterSettingFinder;

public class BooleanCachedSetting extends CachedSetting<Boolean>{
	public BooleanCachedSetting(ClusterSettingFinder finder, String name, Boolean defaultValue){
		super(finder, name, defaultValue);
	}
	@Override
	protected Boolean reload(){
		return finder.getBoolean(name, defaultValue);
	}
}