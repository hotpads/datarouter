package com.hotpads.datarouter.client.imp.jdbc;

import com.google.inject.AbstractModule;
import com.hotpads.GuiceInjector;
import com.hotpads.datarouter.node.Nodes;

public class ChildInjectorInjectionFixModule extends AbstractModule{

	@Override
	protected void configure(){
		bind(GuiceInjector.class);
		bind(Nodes.class);
	}

}
