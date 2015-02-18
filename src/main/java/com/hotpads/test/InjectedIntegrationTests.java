package com.hotpads.test;

import javax.inject.Provider;

import org.junit.Before;

import com.google.inject.Injector;

public abstract class InjectedIntegrationTests{

	@Before
	public void initiateInjectedField(){
		Provider<Injector> injectorProvider = getInjectorProvider();
		Injector injector = injectorProvider.get();
		injector.injectMembers(this);
	}

	protected abstract Provider<Injector> getInjectorProvider();

}
