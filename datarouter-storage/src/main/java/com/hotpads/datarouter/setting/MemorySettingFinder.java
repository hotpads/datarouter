package com.hotpads.datarouter.setting;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import com.hotpads.util.core.Duration;
import com.hotpads.util.core.date.CronExpression;

public class MemorySettingFinder implements SettingFinder{

	//protected so subclasses can modify the settings
	protected final Map<String, Object> settings;

	public MemorySettingFinder(){
		settings = new HashMap<>();
	}

	@Override
	public Boolean getBoolean(String name, Boolean defaultValue){
		Object setting = settings.get(name);
		if(setting == null){
			return defaultValue;
		}
		return (Boolean)setting;
	}

	@Override
	public CronExpression getCronExpression(String name, String defaultValue){
		Object setting = settings.get(name);
		if(setting == null){
			try{
				return new CronExpression(defaultValue);
			}catch(ParseException e){
				throw new RuntimeException(defaultValue);
			}
		}
		return (CronExpression)setting;
	}

	@Override
	public Double getDouble(String name, Double defaultValue){
		Object setting = settings.get(name);
		if(setting == null){
			return defaultValue;
		}
		return (Double)setting;
	}

	@Override
	public Duration getDuration(String name, Duration defaultValue){
		Object setting = settings.get(name);
		if(setting == null){
			return defaultValue;
		}
		return (Duration)setting;
	}

	@Override
	public Integer getInteger(String name, Integer defaultValue){
		Object setting = settings.get(name);
		if(setting == null){
			return defaultValue;
		}
		return (Integer)setting;
	}

	@Override
	public Long getLong(String name, Long defaultValue){
		Object setting = settings.get(name);
		if(setting == null){
			return defaultValue;
		}
		return (Long)setting;
	}

	@Override
	public String getString(String name, String defaultValue){
		Object setting = settings.get(name);
		if(setting == null){
			return defaultValue;
		}
		return (String)setting;
	}

}