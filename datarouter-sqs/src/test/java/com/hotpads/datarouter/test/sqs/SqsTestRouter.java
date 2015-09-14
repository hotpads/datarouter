package com.hotpads.datarouter.test.sqs;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.QueueNodeFactory;
import com.hotpads.datarouter.node.op.raw.GroupQueueStorage;
import com.hotpads.datarouter.node.op.raw.QueueStorage;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanFielder;
import com.hotpads.datarouter.test.TestDatabeanKey;

@Singleton
public class SqsTestRouter extends BaseRouter{

	private static final String NAME = "sqsTestRouter";
	
	public final QueueStorage<TestDatabeanKey,TestDatabean> testDatabean;
	public final GroupQueueStorage<TestDatabeanKey,TestDatabean> groupTestDatabean;
	
	@Inject
	public SqsTestRouter(Datarouter context, QueueNodeFactory nodeFactory){
		super(context, DrTestConstants.CONFIG_PATH, NAME);

		testDatabean = cast(register(nodeFactory.createSingleQueueNode(DrTestConstants.CLIENT_drTestSqs, this,
				TestDatabean.class, null, TestDatabeanFielder.class, true)));
		//Use a different table name to prevent test suites from interfering
		groupTestDatabean = cast(register(nodeFactory.createGroupQueueNode(DrTestConstants.CLIENT_drTestSqs, this,
				TestDatabean.class, "groupTestDatabean", TestDatabeanFielder.class, true)));
	}

	@Override
	public List<ClientId> getClientIds(){
		return Arrays.asList(DrTestConstants.CLIENT_drTestSqs);
	}
}
