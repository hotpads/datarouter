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
	protected TallyTestRouter router;
	protected MemcachedNode<TallyKey, Tally, TallyFielder> tallyNodeRouter;

	/***************************** constructors **************************************/

	public void setup(ClientId clientId, boolean useFielder){
		router = new TallyTestRouter(datarouter, datarouterClients, nodeFactory, clientId, useFielder);
		tallyNodeRouter = router.tally();

		resetTable();
	}

	private void resetTable(){
		if(!isMemcached()){
			tallyNodeRouter.deleteAll(null);
		}
	}

	@BeforeClass
	public void beforeClass(){
		setup(DrTestConstants.CLIENT_drTestMemcached, false);
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	/********************** subclasses should override these ************************/

	public boolean isMemcached(){
		return true;
	}


	/***************************** tests **************************************/


	@Test
	public void testIncrement(){
		Tally bean = new Tally("_key_");
		tallyNodeRouter.put(bean, null);

		int count = 5;
		tallyNodeRouter.increment(bean.getKey(), count, null);
		Assert.assertEquals(tallyNodeRouter.getTallyCount(bean.getKey()), count);

		count += 100;
		tallyNodeRouter.increment(bean.getKey(), count, null);
		Assert.assertEquals(tallyNodeRouter.getTallyCount(bean.getKey()), 110 );
	}
}
