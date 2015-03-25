package com.hotpads.datarouter.test;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.hotpads.datarouter.config.DatarouterGuiceModule;

/**
 * @deprecated use DatarouterTestModuleFactory instead
 * @author cguillaume
 */
@Deprecated
public class TestDatarouterInjectorProvider implements Provider<Injector>{

	private Injector injector;

	public TestDatarouterInjectorProvider(){
		List<Module> modules = new ArrayList<>();
		modules.add(new DatarouterGuiceModule());
		modules.add(new DatarouterTestGuiceModule());
		this.injector = Guice.createInjector(Stage.DEVELOPMENT, modules);
	}

	@Override
	public Injector get(){
		return injector;
	}

}
