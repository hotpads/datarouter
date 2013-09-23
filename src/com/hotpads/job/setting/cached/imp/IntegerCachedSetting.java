package com.hotpads.job.setting.cached.imp;

import com.hotpads.job.setting.cached.CachedSetting;
import com.hotpads.setting.ClusterSettingFinder;

public class IntegerCachedSetting extends CachedSetting<Integer>{
	public IntegerCachedSetting(ClusterSettingFinder finder, String name, Integer defaultValue){
		super(finder, name, defaultValue);
	}

	@Override
	protected Integer reload(){
		return finder.getInteger(name, defaultValue);
	}
}