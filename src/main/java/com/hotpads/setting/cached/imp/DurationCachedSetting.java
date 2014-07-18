package com.hotpads.setting.cached.imp;

import com.hotpads.setting.cached.CachedSetting;
import com.hotpads.setting.cluster.ClusterSettingFinder;

public class DurationCachedSetting extends CachedSetting<Duration>{

	public DurationCachedSetting(ClusterSettingFinder finder, String name, Duration defaultValue){
		super(finder, name, defaultValue);
	}

	@Override
	protected Duration reload(){
		return finder.getDuration(name, defaultValue);
	}

}
