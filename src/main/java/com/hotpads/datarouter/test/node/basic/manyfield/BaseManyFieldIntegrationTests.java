package com.hotpads.datarouter.test.node.basic.manyfield;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DatarouterTestInjectorProvider;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.arrays.LongArray;

public abstract class BaseManyFieldIntegrationTests{
	private static Logger logger = LoggerFactory.getLogger(BaseManyFieldIntegrationTests.class);
	
	/***************************** fields **************************************/
	
	private static DatarouterContext datarouterContext;
	private static MapStorageNode<ManyFieldBeanKey,ManyFieldBean> mapNode;
	
	@Deprecated //currently unused, but not ready to delete
	private List<ManyFieldBeanKey> allKeys = new ArrayList<>();

	/***************************** constructors **************************************/
	
	public static void setup(String clientName, boolean useFielder){
		Injector injector = new DatarouterTestInjectorProvider().get();
		datarouterContext = injector.getInstance(DatarouterContext.class);
		NodeFactory nodeFactory = injector.getInstance(NodeFactory.class);
		ManyFieldTestRouter router = new ManyFieldTestRouter(datarouterContext, nodeFactory, clientName, useFielder);
		mapNode = router.manyFieldTypeBean();

		resetTable();
	}
	
	private static void resetTable(){
		try{
			mapNode.deleteAll(null);
		}catch(UnsupportedOperationException e){
			//swallow memcached unsupported op.  should probably take deleteAll out of the MapStorage interface
			//too bad i can't call the isMemcached method from this static method
		}
	}
	
	@AfterClass
	public static void afterClass(){
		datarouterContext.shutdown();
	}
	
	/********************** subclasses should override these ************************/

	public boolean isMemory(){
		return false;
	}
	
	public boolean isHibernate(){
		return false;
	}
	
	public boolean isJdbc(){
		return false;
	}
	
	public boolean isJdbcOrHibernate(){
		return isHibernate() || isJdbc();
	}
	
	public boolean isHBase(){
		return false;
	}
	
	public boolean isMemcached(){
		return false;
	}

	/***************************** tests **************************************/
	
	@Test
	public void testNullKey(){
		if (!isJdbcOrHibernate()){
			return;
		}
		ManyFieldBean bean = new ManyFieldBean();
		System.out.println("key : " + bean.getKey().getId());
		mapNode.put(bean, null);
		System.out.println("key after : " + bean.getKey().getId());
		Assert.assertNotNull(bean.getKey().getId());
		recordKey(bean.getKey());
	}
	
	@Test
	public void testBoolean(){
		ManyFieldBean bean = new ManyFieldBean();
		
		//test true value
		bean.setBooleanField(true);
		mapNode.put(bean, null);
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		if(isMemory()){
			Assert.assertSame(bean, roundTripped);
		}else{
			Assert.assertNotSame(bean, roundTripped);
		}
		Assert.assertEquals(bean.getBooleanField(), roundTripped.getBooleanField());

		//test false value
		bean.setBooleanField(false);
		mapNode.put(bean, null);
		ManyFieldBean roundTrippedFalse = mapNode.get(bean.getKey(), null);
		if(isMemory()){
			Assert.assertSame(bean, roundTripped);
		}else{
			Assert.assertNotSame(bean, roundTripped);
		}
		Assert.assertEquals(bean.getBooleanField(), roundTrippedFalse.getBooleanField());
		
		recordKey(bean.getKey());
	}

