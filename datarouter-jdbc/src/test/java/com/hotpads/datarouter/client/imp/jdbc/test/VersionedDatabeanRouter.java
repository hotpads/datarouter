package com.hotpads.datarouter.client.imp.jdbc.test;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.imp.jdbc.test.TestVersionedDatabean.TestVersionedDatabeanFielder;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.TestDatabeanKey;

@Singleton
public class VersionedDatabeanRouter extends BaseRouter{

	public final MapStorage<TestDatabeanKey,TestVersionedDatabean> versionedTestDatabean;

	@Inject
	public VersionedDatabeanRouter(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterSettings datarouterSettings){
		super(datarouter, DrTestConstants.CONFIG_PATH, VersionedDatabeanRouter.class.getSimpleName(), nodeFactory,
				datarouterSettings);

		versionedTestDatabean = createAndRegister(DrTestConstants.CLIENT_drTestJdbc0, TestVersionedDatabean::new,
				TestVersionedDatabeanFielder::new);
	}

	@Override
	public List<ClientId> getClientIds(){
		return Arrays.asList(DrTestConstants.CLIENT_drTestJdbc0);
	}

}
