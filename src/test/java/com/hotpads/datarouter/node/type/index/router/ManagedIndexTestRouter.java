package com.hotpads.datarouter.node.type.index.router;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.type.index.node.TestDatabeanWithTxnManagedIndexNode;
import com.hotpads.datarouter.node.type.index.node.TestDatabeanWithManagedIndexNode;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.util.core.ListTool;

@Singleton
public class ManagedIndexTestRouter extends BaseDataRouter{
	
	private static final String NAME = "managedIndexTest";
	
	public final TestDatabeanWithManagedIndexNode testDatabeanWithManagedIndex;
	public final TestDatabeanWithTxnManagedIndexNode testDatabeanWithTxnManagedIndex;

	@Inject
	public ManagedIndexTestRouter(DataRouterContext context){
		super(context, NAME);
		testDatabeanWithManagedIndex = new TestDatabeanWithManagedIndexNode(this);
		testDatabeanWithTxnManagedIndex = new TestDatabeanWithTxnManagedIndexNode(this);
		registerWithContext();
	}

	@Override
	public String getConfigLocation(){
		return DRTestConstants.CONFIG_PATH;
	}

	@Override
	public List<ClientId> getClientIds(){
		return ListTool.create(new ClientId(DRTestConstants.CLIENT_drTestJdbc0, true));
	}

}
