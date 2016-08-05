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
import com.hotpads.datarouter.client.imp.redis.node.RedisNode;
import com.hotpads.datarouter.client.imp.redis.test.databean.RedisTestDatabean;
import com.hotpads.datarouter.client.imp.redis.test.databean.RedisTestDatabean.RedisTestDatabeanFielder;
import com.hotpads.datarouter.client.imp.redis.test.databean.RedisTestDatabeanKey;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;
import com.hotpads.datarouter.test.DrTestConstants;

@Guice(moduleFactory=DatarouterStorageTestModuleFactory.class)
public class RedisTestIntegrationTests{

	/** fields ***************************************************************/

	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterClients datarouterClients;

	private RedisNode <RedisTestDatabeanKey,RedisTestDatabean,RedisTestDatabeanFielder> redisNode;

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
	public void testPut(){
		RedisTestDatabean bean = new RedisTestDatabean("testkey1", "testData1");
		redisNode.put(bean, null);

		deleteRecord(bean);
	}

	@Test
	public void testPutAndGet(){
		RedisTestDatabean bean = new RedisTestDatabean("testkey1", "testData1");
		redisNode.put(bean, null);

		RedisTestDatabean roundTripped = redisNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped, bean);

		deleteRecord(bean);
	}

	@Test
	public void testExists(){
		RedisTestDatabean bean = new RedisTestDatabean("testkey1", "testData1");
		redisNode.put(bean, null);
		Assert.assertTrue(redisNode.exists(bean.getKey(), null));

		RedisTestDatabean bean2 = new RedisTestDatabean("testkey2", "testData2");
		Assert.assertFalse(redisNode.exists(bean2.getKey(), null));

		deleteRecord(bean);
		deleteRecord(bean2);
	}

	@Test
	public void testDelete(){
		RedisTestDatabean bean = new RedisTestDatabean("testkey1", "testData1");
		redisNode.put(bean, null);
		Assert.assertTrue(redisNode.exists(bean.getKey(), null));
		redisNode.delete(bean.getKey(), null);
		Assert.assertFalse(redisNode.exists(bean.getKey(), null));
		Assert.assertNull(redisNode.get(bean.getKey(), null));

		deleteRecord(bean);
	}

	@Test
	public void testUpdate(){
		RedisTestDatabean bean = new RedisTestDatabean("testKey1", "testData1");
		redisNode.put(bean, null);
		Assert.assertTrue(redisNode.exists(bean.getKey(), null));
		RedisTestDatabean roundTripped = redisNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped, bean);
		bean.setData("testData2");
		redisNode.put(bean, null);
		RedisTestDatabean roundTripped2 = redisNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped2.getData(), bean.getData());

		deleteRecord(roundTripped2);
		deleteRecord(bean);
	}

	@Test
	public void testMulti(){
		List<RedisTestDatabean> databeans = new ArrayList<>();
		List<RedisTestDatabeanKey> keys = new ArrayList<>();
		for(int i = 0; i < 10; i++){
			RedisTestDatabean bean = new RedisTestDatabean("testkey" + i, "testData" + i);
			databeans.add(bean);
			keys.add(bean.getKey());
		}

		// putMulti
		redisNode.putMulti(databeans, null);

		for(RedisTestDatabean bean : databeans){
			Assert.assertTrue(redisNode.exists(bean.getKey(), null));
		}

		// getMulti
		List<RedisTestDatabean> roundTrippedBeans = redisNode.getMulti(keys, null);

		Assert.assertEquals(roundTrippedBeans, databeans);

		// deleteMulti
		redisNode.deleteMulti(keys, null);

		for(RedisTestDatabean bean : databeans){
			Assert.assertFalse(redisNode.exists(bean.getKey(), null));
		}

		deleteRecords(databeans);
	}

	@Test
	public void testIncrement(){
		RedisTestDatabean bean = new RedisTestDatabean("testKey1", "testData1");
		deleteRecord(bean);

		int count = 5;
		redisNode.increment(bean.getKey(), count, null);
		Assert.assertEquals(redisNode.getTallyCount(bean.getKey()), new Long(5));

		redisNode.increment(bean.getKey(), count, null);
		Assert.assertEquals(redisNode.getTallyCount(bean.getKey()), new Long(10));

		deleteRecord(bean);
	}

	@Test
	public void testGetTallyCountOnNull(){
		RedisTestDatabean bean = new RedisTestDatabean();
		Assert.assertEquals(redisNode.getTallyCount(bean.getKey()), null);

		RedisTestDatabean bean2 = new RedisTestDatabean("testKey1", "testData1");
		Assert.assertEquals(redisNode.getTallyCount(bean2.getKey()), null);

		deleteRecord(bean);
		deleteRecord(bean2);
	}

	@Test
	public void testTtl(){
		RedisTestDatabean bean = new RedisTestDatabean("testKey1", "testData1");
		redisNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));

		try{
			Thread.sleep(4 * 1000);
		} catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
		Assert.assertEquals(redisNode.getTallyCount(bean.getKey()), null);

		deleteRecord(bean);
	}

	@Test
	public void testTtlUpdate(){
		RedisTestDatabean bean = new RedisTestDatabean("testKey1", "testData1");
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
		} catch (InterruptedException e){
			Thread.currentThread().interrupt();
		}
		Assert.assertEquals(redisNode.getTallyCount(bean.getKey()), null);

		deleteRecord(bean);
	}

	@Test
	public void testTtlAdvance(){
		RedisTestDatabean bean = new RedisTestDatabean("testKey1", "testData1");

		redisNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		redisNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		redisNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		redisNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		redisNode.increment(bean.getKey(), 1, null);

		// Wait for 4 seconds
		try{
			Thread.sleep(4 * 1000);
		} catch (InterruptedException e){
			Thread.currentThread().interrupt();
		}
		Assert.assertEquals(redisNode.getTallyCount(bean.getKey()), null);

		deleteRecord(bean);
	}

	/** private **************************************************************/

	private void deleteRecord(RedisTestDatabean bean){
		redisNode.delete(bean.getKey(), null);
	}

	private void deleteRecords(List<RedisTestDatabean> beans){
		for(RedisTestDatabean bean: beans){
			deleteRecord(bean);
		}
	}
}