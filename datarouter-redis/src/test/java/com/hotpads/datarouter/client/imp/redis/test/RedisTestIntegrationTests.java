package com.hotpads.datarouter.client.imp.redis.test;

import javax.inject.Inject;

import org.junit.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.client.imp.redis.node.RedisNode;
import com.hotpads.datarouter.client.imp.redis.test.RedisTestDatabean.RedisTestDatabeanFielder;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;
import com.hotpads.datarouter.test.DrTestConstants;

@Guice(moduleFactory=DatarouterStorageTestModuleFactory.class)
public class RedisTestIntegrationTests{

	/***************************** fields **************************************/

	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterClients datarouterClients;

	private RedisNode <RedisTestDatabeanKey,RedisTestDatabean,RedisTestDatabeanFielder> redisNode;

	/***************************** constructors **************************************/

	@BeforeClass
	public void beforeClass(){
		RedisTestRouter router = new RedisTestRouter(datarouter, datarouterClients, DrTestConstants.CLIENT_drTestRedis);
		redisNode = router.node();
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}


	/***************************** tests **************************************/

	@Test
	public void testPut(){
		RedisTestDatabean bean = new RedisTestDatabean("testkey1", "testdata1");
		redisNode.put(bean, null);
	}
}
