package com.hotpads.datarouter.test.sqs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.sqs.BaseSqsNode;
import com.hotpads.datarouter.client.imp.sqs.config.DatarouterSqsTestModuleFactory;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.storage.queue.GroupQueueMessage;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

@Guice(moduleFactory = DatarouterSqsTestModuleFactory.class)
@Test(singleThreaded=true)
public class SqsGroupNodeIntegrationTests{

	private static final int DATABEAN_COUNT = 15;

	private final SqsTestRouter router;
	private final SqsTestHelper sqsTestHelper;

	@Inject
	public SqsGroupNodeIntegrationTests(SqsTestRouter router){
		this.router = router;
		this.sqsTestHelper = new SqsTestHelper(router.groupTestDatabean);
	}

	@BeforeMethod
	public void setUp(){
		cleanUp();
	}

	private void cleanUp(){
		Config config = new Config().setTimeout(4, TimeUnit.SECONDS);
		for(GroupQueueMessage<TestDatabeanKey, TestDatabean> message : router.groupTestDatabean.peekUntilEmpty(config)){
			router.groupTestDatabean.ack(message.getKey(), config);
			System.out.println("removed " + message.getDatabeans());
		}
	}

	@Test
	public void testPutMultiAndPollMulti(){
		List<TestDatabean> databeans = new ArrayList<>(DATABEAN_COUNT);
		String longString = SqsTestHelper.makeStringOfByteSize(BaseSqsNode.MAX_BYTES_PER_MESSAGE / (DATABEAN_COUNT-1));
		for(int i = 0 ; i < DATABEAN_COUNT ; i++){
			databeans.add(new TestDatabean(longString, makeRandomString(), makeRandomString()));
		}
		router.groupTestDatabean.putMulti(databeans, null);
		Config config = new Config().setTimeout(5, TimeUnit.SECONDS);
		List<TestDatabean> retrievedDatabeans = router.groupTestDatabean.pollMulti(config);
		retrievedDatabeans.addAll(router.groupTestDatabean.pollMulti(config));
		Assert.assertEquals(retrievedDatabeans.size(), DATABEAN_COUNT);
	}

	@Test
	public void testPutAndPeek(){
		TestDatabean databean = new TestDatabean(makeRandomString(), makeRandomString(), makeRandomString());
		router.groupTestDatabean.put(databean, null);
		Config config = new Config().setTimeout(5, TimeUnit.SECONDS);
		GroupQueueMessage<TestDatabeanKey,TestDatabean> message = router.groupTestDatabean.peek(config);
		Assert.assertEquals(message.getDatabeans().size(), 1);
		Assert.assertEquals(databean, DrCollectionTool.getFirst(message.getDatabeans()));
		Assert.assertNull(router.groupTestDatabean.peek(null));
	}

	@Test
	public void testByteLimitMulti(){
		sqsTestHelper.testByteLimitMulti();
	}

	@Test
	public void testPeekTimeout(){
		Assert.assertNull(router.groupTestDatabean.peek(null));
		Config config = new Config().setTimeout(BaseSqsNode.MAX_TIMEOUT_SECONDS + 1, TimeUnit.SECONDS);
		Assert.assertNull(router.groupTestDatabean.peek(config));
	}

	@Test
	public void testInterruptPeek(){
		SqsTestHelper.testInterruptPeek(new Callable<Void>(){

			@Override
			public Void call(){
				Config config = new Config().setTimeoutMs(5000L);
				Assert.assertNull(router.groupTestDatabean.peek(config));
				return null;
			}

		});
	}

	private static String makeRandomString(){
		return UUID.randomUUID().toString();
	}
}
