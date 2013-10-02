package com.hotpads.setting.cached.imp;

import java.text.ParseException;

import org.quartz.CronExpression;

import com.hotpads.setting.cached.CachedSetting;
import com.hotpads.setting.cluster.ClusterSettingFinder;

public class CronExpressionCachedSetting extends CachedSetting<CronExpression>{
	public CronExpressionCachedSetting(ClusterSettingFinder finder, String name, CronExpression defaultValue)
			throws ParseException{
		super(finder, name, defaultValue);
	}

	@Override
	protected CronExpression reload(){
		if(defaultValue == null){ return finder.getCronExpression(name, null); }
		return finder.getCronExpression(name, defaultValue.getCronExpression());
	}

}