package com.hotpads.job.config.setting.job;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.job.setting.ClusterSettingFinder;
import com.hotpads.job.setting.Setting;
import com.hotpads.job.setting.SettingNode;
import com.hotpads.job.setting.cached.imp.BooleanCachedSetting;

@Singleton
public class PropertySettings extends SettingNode{
	
	protected Setting<Boolean> deactivateExpiredProperties = new BooleanCachedSetting(finder, getName()
			+"deactivateExpiredProperties", false);
	
	@Inject
	public PropertySettings(ClusterSettingFinder finder) {
		super(finder, "job.property.", "job.");
	}
	
	public Setting<Boolean> getDeactivateExpiredProperties() {
		return deactivateExpiredProperties;
	}

}
