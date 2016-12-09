package com.hotpads.datarouter.setting;

import java.util.Optional;

public interface SettingFinder{

	Optional<String> getSettingValue(String name);

}
