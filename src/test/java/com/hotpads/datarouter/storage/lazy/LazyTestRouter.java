package com.hotpads.datarouter.storage.lazy;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanKey;
import com.hotpads.datarouter.test.TestIndexedDatabeanFielder;

@Singleton
public class LazyTestRouter extends BaseDataRouter{
	
	private static final String NAME = "lazyTestRouter";

	
	public IndexedSortedMapStorage<TestDatabeanKey, TestDatabean> testDatabean;
	public LazyIndexedSortedMapStorageReader<TestDatabeanKey, TestDatabean> lazyTestDatabean;

	@Inject
	public LazyTestRouter(DataRouterContext context, NodeFactory nodeFactory){
		super(context, DRTestConstants.CONFIG_PATH, NAME);
		
		testDatabean = cast(register(nodeFactory.create(DRTestConstants.CLIENT_drTestJdbc0, TestDatabean.class,
				TestIndexedDatabeanFielder.class, this, true)));
		lazyTestDatabean = new LazyIndexedSortedMapStorageReader<TestDatabeanKey, TestDatabean>(testDatabean);
		
		registerWithContext();
	}

	@Override
	public List<ClientId> getClientIds(){
		return Collections.singletonList(new ClientId(DRTestConstants.CLIENT_drTestJdbc0, true));
	}

}
