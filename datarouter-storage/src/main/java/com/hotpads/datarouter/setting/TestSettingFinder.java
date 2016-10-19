package com.hotpads.datarouter.setting;

import java.text.ParseException;

import com.hotpads.util.core.Duration;
import com.hotpads.util.core.date.CronExpression;

public class TestSettingFinder implements SettingFinder{

	@Override
	public Boolean getBoolean(String name, Boolean defaultValue){
		return defaultValue;
	}

	@Override
	public CronExpression getCronExpression(String name, String cronExpression){
		try{
			return new CronExpression(cronExpression);
		}catch(ParseException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public Double getDouble(String name, Double defaultValue){
		return defaultValue;
	}

	@Override
	public Duration getDuration(String name, Duration defaultValue){
		return defaultValue;
	}

	@Override
	public Integer getInteger(String name, Integer defaultValue){
		return defaultValue;
	}

	@Override
	public Long getLong(String name, Long defaultValue){
		return defaultValue;
	}

	@Override
	public String getString(String name, String defaultValue){
		return defaultValue;
	}

}
