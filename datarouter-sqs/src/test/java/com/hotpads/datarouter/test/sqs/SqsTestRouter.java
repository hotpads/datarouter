package com.hotpads.datarouter.test.sqs;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.factory.QueueNodeFactory;
import com.hotpads.datarouter.node.op.raw.GroupQueueStorage;
import com.hotpads.datarouter.node.op.raw.QueueStorage;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DatarouterTestClientIds;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanFielder;
import com.hotpads.datarouter.test.TestDatabeanKey;
import com.hotpads.datarouter.test.TestDatarouterProperties;

@Singleton
public class SqsTestRouter extends BaseRouter{

	private static final String NAME = "sqsTestRouter";

	public final QueueStorage<TestDatabeanKey,TestDatabean> testDatabean;
	public final GroupQueueStorage<TestDatabeanKey,TestDatabean> groupTestDatabean;

	@Inject
	public SqsTestRouter(TestDatarouterProperties datarouterProperties, Datarouter context,
			QueueNodeFactory queueNodeFactory, NodeFactory nodeFactory, DatarouterSettings datarouterSettings){
		super(context, datarouterProperties.getTestRouterConfigFileLocation(), NAME, nodeFactory, datarouterSettings);

		testDatabean = register(queueNodeFactory.createSingleQueueNode(DatarouterTestClientIds.CLIENT_drTestSqs, this,
				TestDatabean::new, null, TestDatabeanFielder::new, true));
		//Use a different table name to prevent test suites from interfering
		groupTestDatabean = register(queueNodeFactory.createGroupQueueNode(DatarouterTestClientIds.CLIENT_drTestSqs, this,
				TestDatabean::new, "GroupTestDatabean", TestDatabeanFielder::new, true));
	}

}
