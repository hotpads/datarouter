package com.hotpads.datarouter.client.imp.redis.test;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.client.imp.redis.databean.RedisDatabean;
import com.hotpads.datarouter.client.imp.redis.databean.RedisDatabean.RedisDatabeanFielder;
import com.hotpads.datarouter.client.imp.redis.databean.RedisDatabeanKey;
import com.hotpads.datarouter.client.imp.redis.node.RedisNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;
import com.hotpads.datarouter.test.DrTestConstants;

// Difficult to test TTLs in maven
@Guice(moduleFactory = DatarouterStorageTestModuleFactory.class)
public class RedisTtlTester{

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
	public void testTtl(){
		RedisDatabean bean = new RedisDatabean("testKey2", "testData2");
		redisNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));

		try{
			Thread.sleep(4 * 1000);
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
		Assert.assertEquals(redisNode.getTallyCount(bean.getKey()), null);

		deleteRecord(bean);
	}

	@Test
	public void testTtlUpdate(){
		RedisDatabean bean = new RedisDatabean("testKey3", "testData3");
		deleteRecord(bean);

		// Multiple increments does not modify the original TTL
		// This bean's TTL stays at 2 seconds
		redisNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		redisNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		redisNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		redisNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		redisNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));

		// Wait for 4 seconds
		try{
			Thread.sleep(4 * 1000);
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
		Assert.assertEquals(redisNode.getTallyCount(bean.getKey()), null);

		deleteRecord(bean);
	}

	@Test
	public void testTtlAdvance(){
		RedisDatabean bean = new RedisDatabean("testKey4", "testData4");

		redisNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		redisNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		redisNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		redisNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		redisNode.increment(bean.getKey(), 1, null);

		// Wait for 4 seconds
		try{
			Thread.sleep(4 * 1000);
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
		Assert.assertEquals(redisNode.getTallyCount(bean.getKey()), null);

		deleteRecord(bean);
	}

	/** private **************************************************************/

	private void deleteRecord(RedisDatabean bean){
		redisNode.delete(bean.getKey(), null);
	}
}