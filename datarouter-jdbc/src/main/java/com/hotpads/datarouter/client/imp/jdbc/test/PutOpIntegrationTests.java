package com.hotpads.datarouter.client.imp.jdbc.test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.jdbc.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.client.insert.PutOpTestBean;
import com.hotpads.datarouter.test.client.insert.PutOpTestBeanKey;
import com.hotpads.datarouter.test.client.insert.PutOpTestRouter;
import com.hotpads.util.core.collections.Pair;

@Guice(moduleFactory=TestDatarouterJdbcModuleFactory.class)
public class PutOpIntegrationTests{

	@Inject
	private DatarouterContext datarouterContext;
	@Inject
	private NodeFactory nodeFactory;
	
	private PutOpTestRouter router;
	
	@BeforeClass
	public void beforeClass(){
		router = new PutOpTestRouter(datarouterContext, nodeFactory, DrTestConstants.CLIENT_drTestJdbc0);
		
		resetTable();
	}
	
	@AfterClass
	public void afterClass(){
		datarouterContext.shutdown();
	}
	
	private void resetTable(){
		router.putOptTest().deleteAll(null);
	}
	
	@Test
	public void testDefault(){
		Pair<String, String> result = test("testDefault", null);
		Assert.assertEquals("baz", result.getLeft());
		Assert.assertEquals("qux", result.getRight());
	}
	
	@Test
	public void testInsertIgnore(){
		Pair<String, String> result = test("testInsertIgnore", new Config().setPutMethod(PutMethod.INSERT_IGNORE));
		Assert.assertEquals("baz", result.getLeft());
		Assert.assertEquals("baz", result.getRight());
	}
	
	@Test
	public void testInsertOnDuplicateUpdate(){
		Config config = new Config().setPutMethod(PutMethod.INSERT_ON_DUPLICATE_UPDATE);
		Pair<String, String> result = test("testInsertOnDuplicateUpdate", config);
		Assert.assertEquals("baz", result.getLeft());
		Assert.assertEquals("qux", result.getRight());
	}
	
	@Test
	public void testInsertOrBust(){
		Config config = new Config().setPutMethod(PutMethod.INSERT_OR_BUST);
		Pair<String, String> result = test("testInsertOrBust", config, false, true);
		Assert.assertEquals("baz", result.getLeft());
		Assert.assertEquals("baz", result.getRight());
	}
	
	@Test
	public void testInsertOrUpdate(){
		Pair<String, String> result = test("testInsertOrUpdate", new Config().setPutMethod(PutMethod.INSERT_OR_UPDATE));
		Assert.assertEquals("baz", result.getLeft());
		Assert.assertEquals("qux", result.getRight());
	}
	
	@Test
	public void testUpdateOrInsert(){
		Pair<String, String> result = test("testUpdateOrInsert", new Config().setPutMethod(PutMethod.UPDATE_OR_INSERT));
		Assert.assertEquals("baz", result.getLeft());
		Assert.assertEquals("qux", result.getRight());
	}
	
	@Test
	public void testUpdateOrBust(){
		Config config = new Config().setPutMethod(PutMethod.UPDATE_OR_BUST);
		Pair<String, String> result = test("testUpdateOrBust", config, true, true);
		Assert.assertNull(result.getLeft());
		Assert.assertNull(result.getRight());
	}
	
	@Test
	public void testMerge(){
		Pair<String, String> result = test("testMerge", new Config().setPutMethod(PutMethod.MERGE));
		Assert.assertEquals("baz", result.getLeft());
		Assert.assertEquals("qux", result.getRight());
	}
	
	private Pair<String, String> test(String testName, Config config){
		return test(testName, config, false, false);
	}
	
	private Pair<String,String> test(String testName, Config config, boolean expectedFirstCaught,
			boolean expectedSecondCaught){
		PutOpTestBean bean = new PutOpTestBean(testName, "bar", "baz");
		PutOpTestBean bean2 = new PutOpTestBean(testName, "bar", "qux");
		try{
			router.putOptTest().put(bean, config);
			Assert.assertFalse(expectedFirstCaught);
		}catch(Exception e){
			Assert.assertTrue(expectedFirstCaught);
		}
		String before = nullSafeGetC(router.putOptTest().get(new PutOpTestBeanKey(testName, "bar"), null));
		try{
			router.putOptTest().put(bean2, config);
			Assert.assertFalse(expectedSecondCaught);
		}catch(Exception e){
			Assert.assertTrue(expectedSecondCaught);
		}
		String after = nullSafeGetC(router.putOptTest().get(new PutOpTestBeanKey(testName, "bar"), null));
		
		return new Pair<>(before, after);
	}
	
	private String nullSafeGetC(PutOpTestBean bean){
		if(bean == null){
			return null;
		}
		return bean.getC();
	}
	
	@Test
	public void testMultiInsert(){
		int testBatchSize = 10;
		int totalCount = (int) (testBatchSize * 2.5);
		Config config = new Config()
				.setIterateBatchSize(testBatchSize)
				.setPutMethod(PutMethod.INSERT_ON_DUPLICATE_UPDATE);
		List<PutOpTestBean> databeans = new ArrayList<>();
		for (int count = 0; count < totalCount; count++){
			databeans.add(new PutOpTestBean("testMultiInsert", randomString(), randomString()));
		}
		router.putOptTest().putMulti(databeans, config);
		Assert.assertEquals(router.putOptTest().getMulti(DatabeanTool.getKeys(databeans), null).size(), totalCount);
	}
	
	private static final String randomString(){
		return UUID.randomUUID().toString();
	}
	
}
