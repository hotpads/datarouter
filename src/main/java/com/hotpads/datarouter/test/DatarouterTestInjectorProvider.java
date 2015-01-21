package com.hotpads.datarouter.test;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.hotpads.guice.DatarouterGuiceModule;

public class DatarouterTestInjectorProvider implements Provider<Injector>{
	protected List<Module> modules = new ArrayList<>();
	protected Injector injector;

	public DatarouterTestInjectorProvider(){
		this.modules.add(new DatarouterGuiceModule());
		this.modules.add(new DatarouterTestGuiceModule());
		this.injector = Guice.createInjector(Stage.DEVELOPMENT, modules);
	}
	
	@Override
	public Injector get(){
		return injector;
	}
}
