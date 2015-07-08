package com.hotpads.datarouter.test.sqs;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.GroupQueueStorage;
import com.hotpads.datarouter.node.op.raw.QueueStorage;
import com.hotpads.datarouter.routing.BaseDatarouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanFielder;
import com.hotpads.datarouter.test.TestDatabeanKey;

@Singleton
public class SqsTestRouter extends BaseDatarouter{

	private static final String NAME = "sqsTestRouter";
	
	public final QueueStorage<TestDatabeanKey,TestDatabean> testDatabean;
	public final GroupQueueStorage<TestDatabeanKey,TestDatabean> groupTestDatabean;
	
	@Inject
	public SqsTestRouter(DatarouterContext context, NodeFactory nodeFactory){
		super(context, DrTestConstants.CONFIG_PATH, NAME);

		testDatabean = cast(register(nodeFactory.create(DrTestConstants.CLIENT_drTestSqs, TestDatabean.class,
				TestDatabeanFielder.class, this, true)));
		//Use a different table name to avoid test suites to interfere
		groupTestDatabean = cast(register(nodeFactory.create(DrTestConstants.CLIENT_drTestSqsGroup, "groupTestDatabean",
				null, TestDatabean.class, TestDatabeanFielder.class, this, true)));
	}

	@Override
	public List<ClientId> getClientIds(){
		return Arrays.asList(DrTestConstants.CLIENT_drTestSqs, DrTestConstants.CLIENT_drTestSqsGroup);
	}

}
