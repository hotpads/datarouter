
package com.hotpads.datarouter.client.imp.memcached.test;

import javax.inject.Inject;

import org.junit.AfterClass;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.client.imp.memcached.node.MemcachedNode;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.profile.tally.Tally;
import com.hotpads.datarouter.profile.tally.Tally.TallyFielder;
import com.hotpads.datarouter.profile.tally.TallyKey;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;
import com.hotpads.datarouter.test.DrTestConstants;

@Guice(moduleFactory=DatarouterStorageTestModuleFactory.class)
public class TallyIntegrationTests{

	/***************************** fields **************************************/

	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterClients datarouterClients;
	@Inject
	private NodeFactory nodeFactory;

	private MemcachedNode<TallyKey, Tally, TallyFielder> tallyNode;

	/***************************** constructors **************************************/

	public void setup(ClientId clientId, boolean useFielder){
		TallyTestRouter router = new TallyTestRouter(datarouter, datarouterClients, nodeFactory, clientId, useFielder);
		tallyNode = router.tally();
	}


	@BeforeClass
	public void beforeClass(){
		setup(DrTestConstants.CLIENT_drTestMemcached, false);
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}


	/***************************** tests **************************************/


	@Test
	public void testIncrement(){
		Tally bean = new Tally("testKey1");
		tallyNode.put(bean, null);

		int count = 5;
		tallyNode.increment(bean.getKey(), count, null);
		Assert.assertEquals(tallyNode.getTallyCount(bean.getKey()), null);

		count += 100;
		tallyNode.increment(bean.getKey(), count, null);
		Assert.assertEquals(tallyNode.getTallyCount(bean.getKey()), null);

		deleteRecord(bean.getKey());
	}


	@Test(expectedExceptions=NullPointerException.class)
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

		tallyNode.increment(bean.getKey(), 5, null);

		// if assert error occurs, delete key then rerun test
		Assert.assertEquals(tallyNode.getTallyCount(bean.getKey()), new Long(5));
		Assert.assertEquals(tallyNode.getTallyCount(bean.getKey()), new Long(5));
		tallyNode.increment(bean.getKey(), 5, null);
		Assert.assertEquals(tallyNode.getTallyCount(bean.getKey()), new Long(10));

		deleteRecord(bean.getKey());
	}

	@Test
	public void testGetTallyCountOnNull(){
		Tally bean = new Tally();
		Assert.assertEquals(tallyNode.getTallyCount(bean.getKey()), null);

		deleteRecord(bean.getKey());
	}


	/*********************** Tracking Test Keys *********************************/


	private void deleteRecord(TallyKey key){
		tallyNode.delete(key, null);
	}

}
