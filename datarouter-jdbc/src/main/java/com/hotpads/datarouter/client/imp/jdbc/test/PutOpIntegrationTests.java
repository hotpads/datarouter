package com.hotpads.datarouter.client.imp.jdbc.test;

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
		Pair<String, String> result = test("testInsertOnDuplicateUpdate", new Config().setPutMethod(PutMethod.INSERT_ON_DUPLICATE_UPDATE));
		Assert.assertEquals("baz", result.getLeft());
		Assert.assertEquals("qux", result.getRight());
	}
	
	@Test
	public void testInsertOrBust(){
		Pair<String, String> result = test("testInsertOrBust", new Config().setPutMethod(PutMethod.INSERT_OR_BUST), false, true);
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
		Pair<String, String> result = test("testUpdateOrBust", new Config().setPutMethod(PutMethod.UPDATE_OR_BUST), true, true);
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
	
	private Pair<String, String> test(String testName, Config config, boolean expectedFirstCaught, boolean expectedSecondCaught){
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
		String after =  nullSafeGetC(router.putOptTest().get(new PutOpTestBeanKey(testName, "bar"), null));
		
		return new Pair<String, String>(before, after);
	}
	
	private String nullSafeGetC(PutOpTestBean bean){
		if(bean == null){
			return null;
		}
		return bean.getC();
	}
	
}
