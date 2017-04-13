package com.hotpads.datarouter.storage.lazy;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DatarouterTestClientIds;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanKey;
import com.hotpads.datarouter.test.TestDatarouterProperties;
import com.hotpads.datarouter.test.TestIndexedDatabeanFielder;

@Singleton
public class LazyTestRouter extends BaseRouter{

	private static final String NAME = "lazyTestRouter";

	public IndexedSortedMapStorage<TestDatabeanKey,TestDatabean> testDatabean;
	public LazyIndexedSortedMapStorageReader<TestDatabeanKey,TestDatabean> lazyTestDatabean;

	@Inject
	public LazyTestRouter(TestDatarouterProperties datarouterProperties, Datarouter datarouter,
			DatarouterSettings datarouterSettings, NodeFactory nodeFactory){
		super(datarouter, datarouterProperties.getTestRouterConfigFileLocation(), NAME, nodeFactory,
				datarouterSettings);

		testDatabean = register(nodeFactory.create(DatarouterTestClientIds.jdbc0, TestDatabean.class,
				TestIndexedDatabeanFielder.class, this, true));
		lazyTestDatabean = new LazyIndexedSortedMapStorageReader<>(testDatabean);

	}

}
