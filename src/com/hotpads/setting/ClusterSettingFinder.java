package com.hotpads.setting;

import java.util.List;

import org.quartz.CronExpression;

public interface ClusterSettingFinder{

	public abstract Integer getInteger(String name, Integer defaultValue);

	public abstract Boolean getBoolean(String name, Boolean defaultValue);

	public abstract String getString(String name, String defaultValue);

	public abstract CronExpression getCronExpression(String name, String defaultValue);

	public abstract List<CronExpression> getAllTriggers();

}