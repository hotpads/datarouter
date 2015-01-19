package com.hotpads.datarouter.test.client.insert;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Injector;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.DatarouterTestInjectorProvider;
import com.hotpads.util.core.collections.Pair;

public class PutOpIntegrationTests{

	private static DatarouterContext datarouterContext;
	private static PutOpTestRouter router;
	
	@BeforeClass
	public static void beforeClass(){
		Injector injector = new DatarouterTestInjectorProvider().get();
		datarouterContext = injector.getInstance(DatarouterContext.class);
		NodeFactory nodeFactory = injector.getInstance(NodeFactory.class);
		router = new PutOpTestRouter(datarouterContext, nodeFactory, DRTestConstants.CLIENT_drTestJdbc0);
		
		resetTable();
	}
	
	@AfterClass
	public static void afterClass(){
		datarouterContext.shutdown();
	}
	
	private static void resetTable(){
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
