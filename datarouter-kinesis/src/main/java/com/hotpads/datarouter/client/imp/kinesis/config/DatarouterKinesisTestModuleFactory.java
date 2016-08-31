package com.hotpads.datarouter.client.imp.kinesis.config;

import java.util.List;

import com.google.inject.Module;
import com.hotpads.datarouter.client.imp.jdbc.ChildInjectorInjectionFixModule;
import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;

public class DatarouterKinesisTestModuleFactory extends DatarouterStorageTestModuleFactory{

	@Override
	protected List<Module> getOverriders(){
		List<Module> overrides = super.getOverriders();
		overrides.add(new ChildInjectorInjectionFixModule());
		return overrides;
	}

}
