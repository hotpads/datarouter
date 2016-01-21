package com.hotpads.datarouter.test.node.type.index.router;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.type.index.node.TestDatabeanWithManagedIndexNode;
import com.hotpads.datarouter.test.node.type.index.node.TestDatabeanWithTxnManagedIndexNode;

public class ManagedIndexTestRouter extends BaseRouter{

	@Singleton
	public static class ManagedIndexTestRouterFactory{

		private final Datarouter datarouter;
		private final NodeFactory nodeFactory;

		@Inject
		public ManagedIndexTestRouterFactory(Datarouter context, NodeFactory nodeFactory){
			this.datarouter = context;
			this.nodeFactory = nodeFactory;
		}

		public ManagedIndexTestRouter createWithClientId(ClientId clientId){
			return new ManagedIndexTestRouter(datarouter, nodeFactory, clientId);
		}
	}

	private static final String NAME = "managedIndexTest";

	public final TestDatabeanWithManagedIndexNode testDatabeanWithManagedIndex;
	public final TestDatabeanWithTxnManagedIndexNode testDatabeanWithTxnManagedIndex;

	private ManagedIndexTestRouter(Datarouter context, NodeFactory nodeFactory, ClientId clientId){
		super(context, DrTestConstants.CONFIG_PATH, NAME);
		testDatabeanWithManagedIndex = new TestDatabeanWithManagedIndexNode(nodeFactory, this, clientId);
		testDatabeanWithTxnManagedIndex = new TestDatabeanWithTxnManagedIndexNode(nodeFactory, this, clientId);
	}

	@Override
	public List<ClientId> getClientIds(){
		return Collections.emptyList();
	}

}
