package com.hotpads.datarouter.test;

import java.util.Collections;
import java.util.List;

import com.google.inject.Module;
import com.hotpads.datarouter.config.DatarouterGuiceModule;
import com.hotpads.test.ModuleFactory;

public class DatarouterTestModuleFactory extends ModuleFactory{

	public DatarouterTestModuleFactory(){
		super(Collections.singleton(new DatarouterGuiceModule()));
	}

	@Override
	protected List<Module> getOverriders(){
		List<Module> modules = super.getOverriders();
		modules.add(new DatarouterTestGuiceModule());
		return modules;
	}

}
