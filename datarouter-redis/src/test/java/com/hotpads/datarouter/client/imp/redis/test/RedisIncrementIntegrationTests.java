package com.hotpads.datarouter.client.imp.redis.test;

import javax.inject.Inject;

import org.junit.AfterClass;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.client.imp.redis.databean.RedisDatabean;
import com.hotpads.datarouter.client.imp.redis.databean.RedisDatabean.RedisDatabeanFielder;
import com.hotpads.datarouter.client.imp.redis.databean.RedisDatabeanKey;
import com.hotpads.datarouter.client.imp.redis.node.RedisNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;
import com.hotpads.datarouter.test.DrTestConstants;

@Guice(moduleFactory=DatarouterStorageTestModuleFactory.class)
public class RedisIncrementIntegrationTests{

	/** fields ***************************************************************/

	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterClients datarouterClients;

	private RedisNode<RedisDatabeanKey,RedisDatabean,RedisDatabeanFielder> redisNode;

	/** constructors *********************************************************/

	@BeforeClass
	public void beforeClass(){
		RedisTestRouter router = new RedisTestRouter(datarouter, datarouterClients, DrTestConstants.CLIENT_drTestRedis);
		redisNode = router.redisNode();
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	/** tests ****************************************************************/

	@Test
	public void testGetTallyCountOnNull(){
		RedisDatabean bean = new RedisDatabean();
		Assert.assertEquals(redisNode.getTallyCount(bean.getKey()), null);

		RedisDatabean bean2 = new RedisDatabean("testKey1", "testData1");
		Assert.assertEquals(redisNode.getTallyCount(bean2.getKey()), null);

		deleteRecord(bean);
		deleteRecord(bean2);
	}

	/** private **************************************************************/

	private void deleteRecord(RedisDatabean bean){
		redisNode.delete(bean.getKey(), null);
	}
}