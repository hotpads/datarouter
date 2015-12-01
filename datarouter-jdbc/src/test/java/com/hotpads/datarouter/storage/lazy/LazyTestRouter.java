package com.hotpads.datarouter.storage.lazy;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanKey;
import com.hotpads.datarouter.test.TestIndexedDatabeanFielder;

@Singleton
public class LazyTestRouter extends BaseRouter{

	private static final String NAME = "lazyTestRouter";

	public IndexedSortedMapStorage<TestDatabeanKey, TestDatabean> testDatabean;
	public LazyIndexedSortedMapStorageReader<TestDatabeanKey, TestDatabean> lazyTestDatabean;

	@Inject
	public LazyTestRouter(Datarouter context, NodeFactory nodeFactory){
		super(context, DrTestConstants.CONFIG_PATH, NAME);

		testDatabean = register(nodeFactory.create(DrTestConstants.CLIENT_drTestJdbc0, TestDatabean.class,
				TestIndexedDatabeanFielder.class, this, true));
		lazyTestDatabean = new LazyIndexedSortedMapStorageReader<>(testDatabean);

	}

	@Override
	public List<ClientId> getClientIds(){
		return Collections.singletonList(DrTestConstants.CLIENT_drTestJdbc0);
	}

}
