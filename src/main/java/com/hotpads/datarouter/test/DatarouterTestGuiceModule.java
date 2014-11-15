package com.hotpads.datarouter.test;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.hotpads.setting.DatarouterSettings;

public class DatarouterTestGuiceModule extends AbstractModule{

	@Override
	protected void configure(){
		bind(DatarouterSettings.class).toProvider(NullDatarouterSettingsProvider.class);
	}


	public static class NullDatarouterSettingsProvider implements Provider<DatarouterSettings>{
		@Override
		public DatarouterSettings get(){
			return null;
		}
	}

}
