package com.hotpads.setting.cached.imp;

import com.hotpads.setting.cached.CachedSetting;
import com.hotpads.setting.cluster.ClusterSettingFinder;

public class IntegerCachedSetting extends CachedSetting<Integer>{
	public IntegerCachedSetting(ClusterSettingFinder finder, String name, Integer defaultValue){
		super(finder, name, defaultValue);
	}

	@Override
	protected Integer reload(){
		return finder.getInteger(name, defaultValue);
	}
}