package com.hotpads.setting.cluster;

import java.util.List;

import org.quartz.CronExpression;

public interface ClusterSettingFinder{

	Integer getInteger(String name, Integer defaultValue);
	Boolean getBoolean(String name, Boolean defaultValue);
	String getString(String name, String defaultValue);
	CronExpression getCronExpression(String name, String defaultValue);
	List<CronExpression> getAllTriggers();

}