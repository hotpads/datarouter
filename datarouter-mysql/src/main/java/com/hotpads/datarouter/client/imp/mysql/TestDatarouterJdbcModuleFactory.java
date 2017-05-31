package com.hotpads.datarouter.client.imp.mysql;

import java.util.List;

import com.google.inject.Module;
import com.hotpads.datarouter.client.imp.mysql.ChildInjectorInjectionFixModule;
import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;

public class TestDatarouterJdbcModuleFactory extends DatarouterStorageTestModuleFactory{

	@Override
	protected List<Module> getOverriders(){
		List<Module> overriders = super.getOverriders();
		overriders.add(new DefaultDatarouterJdbcGuiceModule());
		overriders.add(new ChildInjectorInjectionFixModule());
		return overriders;
	}

}
