package com.hotpads.setting.cached.imp;

import com.hotpads.setting.cached.CachedSetting;
import com.hotpads.setting.cluster.SettingFinder;

public class DoubleCachedSetting extends CachedSetting<Double> {
	
	public DoubleCachedSetting(SettingFinder finder, String name, Double defaultValue){
		super(finder, name, defaultValue);
	}

	@Override
	protected Double reload(){
		return finder.getDouble(name, defaultValue);
	}
	
	//Not a great implementation but at that time, 
	//there is no simple way to check that a String is parsable to a Double
	@Override
	public boolean isValid(String value){
		try{
			Double.parseDouble(value);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}
}
