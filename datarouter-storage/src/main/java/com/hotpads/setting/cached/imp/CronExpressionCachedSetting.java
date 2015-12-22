package com.hotpads.setting.cached.imp;

import org.quartz.CronExpression;

import com.hotpads.setting.cached.CachedSetting;
import com.hotpads.setting.cluster.SettingFinder;

public class CronExpressionCachedSetting extends CachedSetting<CronExpression>{
	public CronExpressionCachedSetting(SettingFinder finder, String name, CronExpression defaultValue){
		super(finder, name, defaultValue);
	}

	@Override
	protected CronExpression reload(){
		if(defaultValue == null){ return finder.getCronExpression(name, null); }
		return finder.getCronExpression(name, defaultValue.getCronExpression());
	}
	
	@Override
	public boolean isValid(String value){
		return CronExpression.isValidExpression(value);
	}

}