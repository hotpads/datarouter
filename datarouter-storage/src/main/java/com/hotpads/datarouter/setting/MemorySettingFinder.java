package com.hotpads.datarouter.setting;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MemorySettingFinder implements SettingFinder{

	//protected so subclasses can modify the settings
	protected final Map<String, Object> settings;

	public MemorySettingFinder(){
		settings = new HashMap<>();
	}

	@Override
	public Optional<String> getSettingValue(String name){
		return Optional.ofNullable(settings.get(name)).map(Object::toString);
	}

}
