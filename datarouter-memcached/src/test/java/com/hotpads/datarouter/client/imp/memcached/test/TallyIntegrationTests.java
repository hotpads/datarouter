
package com.hotpads.datarouter.client.imp.memcached.test;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.memcached.node.MemcachedNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.profile.tally.Tally;
import com.hotpads.datarouter.profile.tally.Tally.TallyFielder;
import com.hotpads.datarouter.profile.tally.TallyKey;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;

@Guice(moduleFactory = DatarouterStorageTestModuleFactory.class)
public class TallyIntegrationTests{

	/***************************** fields **************************************/

	@Inject
	private Datarouter datarouter;
	@Inject
	private TallyTestRouter router;

	private MemcachedNode<TallyKey, Tally, TallyFielder> tallyNode;

	/***************************** constructors **************************************/

	@BeforeClass
	public void beforeClass(){
		tallyNode = router.tally();
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}


	/***************************** tests **************************************/

	// Runs on local, but throws error on buildserver
	//@Test
	public void testIncrement(){
		Tally bean = new Tally("testKey1");
		tallyNode.put(bean, null);

		int count = 5;
		tallyNode.increment(bean.getKey(), count, null);
		Assert.assertEquals(tallyNode.getTallyCount(bean.getKey(), null), null);

		count += 100;
		tallyNode.increment(bean.getKey(), count, null);
		Assert.assertEquals(tallyNode.getTallyCount(bean.getKey(), null), null);

		deleteRecord(bean.getKey());
	}


	@Test(expectedExceptions = NullPointerException.class)
	public void testDelete(){
		Tally bean = new Tally("testKey2");
		tallyNode.put(bean, null);

		tallyNode.delete(bean.getKey(), null);
		Tally roundTripped = tallyNode.get(bean.getKey(), null);
		Assert.assertNull(roundTripped);

		// Throws NullPointerException since databean has been deleted from Memcached
		tallyNode.increment(roundTripped.getKey(), 10, null);

		deleteRecord(bean.getKey());
	}

	@Test
	public void testIncrementWihoutPut(){
		Tally bean = new Tally("testKey3");
		deleteRecord(bean.getKey());

		tallyNode.increment(bean.getKey(), 5, null);

		// if assert error occurs, delete key then rerun test
		Assert.assertEquals(tallyNode.getTallyCount(bean.getKey(), null), new Long(5));
		Assert.assertEquals(tallyNode.getTallyCount(bean.getKey(), null), new Long(5));
		tallyNode.increment(bean.getKey(), 5, null);
		Assert.assertEquals(tallyNode.getTallyCount(bean.getKey(), null), new Long(10));

		deleteRecord(bean.getKey());
	}

	@Test
	public void testGetTallyCountOnNull(){
		Tally bean = new Tally();
		Assert.assertEquals(tallyNode.getTallyCount(bean.getKey(), null), null);

		deleteRecord(bean.getKey());
	}


	@Test
	public void testTtl(){
		Tally bean = new Tally("testKey4");
		deleteRecord(bean.getKey());

		tallyNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));

		// Wait for 4 seconds
		try{
			Thread.sleep(4 * 1000);
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
		Assert.assertEquals(tallyNode.getTallyCount(bean.getKey(), null), null);
	}


	@Test
	public void testTtlUpdate(){
		Tally bean = new Tally("testKey5");
		deleteRecord(bean.getKey());

		// Multiple increments does not modify the original TTL
		// This bean's TTL stays at 2 seconds
		tallyNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		tallyNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		tallyNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		tallyNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		tallyNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));

		// Wait for 4 seconds
		try{
			Thread.sleep(4 * 1000);
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
		Assert.assertEquals(tallyNode.getTallyCount(bean.getKey(), null), null);
	}

	@Test
	public void testTtlAdvance(){
		Tally bean = new Tally("testKey6");
		deleteRecord(bean.getKey());

		tallyNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		tallyNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		tallyNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		tallyNode.increment(bean.getKey(), 1, new Config().setTtlMs(2000L));
		tallyNode.increment(bean.getKey(), 1, null);

		// Wait for 4 seconds
		try{
			Thread.sleep(4 * 1000);
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
		}
		Assert.assertEquals(tallyNode.getTallyCount(bean.getKey(), null), null);
	}


	/*********************** Tracking Test Keys *********************************/


	private void deleteRecord(TallyKey key){
		tallyNode.delete(key, null);
	}

}
