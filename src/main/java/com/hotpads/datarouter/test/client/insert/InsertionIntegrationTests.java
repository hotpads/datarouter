package com.hotpads.datarouter.test.client.insert;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.test.client.BasicClientTestRouter;
import com.hotpads.util.core.collections.Pair;

public class InsertionIntegrationTests{

	private BasicClientTestRouter router;
	
	public InsertionIntegrationTests(){
		Injector injector = Guice.createInjector();
		router = injector.getInstance(BasicClientTestRouter.class);
	}
	
	@Before
	public void clear(){
		router.putOptTest().deleteAll(null);
	}
	
	@Test
	public void testDefault(){
		Pair<String, String> result = test(null);
		Assert.assertEquals("baz", result.getLeft());
		Assert.assertEquals("qux", result.getRight());
	}
	
	@Test
	public void testInsertIgnore(){
		Pair<String, String> result = test(new Config().setPutMethod(PutMethod.INSERT_IGNORE));
		Assert.assertEquals("baz", result.getLeft());
		Assert.assertEquals("baz", result.getRight());
	}
	
	@Test
	public void testInsertOnDuplicateUpdate(){
		Pair<String, String> result = test(new Config().setPutMethod(PutMethod.INSERT_ON_DUPLICATE_UPDATE));
		Assert.assertEquals("baz", result.getLeft());
		Assert.assertEquals("qux", result.getRight());
	}
	
	@Test
	public void testInsertOrBust(){
		Pair<String, String> result = test(new Config().setPutMethod(PutMethod.INSERT_OR_BUST), false, true);
		Assert.assertEquals("baz", result.getLeft());
		Assert.assertEquals("baz", result.getRight());
	}
	
	@Test
	public void testInsertOrUpdate(){
		Pair<String, String> result = test(new Config().setPutMethod(PutMethod.INSERT_OR_UPDATE));
		Assert.assertEquals("baz", result.getLeft());
		Assert.assertEquals("qux", result.getRight());
	}
	
	@Test
	public void testUpdateOrInsert(){
		Pair<String, String> result = test(new Config().setPutMethod(PutMethod.UPDATE_OR_INSERT));
		Assert.assertEquals("baz", result.getLeft());
		Assert.assertEquals("qux", result.getRight());
	}
	
	@Test
	public void testUpdateOrBust(){
		Pair<String, String> result = test(new Config().setPutMethod(PutMethod.UPDATE_OR_BUST), true, true);
		Assert.assertNull(result.getLeft());
		Assert.assertNull(result.getRight());
	}
	
	@Test
	public void testMerge(){
		Pair<String, String> result = test(new Config().setPutMethod(PutMethod.MERGE));
		Assert.assertEquals("baz", result.getLeft());
		Assert.assertEquals("qux", result.getRight());
	}
	
	private Pair<String, String> test(Config config){
		return test(config, false, false);
	}
	
	private Pair<String, String> test(Config config, boolean expectedFirstCaught, boolean expectedSecondCaught){
		PutOpTestBean bean = new PutOpTestBean("foo", "bar", "baz");
		PutOpTestBean bean2 = new PutOpTestBean("foo", "bar", "qux");
		try{
			router.putOptTest().put(bean, config);
			Assert.assertFalse(expectedFirstCaught);
		}catch(Exception e){
			Assert.assertTrue(expectedFirstCaught);
		}
		String before = nullSafeGetC(router.putOptTest().get(new PutOpTestBeanKey("foo", "bar"), null));
		try{
			router.putOptTest().put(bean2, config);
			Assert.assertFalse(expectedSecondCaught);
		}catch(Exception e){
			Assert.assertTrue(expectedSecondCaught);
		}
		String after =  nullSafeGetC(router.putOptTest().get(new PutOpTestBeanKey("foo", "bar"), null));
		
		return new Pair<String, String>(before, after);
	}
	
	private String nullSafeGetC(PutOpTestBean bean){
		if(bean == null){
			return null;
		}
		return bean.getC();
	}
	
}
