package com.hotpads.datarouter.client.imp.jdbc;

import java.util.List;

import com.google.inject.Module;
import com.hotpads.datarouter.test.DatarouterTestModuleFactory;

public class TestDatarouterJdbcModuleFactory extends DatarouterTestModuleFactory{

	@Override
	protected List<Module> getOverriders(){
		List<Module> overriders = super.getOverriders();
		overriders.add(new DefaultDatarouterJdbcGuiceModule());
		overriders.add(new ChildInjectorInjectionFixModule());
		return overriders;
	}

}
