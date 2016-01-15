package com.hotpads.datarouter.setting;

import com.hotpads.datarouter.setting.cached.impl.Duration;
import com.hotpads.util.core.date.CronExpression;

public interface SettingFinder{

	Boolean getBoolean(String name, Boolean defaultValue);

	CronExpression getCronExpression(String name, String cronExpression);

	Double getDouble(String name, Double defaultValue);

	Duration getDuration(String name, Duration defaultValue);

	Integer getInteger(String name, Integer defaultValue);

	Long getLong(String name, Long defaultValue);

	String getString(String name, String defaultValue);

}
