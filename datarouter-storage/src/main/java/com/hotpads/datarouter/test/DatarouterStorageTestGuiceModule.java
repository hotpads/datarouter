package com.hotpads.datarouter.test;

import com.google.inject.AbstractModule;
import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.config.NoOpDatarouterSettings;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.TestSettingFinder;

public class DatarouterStorageTestGuiceModule extends AbstractModule{

	@Override
	protected void configure(){
		bind(DatarouterSettings.class).to(NoOpDatarouterSettings.class);
		bind(SettingFinder.class).to(TestSettingFinder.class);
		bind(DatarouterProperties.class).to(TestDatarouterProperties.class);
	}

}
