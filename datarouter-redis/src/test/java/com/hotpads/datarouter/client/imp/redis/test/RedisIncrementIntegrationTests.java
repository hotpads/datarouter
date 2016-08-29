package com.hotpads.datarouter.client.imp.redis.test;

import java.util.ArrayList;
import java.util.List;

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

@Guice(moduleFactory = DatarouterStorageTestModuleFactory.class)
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

	@Test
	public void testGetMulti(){
		RedisDatabean bean0 = new RedisDatabean("key0", "data0");
		RedisDatabean bean1 = new RedisDatabean("key1", "data1");
		RedisDatabean bean2 = new RedisDatabean("key2", "data2");

		List<RedisDatabean> beans = new ArrayList<>();
		beans.add(bean0);
		beans.add(bean1);
		beans.add(bean2);
		redisNode.putMulti(beans, null);

		List<RedisDatabeanKey> keys = new ArrayList<>();
		keys.add(bean0.getKey());
		keys.add(bean1.getKey());
		keys.add(bean2.getKey());

		List<RedisDatabean> roundTripped = redisNode.getMulti(keys, null);

		Assert.assertEquals(beans, roundTripped);
		for(RedisDatabean bean : beans){
			deleteRecord(bean);
		}
	}

	@Test
	public void testPutMulti(){
		List<RedisDatabean> beans = new ArrayList<>();
		for(int i = 0; i < 10; i++){
			beans.add(new RedisDatabean("key " + i, "data " + i));
		}

		for(RedisDatabean bean : beans){
			Assert.assertFalse(redisNode.exists(bean.getKey(), null));
		}

		redisNode.putMulti(beans, null);

		for(RedisDatabean bean : beans){
			Assert.assertTrue(redisNode.exists(bean.getKey(), null));
		}

		for(RedisDatabean bean : beans){
			deleteRecord(bean);
		}
	}

	/** private **************************************************************/

	private void deleteRecord(RedisDatabean bean){
		redisNode.delete(bean.getKey(), null);
	}
}