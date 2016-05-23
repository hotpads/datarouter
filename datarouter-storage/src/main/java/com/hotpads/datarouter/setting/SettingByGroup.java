package com.hotpads.datarouter.setting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import com.hotpads.datarouter.util.core.DrStringTool;

public class SettingByGroup<T>{
	private final String name;
	private final Map<String, Setting<T>> settings;
	private final BiFunction<String, T, Setting<T>> settingRegistrar;
	public SettingByGroup(String name, T defaultValue, BiFunction<String, T, Setting<T>> settingRegistrar){
		this.name = name;
		this.settings = new ConcurrentHashMap<>();
		this.settingRegistrar = settingRegistrar;
		addSetting("", defaultValue);
	}

	//we can avoid this method if we can register settings on the fly
	//that is, if the user creates a cluster setting using the UI
	public void addSetting(String group, T defaultValue){
		String settingName = getGroupSettingName(group);
		settings.put(settingName, settingRegistrar.apply(settingName, defaultValue));
	}

	public Setting<T> getSetting(String group){
		return settings.getOrDefault(getGroupSettingName(group), settings.get(name));
	}

	public Setting<T> getDefaultSetting(){
		return settings.get(name);
	}

	private String getGroupSettingName(String group){
		String groupSettingPostfix = DrStringTool.notEmpty(group) ? "." + group : "";
		return name + groupSettingPostfix;
	}
}
