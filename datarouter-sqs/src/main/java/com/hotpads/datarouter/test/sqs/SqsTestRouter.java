package com.hotpads.datarouter.test.sqs;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.QueueStorage;
import com.hotpads.datarouter.routing.BaseDatarouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanFielder;
import com.hotpads.datarouter.test.TestDatabeanKey;

@Singleton
public class SqsTestRouter extends BaseDatarouter{

	private static final String NAME = "sqsTestRouter";
	
	public QueueStorage<TestDatabeanKey,TestDatabean> testDatabean;
	
	@Inject
	public SqsTestRouter(DatarouterContext context, NodeFactory nodeFactory){
		super(context, DRTestConstants.CONFIG_PATH, NAME);
		
		testDatabean = cast(register(nodeFactory.create(DRTestConstants.CLIENT_drTestSqs, TestDatabean.class,
				TestDatabeanFielder.class, this, false)));
		
		registerWithContext();
	}

	@Override
	public List<ClientId> getClientIds(){
		return Collections.singletonList(new ClientId(DRTestConstants.CLIENT_drTestSqs, true));
	}

}
