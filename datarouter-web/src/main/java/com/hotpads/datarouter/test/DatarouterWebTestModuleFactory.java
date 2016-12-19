package com.hotpads.datarouter.test;

import java.util.Collections;
import java.util.List;

import com.google.inject.Module;
import com.hotpads.datarouter.client.imp.jdbc.ChildInjectorInjectionFixModule;
import com.hotpads.datarouter.config.DatarouterWebGuiceModule;
import com.hotpads.datarouter.test.testng.ModuleFactory;

public class DatarouterWebTestModuleFactory extends ModuleFactory{

	public DatarouterWebTestModuleFactory(){
		super(Collections.singleton(new DatarouterWebGuiceModule()));
	}

	@Override
	protected List<Module> getOverriders(){
		List<Module> modules = super.getOverriders();
		modules.add(new ChildInjectorInjectionFixModule());
		modules.add(new DatarouterStorageTestGuiceModule());
		return modules;
	}

}
