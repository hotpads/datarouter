package com.hotpads.datarouter.client.imp.jdbc.test;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.jdbc.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.storage.databean.DatabeanVersioningException;
import com.hotpads.datarouter.test.TestDatabeanKey;

@Guice(moduleFactory = TestDatarouterJdbcModuleFactory.class)
public class JdbcVersionedDatabeanIntegrationTests{

	private static final TestDatabeanKey KEY = new TestDatabeanKey("demat");
	private static final String ROAZHON = "roazhon";
	private static final String BEG = "beg";

	@Inject
	private VersionedDatabeanRouter router;

	@Test
	public void test(){
		router.versionedTestDatabean.delete(KEY, null);
		Assert.assertNull(router.versionedTestDatabean.get(KEY, null));

		TestVersionedDatabean databean = new TestVersionedDatabean(KEY, ROAZHON);
		router.versionedTestDatabean.put(databean, null);
		Assert.assertEquals(router.versionedTestDatabean.get(KEY, null).getBar(), ROAZHON);

		TestVersionedDatabean concurrentDatabean = new TestVersionedDatabean(KEY, BEG);
		try{
			router.versionedTestDatabean.put(concurrentDatabean, null);
			Assert.fail();
		}catch(DatabeanVersioningException e){
			//good
		}

		concurrentDatabean = router.versionedTestDatabean.get(KEY, null);
		Assert.assertEquals(concurrentDatabean.getBar(), ROAZHON);
		concurrentDatabean.setBar(BEG);
		router.versionedTestDatabean.put(concurrentDatabean, null);
		Assert.assertEquals(router.versionedTestDatabean.get(KEY, null).getBar(), BEG);
	}

}
