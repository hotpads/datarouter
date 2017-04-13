package com.hotpads.datarouter.test.node.type.index.router;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.TestDatarouterProperties;
import com.hotpads.datarouter.test.node.type.index.node.TestDatabeanWithManagedIndexNode;
import com.hotpads.datarouter.test.node.type.index.node.TestDatabeanWithTxnManagedIndexNode;

public class ManagedIndexTestRouter extends BaseRouter{

	@Singleton
	public static class ManagedIndexTestRouterFactory{

		@Inject
		private TestDatarouterProperties datarouterProperties;
		@Inject
		private Datarouter datarouter;
		@Inject
		private NodeFactory nodeFactory;
		@Inject
		private DatarouterSettings datarouterSettings;

		public ManagedIndexTestRouter createWithClientId(ClientId clientId){
			return new ManagedIndexTestRouter(datarouterProperties, datarouter, datarouterSettings, nodeFactory,
					clientId);
		}
	}

	private static final String NAME = "managedIndexTest";

	public final TestDatabeanWithManagedIndexNode testDatabeanWithManagedIndex;
	public final TestDatabeanWithTxnManagedIndexNode testDatabeanWithTxnManagedIndex;

	private ManagedIndexTestRouter(TestDatarouterProperties datarouterProperties, Datarouter datarouter,
			DatarouterSettings datarouterSettings, NodeFactory nodeFactory, ClientId clientId){
		super(datarouter, datarouterProperties.getTestRouterConfigFileLocation(), NAME, nodeFactory,
				datarouterSettings);
		testDatabeanWithManagedIndex = new TestDatabeanWithManagedIndexNode(nodeFactory, this, clientId);
		testDatabeanWithTxnManagedIndex = new TestDatabeanWithTxnManagedIndexNode(nodeFactory, this, clientId);
	}

}
