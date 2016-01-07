package com.hotpads.datarouter.client.imp.jdbc;

import com.google.inject.AbstractModule;
import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.inject.guice.GuiceInjector;
import com.hotpads.datarouter.node.DatarouterNodes;

public class ChildInjectorInjectionFixModule extends AbstractModule{

	@Override
	protected void configure(){
		bind(GuiceInjector.class);
		bind(DatarouterNodes.class);
		bind(DatarouterClients.class);
	}

}
