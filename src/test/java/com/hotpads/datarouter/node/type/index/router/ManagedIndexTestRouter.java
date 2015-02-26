package com.hotpads.datarouter.node.type.index.router;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.type.index.node.TestDatabeanWithTxnManagedIndexNode;
import com.hotpads.datarouter.node.type.index.node.TestDatabeanWithManagedIndexNode;
import com.hotpads.datarouter.routing.BaseDatarouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.util.core.ListTool;

@Singleton
public class ManagedIndexTestRouter extends BaseDatarouter{
	
	private static final String NAME = "managedIndexTest";

	private final NodeFactory nodeFactory;
	
	public final TestDatabeanWithManagedIndexNode testDatabeanWithManagedIndex;
	public final TestDatabeanWithTxnManagedIndexNode testDatabeanWithTxnManagedIndex;

	@Inject
	public ManagedIndexTestRouter(DatarouterContext context, NodeFactory nodeFactory){
		super(context, DRTestConstants.CONFIG_PATH, NAME);
		this.nodeFactory = nodeFactory;
		testDatabeanWithManagedIndex = new TestDatabeanWithManagedIndexNode(nodeFactory, this);
		testDatabeanWithTxnManagedIndex = new TestDatabeanWithTxnManagedIndexNode(nodeFactory, this);
		registerWithContext();
	}

	@Override
	public List<ClientId> getClientIds(){
		return ListTool.create(new ClientId(DRTestConstants.CLIENT_drTestJdbc0, true));
	}

}
