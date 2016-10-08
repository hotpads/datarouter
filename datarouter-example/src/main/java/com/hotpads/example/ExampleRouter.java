package com.hotpads.example;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.example.Cake.CakeFielder;

@Singleton
public class ExampleRouter extends BaseRouter{

	private static final ClientId memory = new ClientId("memory", true);

	public final MapStorage<CakeKey,Cake> cake;

	@Inject
	public ExampleRouter(Datarouter datarouter, DatarouterSettings datarouterSettings, NodeFactory nodeFactory){
		super(datarouter, "/hotpads/config/datarouter-example.properties", "example", nodeFactory, datarouterSettings);
		cake = register(nodeFactory.create(memory, Cake.class, CakeFielder.class, this, false));
	}

}
