package com.hotpads.datarouter.client.imp.sqs.config;

import java.util.List;

import com.google.inject.Module;
import com.hotpads.datarouter.client.imp.mysql.ChildInjectorInjectionFixModule;
import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;

public class DatarouterSqsTestModuleFactory extends DatarouterStorageTestModuleFactory{

	@Override
	protected List<Module> getOverriders(){
		List<Module> overrides = super.getOverriders();
		overrides.add(new ChildInjectorInjectionFixModule());
		return overrides;
	}

}
