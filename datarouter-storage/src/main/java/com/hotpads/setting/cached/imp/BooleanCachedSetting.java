package com.hotpads.setting.cached.imp;

import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.setting.cached.CachedSetting;
import com.hotpads.setting.cluster.SettingFinder;

public class BooleanCachedSetting extends CachedSetting<Boolean>{
	
	public BooleanCachedSetting(SettingFinder finder, String name, Boolean defaultValue){
		super(finder, name, defaultValue);
	}
	
	@Override
	protected Boolean reload(){
		return finder.getBoolean(name, defaultValue);
	}
	
	@Override
	public boolean isValid(String value){
		return DrBooleanTool.isBoolean(value);
	}
}