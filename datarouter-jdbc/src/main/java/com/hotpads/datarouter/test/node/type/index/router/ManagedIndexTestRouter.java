package com.hotpads.datarouter.test.node.type.index.router;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.type.index.node.TestDatabeanWithManagedIndexNode;
import com.hotpads.datarouter.test.node.type.index.node.TestDatabeanWithTxnManagedIndexNode;

@Singleton
public class ManagedIndexTestRouter extends BaseRouter{
	
	private static final String NAME = "managedIndexTest";
	
	public final TestDatabeanWithManagedIndexNode testDatabeanWithManagedIndex;
	public final TestDatabeanWithTxnManagedIndexNode testDatabeanWithTxnManagedIndex;

	@Inject
	public ManagedIndexTestRouter(DatarouterContext context, NodeFactory nodeFactory){
		super(context, DrTestConstants.CONFIG_PATH, NAME);
		testDatabeanWithManagedIndex = new TestDatabeanWithManagedIndexNode(nodeFactory, this);
		testDatabeanWithTxnManagedIndex = new TestDatabeanWithTxnManagedIndexNode(nodeFactory, this);
	}

	@Override
	public List<ClientId> getClientIds(){
		return Collections.singletonList(DrTestConstants.CLIENT_drTestJdbc0);
	}

}
