package com.hotpads.datarouter.inject.guice;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Injector;
import com.hotpads.datarouter.inject.DatarouterInjector;

@Singleton
public class GuiceInjector implements DatarouterInjector{

	private final Injector injector;

	@Inject
	public GuiceInjector(Injector injector){
		this.injector = injector;
	}

	@Override
	public <T>T getInstance(Class<? extends T> clazz){
		return injector.getInstance(clazz);
	}

	@Override
	public <T> Collection<T> getInstancesOfType(Class<T> type){
		return GuiceTool.getInstancesOfType(injector, type);
	}

	@Override
	public void injectMenbers(Object instance){
		injector.injectMembers(instance);
	}

}
