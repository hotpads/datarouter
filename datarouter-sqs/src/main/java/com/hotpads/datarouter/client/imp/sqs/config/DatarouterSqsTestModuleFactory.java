package com.hotpads.datarouter.client.imp.sqs.config;

import java.util.List;

import com.google.inject.Module;
import com.hotpads.datarouter.test.DatarouterTestModuleFactory;

public class DatarouterSqsTestModuleFactory extends DatarouterTestModuleFactory{

	@Override
	protected List<Module> getOverriders(){
		List<Module> overrides = super.getOverriders();
		overrides.add(new SqsGuiceModule());
		return overrides;
	}
	
}
