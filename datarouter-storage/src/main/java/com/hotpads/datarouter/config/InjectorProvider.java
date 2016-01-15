package com.hotpads.datarouter.config;

import javax.inject.Provider;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

public class InjectorProvider implements Provider<Injector>{

	private Injector injector;

	public InjectorProvider(Stage stage,Iterable<Module>  modules){
		this.injector = Guice.createInjector(stage, modules);
	}

	@Override
	public Injector get(){
		return injector;
	}

}
