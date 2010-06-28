package com.hotpads.datarouter.test.node.basic.manyfield.test;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.test.node.basic.BasicNodeTestRouter;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBeanKey;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.arrays.LongArray;

public class ManyFieldTypeIntegrationTests {
	
	static BasicNodeTestRouter router;
	static List<ManyFieldTypeBeanKey> keys = ListTool.create();
	
	@BeforeClass
	public static void init() throws IOException{
		Injector injector = Guice.createInjector();
		router = injector.getInstance(BasicNodeTestRouter.class);
		
		router.manyFieldTypeBean().deleteAll(null);
		Assert.assertEquals(0, CollectionTool.size(router.manyFieldTypeBean().getAll(null)));
	}
	
	@Test 
	public void testByte(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setByteField((byte)-57);
		router.manyFieldTypeBean().put(bean, null);
		
		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertNotSame(bean, roundTripped);
		Assert.assertEquals(bean.getByteField(), roundTripped.getByteField());
		keys.add(bean.getKey());
	}
	
	@Test 
	public void testShort(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setShortField((short)-57);
		router.manyFieldTypeBean().put(bean, null);
		
		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertNotSame(bean, roundTripped);
		Assert.assertEquals(bean.getShortField(), roundTripped.getShortField());
		keys.add(bean.getKey());
	}
	
	@Test 
	public void testInteger(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setIntegerField(-100057);
		router.manyFieldTypeBean().put(bean, null);
		
		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getIntegerField(), roundTripped.getIntegerField());
		
		bean.setIntegerField(12345);
		router.manyFieldTypeBean().put(bean, null);
		roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getIntegerField(), roundTripped.getIntegerField());
		
		bean.setIntegerField(-77);
		int exceptions=0;
		try{
			router.manyFieldTypeBean().put(bean, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
		}catch(Exception e){
			++exceptions;
			router.manyFieldTypeBean().put(bean, new Config().setPutMethod(PutMethod.INSERT_OR_UPDATE));
		}
		Assert.assertEquals(1, exceptions);
		roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getIntegerField(), roundTripped.getIntegerField());
		Assert.assertTrue(roundTripped.getIntegerField().equals(-77));
		keys.add(bean.getKey());
	}
	
	@Test 
	public void testLong(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		long negative6Billion = 3*(long)Integer.MIN_VALUE;
		bean.setLongField(negative6Billion);
		router.manyFieldTypeBean().put(bean, null);
		
		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getLongField(), roundTripped.getLongField());
		Assert.assertTrue(negative6Billion==roundTripped.getLongField());
		keys.add(bean.getKey());
	}
	
	@Test 
	public void testFloat(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		float val = -157.34f;
		bean.setFloatField(val);
		router.manyFieldTypeBean().put(bean, null);
		
		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getFloatField(), roundTripped.getFloatField());
		Assert.assertTrue(val==roundTripped.getFloatField());
		keys.add(bean.getKey());
	}
	
	@Test 
	public void testDouble(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		double val = -100057.3456f;
		bean.setDoubleField(val);
		router.manyFieldTypeBean().put(bean, null);
		
		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getDoubleField(), roundTripped.getDoubleField());
		Assert.assertTrue(val==roundTripped.getDoubleField());
		keys.add(bean.getKey());
	}
	
	@Test 
	public void testLongDate(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		Date val = new Date();
		bean.setLongDateField(val);
		router.manyFieldTypeBean().put(bean, null);
		
		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getLongDateField(), roundTripped.getLongDateField());
		Assert.assertTrue(val.equals(roundTripped.getLongDateField()));
		keys.add(bean.getKey());
	}
	
	@Test 
	public void testCharacter(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setCharacterField('Q');
		router.manyFieldTypeBean().put(bean, null);
		
		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getCharacterField(), roundTripped.getCharacterField());
		Assert.assertTrue('Q'==roundTripped.getCharacterField());
		keys.add(bean.getKey());
	}
	
	@Test 
	public void testString(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		String val = "abcdefち";
		bean.setStringField(val);
		bean.setStringByteField(StringByteTool.getByteArray(val, StringByteTool.CHARSET_UTF8));
		router.manyFieldTypeBean().put(bean, null);
		
		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertFalse(bean.getStringField().equals(roundTripped.getStringField()));
		Assert.assertFalse(val.equals(roundTripped.getCharacterField()));//false if MySQL set to latin-1
		String roundTrippedByteString = new String(roundTripped.getStringByteField(), StringByteTool.CHARSET_UTF8);
		Assert.assertEquals(val, roundTrippedByteString);
		keys.add(bean.getKey());
	}
	
	@Test 
	public void testBlob(){
		LongArray ids = new LongArray();
		ids.add(5L);
		ids.add(10L);
		ids.add(15L);
		ids.add(126L);
		byte[] bytes = LongByteTool.getComparableByteArray(ids);
		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setData(bytes);
		router.manyFieldTypeBean().put(bean, null);
		
		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertArrayEquals(ArrayTool.primitiveLongArray(ids), LongByteTool.fromComparableByteArray(roundTripped.getData()));
		keys.add(bean.getKey());
	}
	
	@Test 
	public void testUInt31(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setIntegerField(7888);
		router.manyFieldTypeBean().put(bean, null);
		
		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertEquals(bean.getIntegerField(), roundTripped.getIntegerField());
		Assert.assertTrue(7888==roundTripped.getIntegerField());
		keys.add(bean.getKey());
	}
	
	@Test 
	public void testLongArray(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.appendToLongArrayField(Long.MAX_VALUE);
		bean.appendToLongArrayField(Integer.MAX_VALUE);
		bean.appendToLongArrayField(Short.MAX_VALUE);
		bean.appendToLongArrayField(Byte.MAX_VALUE);
		bean.appendToLongArrayField(5);
		bean.appendToLongArrayField(0);
		router.manyFieldTypeBean().put(bean, null);
		
		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertTrue(0==ListTool.compare(bean.getLongArrayField(), roundTripped.getLongArrayField()));
		keys.add(bean.getKey());
	}
	
	@Test 
	public void testBigLongArray(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		for(int i=0; i < 1000000; ++i){ //8MB
			bean.appendToLongArrayField(i);
		}
		router.manyFieldTypeBean().put(bean, null);
		
		ManyFieldTypeBean roundTripped = router.manyFieldTypeBean().get(bean.getKey(), null);
		Assert.assertTrue(0==ListTool.compare(bean.getLongArrayField(), roundTripped.getLongArrayField()));
		keys.add(bean.getKey());
	}
	
	
	/************************** tests for unmarshalling into databeans (a little out of place here **************/
	
	@Test 
	public void testGetAll(){
		List<ManyFieldTypeBean> allBeans = router.manyFieldTypeBean().getAll(null);
		Assert.assertTrue(CollectionTool.sameSize(keys, allBeans));
	}
	
	@Test
	public void testGetMulti(){
		List<ManyFieldTypeBean> allBeans = router.manyFieldTypeBean().getMulti(keys, null);
		Assert.assertTrue(CollectionTool.sameSize(keys, allBeans));
	}
	
}







