package com.hotpads.setting.cluster;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.quartz.CronExpression;

import com.hotpads.setting.cached.imp.Duration;

public abstract class MemorySettingFinder implements SettingFinder{

	protected final Map<String, Object> settings;

	public MemorySettingFinder(){
		settings = new HashMap<>();
	}

	protected abstract void configureSettings();

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
