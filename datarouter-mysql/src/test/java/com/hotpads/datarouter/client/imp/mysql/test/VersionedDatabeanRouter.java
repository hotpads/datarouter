package com.hotpads.datarouter.client.imp.mysql.test;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.imp.mysql.test.TestVersionedDatabean.TestVersionedDatabeanFielder;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DatarouterTestClientIds;
import com.hotpads.datarouter.test.TestDatabeanKey;
import com.hotpads.datarouter.test.TestDatarouterProperties;

@Singleton
public class VersionedDatabeanRouter extends BaseRouter{

	public final MapStorage<TestDatabeanKey,TestVersionedDatabean> versionedTestDatabean;

	@Inject
	public VersionedDatabeanRouter(TestDatarouterProperties datarouterProperties, Datarouter datarouter,
			NodeFactory nodeFactory, DatarouterSettings datarouterSettings){
		super(datarouter, datarouterProperties.getDatarouterTestFileLocation(), VersionedDatabeanRouter.class
				.getSimpleName(), nodeFactory, datarouterSettings);

		versionedTestDatabean = createAndRegister(DatarouterTestClientIds.jdbc0, TestVersionedDatabean::new,
				TestVersionedDatabeanFielder::new);
	}

}