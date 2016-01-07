package com.hotpads.datarouter.test;

import java.util.Collections;
import java.util.List;

import com.google.inject.Module;
import com.hotpads.datarouter.client.imp.jdbc.ChildInjectorInjectionFixModule;
import com.hotpads.datarouter.config.DatarouterStorageGuiceModule;
import com.hotpads.datarouter.test.testng.ModuleFactory;

public class DatarouterStorageTestModuleFactory extends ModuleFactory{

	public DatarouterStorageTestModuleFactory(){
		super(Collections.singleton(new DatarouterStorageGuiceModule()));
	}

	@Override
	protected List<Module> getOverriders(){
		List<Module> modules = super.getOverriders();
		modules.add(new ChildInjectorInjectionFixModule());
		modules.add(new DatarouterStorageTestGuiceModule());
		return modules;
	}

}