	@Test 
	public void testByte(){		
		ManyFieldBean bean = new ManyFieldBean();
		bean.setByteField((byte)-57);
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		if(isMemory()){
			Assert.assertSame(bean, roundTripped);
		}else{
			Assert.assertNotSame(bean, roundTripped);
		}
		Assert.assertEquals(bean.getByteField(), roundTripped.getByteField());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testShort(){		
		ManyFieldBean bean = new ManyFieldBean();
		bean.setShortField((short)-57);
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		if(isMemory()){
			Assert.assertSame(bean, roundTripped);
		}else{
			Assert.assertNotSame(bean, roundTripped);
		}
		Assert.assertEquals(bean.getShortField(), roundTripped.getShortField());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testInteger(){		
		ManyFieldBean bean = new ManyFieldBean();
		bean.setIntegerField(-100057);
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getIntegerField(), roundTripped.getIntegerField());
		
		bean.setIntegerField(12345);
		mapNode.put(bean, null);
		roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getIntegerField(), roundTripped.getIntegerField());
		
		bean.setIntegerField(-77);
		int exceptions=0;
		try{
			//hibernate should error with this PutMethod
			mapNode.put(bean, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
		}catch(Exception e){
			++exceptions;
			mapNode.put(bean, new Config().setPutMethod(PutMethod.INSERT_OR_UPDATE));
		}
		int expectedExceptions;
		if(isJdbcOrHibernate()){
			expectedExceptions = 1;
		}else{
			expectedExceptions = 0;
		}
		Assert.assertEquals(expectedExceptions, exceptions);
		roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getIntegerField(), roundTripped.getIntegerField());
		Assert.assertTrue(roundTripped.getIntegerField().equals(-77));
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testLong(){		
		ManyFieldBean bean = new ManyFieldBean();
		long negative6Billion = 3*(long)Integer.MIN_VALUE;
		bean.setLongField(negative6Billion);
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getLongField(), roundTripped.getLongField());
		Assert.assertTrue(negative6Billion==roundTripped.getLongField());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testFloat(){		
		ManyFieldBean bean = new ManyFieldBean();
		float val = -157.34f;
		bean.setFloatField(val);
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertNotNull(roundTripped);
		Assert.assertEquals(bean.getFloatField(), roundTripped.getFloatField());
		Assert.assertTrue(val==roundTripped.getFloatField());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testNullPrimitive(){		
		ManyFieldBean bean = new ManyFieldBean();
		Float val = null;
		bean.setFloatField(val);
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getFloatField(), roundTripped.getFloatField());
		Assert.assertTrue(val==roundTripped.getFloatField());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testDouble(){		
		ManyFieldBean bean = new ManyFieldBean();
		double val = -100057.3456f;
		bean.setDoubleField(val);
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getDoubleField(), roundTripped.getDoubleField());
		Assert.assertTrue(val==roundTripped.getDoubleField());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testLongDate(){	
		if(isHibernate()){ return; }	
		ManyFieldBean bean = new ManyFieldBean();
		Date val = new Date();
		bean.setLongDateField(val);
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getLongDateField(), roundTripped.getLongDateField());
		Assert.assertTrue(val.equals(roundTripped.getLongDateField()));
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testCharacter(){		
		ManyFieldBean bean = new ManyFieldBean();
		bean.setCharacterField('Q');
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getCharacterField(), roundTripped.getCharacterField());
		Assert.assertTrue('Q'==roundTripped.getCharacterField());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testString(){		
		ManyFieldBean bean = new ManyFieldBean();
		char multiByteUtf8Char = (char)555;
		String val = "abcdef"+multiByteUtf8Char;
		bean.setStringField(val);
		bean.setStringByteField(StringByteTool.getByteArray(val, StringByteTool.CHARSET_UTF8));
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		if(isJdbcOrHibernate()){//we're expecting the db to be in ASCII mode and strip out that weird character
			Assert.assertFalse(bean.getStringField().equals(roundTripped.getStringField()));
		}else{//byte arrays should handle any string
			Assert.assertEquals(bean.getStringField(), roundTripped.getStringField());
		}
		String roundTrippedByteString = new String(roundTripped.getStringByteField(), StringByteTool.CHARSET_UTF8);
		Assert.assertEquals(val, roundTrippedByteString);
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testVarInt(){	
		//0
		ManyFieldBean bean0 = new ManyFieldBean();
		bean0.setVarIntField(0);
		mapNode.put(bean0, null);
		
		ManyFieldBean roundTripped0 = mapNode.get(bean0.getKey(), null);		
		if(isMemory()){
			Assert.assertSame(bean0, roundTripped0);
		}else{
			Assert.assertNotSame(bean0, roundTripped0);
		}
		Assert.assertEquals(bean0.getVarIntField(), roundTripped0.getVarIntField());
		recordKey(bean0.getKey());
		
		//1234567
		ManyFieldBean bean1234567 = new ManyFieldBean();
		bean1234567.setVarIntField(1234567);
		mapNode.put(bean1234567, null);
		
		ManyFieldBean roundTripped1234567 = mapNode.get(bean1234567.getKey(), null);
		if(isMemory()){
			Assert.assertSame(bean1234567, roundTripped1234567);
		}else{
			Assert.assertNotSame(bean1234567, roundTripped1234567);
		}
		Assert.assertEquals(bean1234567.getVarIntField(), roundTripped1234567.getVarIntField());
		recordKey(bean1234567.getKey());
		
		//Integer.MAX_VALUE
		ManyFieldBean beanMax = new ManyFieldBean();
		beanMax.setVarIntField(Integer.MAX_VALUE);
		mapNode.put(beanMax, null);
		
		ManyFieldBean roundTrippedMax = mapNode.get(beanMax.getKey(), null);
		if(isMemory()){
			Assert.assertSame(beanMax, roundTrippedMax);
		}else{
			Assert.assertNotSame(beanMax, roundTrippedMax);
		}
		Assert.assertEquals(beanMax.getVarIntField(), roundTrippedMax.getVarIntField());
		recordKey(beanMax.getKey());
	}
	
	@Test 
	public void testIntegerEnum(){		
		ManyFieldBean bean = new ManyFieldBean();
		bean.setIntEnumField(TestEnum.beast);
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getIntEnumField(), roundTripped.getIntEnumField());
		Assert.assertTrue(TestEnum.beast==roundTripped.getIntEnumField());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testVarIntEnum(){		
		ManyFieldBean bean = new ManyFieldBean();
		bean.setVarIntEnumField(TestEnum.fish);
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getVarIntEnumField(), roundTripped.getVarIntEnumField());
		Assert.assertTrue(TestEnum.fish==roundTripped.getVarIntEnumField());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testStringEnum(){
		if(isHibernate()){ return; }	
		ManyFieldBean bean = new ManyFieldBean();
		bean.setStringEnumField(TestEnum.cat);
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getStringEnumField(), roundTripped.getStringEnumField());
		Assert.assertTrue(TestEnum.cat==roundTripped.getStringEnumField());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testBlob(){
		LongArray ids = new LongArray();
		ids.add(5L);
		ids.add(10L);
		ids.add(15L);
		ids.add(126L);
		byte[] bytes = LongByteTool.getComparableByteArray(ids);
		
		ManyFieldBean bean = new ManyFieldBean();
		bean.setData(bytes);
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertArrayEquals(ArrayTool.primitiveLongArray(ids), LongByteTool.fromComparableByteArray(roundTripped.getData()));
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testUInt31(){		
		ManyFieldBean bean = new ManyFieldBean();
		bean.setIntegerField(7888);
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getIntegerField(), roundTripped.getIntegerField());
		Assert.assertTrue(7888==roundTripped.getIntegerField());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testLongArray(){
		if(isHibernate()){ return; }	
		ManyFieldBean bean = new ManyFieldBean();
		bean.appendToLongArrayField(Long.MAX_VALUE);
		bean.appendToLongArrayField(Integer.MAX_VALUE);
		bean.appendToLongArrayField(Short.MAX_VALUE);
		bean.appendToLongArrayField(Byte.MAX_VALUE);
		bean.appendToLongArrayField(5);
		bean.appendToLongArrayField(0);
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertTrue(0==ListTool.compare(bean.getLongArrayField(), roundTripped.getLongArrayField()));
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testBooleanArray(){		
		if(isHibernate()){ return; }
		ManyFieldBean bean = new ManyFieldBean();
		bean.appendToBooleanArrayField(true);
		bean.appendToBooleanArrayField(null);
		bean.appendToBooleanArrayField(false);
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertTrue(0==ListTool.compare(bean.getBooleanArrayField(), roundTripped.getBooleanArrayField()));
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testIntegerArray(){	
		if(isHibernate()){ return; }	
		ManyFieldBean bean = new ManyFieldBean();
		bean.appendToIntegerArrayField(Integer.MAX_VALUE);
		bean.appendToIntegerArrayField(null);
		bean.appendToIntegerArrayField(-5029);
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertTrue(0==ListTool.compare(bean.getIntegerArrayField(), roundTripped.getIntegerArrayField()));
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testDoubleArray(){
		if(isHibernate()){ return; }	
		ManyFieldBean bean = new ManyFieldBean();
		bean.appendToDoubleArrayField(Double.MAX_VALUE);
		bean.appendToDoubleArrayField(null);
		bean.appendToDoubleArrayField(null);
		bean.appendToDoubleArrayField(Double.MIN_VALUE);
		bean.appendToDoubleArrayField(-5029.02939);
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertTrue(0==ListTool.compare(bean.getDoubleArrayField(), roundTripped.getDoubleArrayField()));
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testDelimitedStringArray(){	
		if(isHibernate()){ return; }
		ManyFieldBean bean = new ManyFieldBean();
		List<String> strings = ListTool.create("abc hi!", "xxx's", "bb_3");
		bean.setDelimitedStringArrayField(strings);
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertArrayEquals(strings.toArray(), roundTripped.getDelimitedStringArrayField().toArray());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testBigLongArray(){	
		if(isHibernate()){ return; }	
		ManyFieldBean bean = new ManyFieldBean();
		int numLongs = 1000000;//8MB
		if(isMemcached()){ numLongs = 100000; }//800kb (under memcached default 1mb max size)
		for(int i=0; i < numLongs; ++i){ 
			bean.appendToLongArrayField(i);
		}
		mapNode.put(bean, null);
		
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertTrue(0==ListTool.compare(bean.getLongArrayField(), roundTripped.getLongArrayField()));
		recordKey(bean.getKey());
	}
	
	@Test
	public void testIncrement(){
		if(!isHBase()){ return; }
		HBaseNode hBaseNode = (HBaseNode)mapNode;
		ManyFieldBean bean = new ManyFieldBean();
		
		//increment by 3
		Map<ManyFieldBeanKey,Map<String,Long>> increments = MapTool.create();
		MapTool.increment(increments, bean.getKey(), ManyFieldBean.F.incrementField, 3L);
		hBaseNode.increment(increments, null);
		ManyFieldBean result1 = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(new Long(3), result1.getIncrementField());
		
		//decrement by 11
		increments.clear();
		MapTool.increment(increments, bean.getKey(), ManyFieldBean.F.incrementField, -11L);
		hBaseNode.increment(increments, null);
		ManyFieldBean result2 = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(new Long(-8), result2.getIncrementField());
		
		//increment by 17 (expecting 3 - 11 + 17 => 9)
		increments.clear();
		MapTool.increment(increments, bean.getKey(), ManyFieldBean.F.incrementField, 17L);
		hBaseNode.increment(increments, null);
		ManyFieldBean result3 = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(new Long(9), result3.getIncrementField());
		
		recordKey(bean.getKey());
	}
	
	
	/************************* helpers ********************************************/
	
	protected void recordKey(ManyFieldBeanKey key){
		allKeys.add(key);
	}
	
}
