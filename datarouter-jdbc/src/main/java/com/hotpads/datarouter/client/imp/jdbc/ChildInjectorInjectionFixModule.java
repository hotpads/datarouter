package com.hotpads.datarouter.client.imp.jdbc;

import com.google.inject.AbstractModule;
import com.hotpads.GuiceInjector;

public class ChildInjectorInjectionFixModule extends AbstractModule{

	@Override
	protected void configure(){
		bind(GuiceInjector.class);
	}

}
