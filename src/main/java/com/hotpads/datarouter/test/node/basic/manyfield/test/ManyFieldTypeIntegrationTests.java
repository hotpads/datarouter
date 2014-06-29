package com.hotpads.datarouter.test.node.basic.manyfield.test;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hbase.HBaseClientType;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseNode;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientType;
import com.hotpads.datarouter.client.imp.jdbc.JdbcClientType;
import com.hotpads.datarouter.client.imp.memcached.MemcachedClientType;
import com.hotpads.datarouter.client.imp.memory.MemoryClientType;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.SortedStorage.SortedStorageNode;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.BasicNodeTestRouter;
import com.hotpads.datarouter.test.node.basic.BasicNodeTestRouter.SortedBasicNodeTestRouter;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBean;
import com.hotpads.datarouter.test.node.basic.manyfield.ManyFieldTypeBeanKey;
import com.hotpads.datarouter.test.node.basic.manyfield.TestEnum;
import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.arrays.LongArray;

@RunWith(Parameterized.class)
public class ManyFieldTypeIntegrationTests {
	private static Logger logger = Logger.getLogger(ManyFieldTypeIntegrationTests.class);
	

	@Parameters
	public static Collection<Object[]> parameters(){
		List<Object[]> params = ListTool.createArrayList();
		params.add(new Object[]{DRTestConstants.CLIENT_drTestMemory, MemoryClientType.INSTANCE, false, false});
		params.add(new Object[]{DRTestConstants.CLIENT_drTestMemory, MemoryClientType.INSTANCE, false, true});
		params.add(new Object[]{DRTestConstants.CLIENT_drTestMemcached, MemcachedClientType.INSTANCE, false, false});
		params.add(new Object[]{DRTestConstants.CLIENT_drTestMemcached, MemcachedClientType.INSTANCE, false, true});
		params.add(new Object[]{DRTestConstants.CLIENT_drTestJdbc0, JdbcClientType.INSTANCE, true, false});
		params.add(new Object[]{DRTestConstants.CLIENT_drTestJdbc0, JdbcClientType.INSTANCE, true, true});
		params.add(new Object[]{DRTestConstants.CLIENT_drTestHibernate0, HibernateClientType.INSTANCE, true, false});
		params.add(new Object[]{DRTestConstants.CLIENT_drTestHibernate0, HibernateClientType.INSTANCE, true, true});
		params.add(new Object[]{DRTestConstants.CLIENT_drTestHBase, HBaseClientType.INSTANCE, true, false});
		params.add(new Object[]{DRTestConstants.CLIENT_drTestHBase, HBaseClientType.INSTANCE, true, true});
		return params;
	}
	
	@BeforeClass
	public static void init() throws IOException{	
	}
	
	/***************************** fields **************************************/
	
	private ClientType clientType;
	private boolean sorted;
	private BasicNodeTestRouter router;
	private MapStorageNode<ManyFieldTypeBeanKey,ManyFieldTypeBean> mapNode;
	private SortedStorageNode<ManyFieldTypeBeanKey,ManyFieldTypeBean> sortedNode;
	private List<ManyFieldTypeBeanKey> allKeys;

	/***************************** constructors **************************************/
	
	//runs before every @Test
	public ManyFieldTypeIntegrationTests(String clientName, ClientType clientType, boolean sorted, boolean entity){
		this.clientType = clientType;
		this.sorted = sorted;
		this.router = new SortedBasicNodeTestRouter(clientName, getClass(), entity);
		this.mapNode = router.manyFieldTypeBean();
		if(sorted){
			this.sortedNode = BaseDataRouter.cast(mapNode);
		}
		this.allKeys = ListTool.createArrayList();
		
		//delete rows from previous tests
		if(ObjectTool.notEquals(MemcachedClientType.INSTANCE, clientType)){
			logger.warn("calling deleteAll on :"+router.manyFieldTypeBean().getClass());
			mapNode.deleteAll(null);
		}
		if(isSorted()){
			Assert.assertEquals(0, IterableTool.count(sortedNode.scan(null, null)).intValue());
		}
	}
	
	@Before
	public void beforeEachTest(){
	}

	/***************************** tests **************************************/

	@Test
	public void testNullKey(){
		if (!isJdbcOrHibernate()){
			return;
		}
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		System.out.println("key : " + bean.getKey().getId());
		mapNode.put(bean, null);
		System.out.println("key after : " + bean.getKey().getId());
		Assert.assertNotNull(bean.getKey().getId());
		recordKey(bean.getKey());
	}
	
	@Test
	public void testBoolean(){
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		
		//test true value
		bean.setBooleanField(true);
		mapNode.put(bean, null);
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
		if(isMemory()){
			Assert.assertSame(bean, roundTripped);
		}else{
			Assert.assertNotSame(bean, roundTripped);
		}
		Assert.assertEquals(bean.getBooleanField(), roundTripped.getBooleanField());

		//test false value
		bean.setBooleanField(false);
		mapNode.put(bean, null);
		ManyFieldTypeBean roundTrippedFalse = mapNode.get(bean.getKey(), null);
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
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setByteField((byte)-57);
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
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
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setShortField((short)-57);
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
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
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setIntegerField(-100057);
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
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
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		long negative6Billion = 3*(long)Integer.MIN_VALUE;
		bean.setLongField(negative6Billion);
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getLongField(), roundTripped.getLongField());
		Assert.assertTrue(negative6Billion==roundTripped.getLongField());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testFloat(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		float val = -157.34f;
		bean.setFloatField(val);
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getFloatField(), roundTripped.getFloatField());
		Assert.assertTrue(val==roundTripped.getFloatField());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testNullPrimitive(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		Float val = null;
		bean.setFloatField(val);
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getFloatField(), roundTripped.getFloatField());
		Assert.assertTrue(val==roundTripped.getFloatField());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testDouble(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		double val = -100057.3456f;
		bean.setDoubleField(val);
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getDoubleField(), roundTripped.getDoubleField());
		Assert.assertTrue(val==roundTripped.getDoubleField());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testLongDate(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		Date val = new Date();
		bean.setLongDateField(val);
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getLongDateField(), roundTripped.getLongDateField());
		Assert.assertTrue(val.equals(roundTripped.getLongDateField()));
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testCharacter(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setCharacterField('Q');
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getCharacterField(), roundTripped.getCharacterField());
		Assert.assertTrue('Q'==roundTripped.getCharacterField());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testString(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		char multiByteUtf8Char = (char)555;
		String val = "abcdef"+multiByteUtf8Char;
		bean.setStringField(val);
		bean.setStringByteField(StringByteTool.getByteArray(val, StringByteTool.CHARSET_UTF8));
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
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
		ManyFieldTypeBean bean0 = new ManyFieldTypeBean();
		bean0.setVarIntField(0);
		mapNode.put(bean0, null);
		
		ManyFieldTypeBean roundTripped0 = mapNode.get(bean0.getKey(), null);		
		if(isMemory()){
			Assert.assertSame(bean0, roundTripped0);
		}else{
			Assert.assertNotSame(bean0, roundTripped0);
		}
		Assert.assertEquals(bean0.getVarIntField(), roundTripped0.getVarIntField());
		recordKey(bean0.getKey());
		
		//1234567
		ManyFieldTypeBean bean1234567 = new ManyFieldTypeBean();
		bean1234567.setVarIntField(1234567);
		mapNode.put(bean1234567, null);
		
		ManyFieldTypeBean roundTripped1234567 = mapNode.get(bean1234567.getKey(), null);
		if(isMemory()){
			Assert.assertSame(bean1234567, roundTripped1234567);
		}else{
			Assert.assertNotSame(bean1234567, roundTripped1234567);
		}
		Assert.assertEquals(bean1234567.getVarIntField(), roundTripped1234567.getVarIntField());
		recordKey(bean1234567.getKey());
		
		//Integer.MAX_VALUE
		ManyFieldTypeBean beanMax = new ManyFieldTypeBean();
		beanMax.setVarIntField(Integer.MAX_VALUE);
		mapNode.put(beanMax, null);
		
		ManyFieldTypeBean roundTrippedMax = mapNode.get(beanMax.getKey(), null);
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
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setIntEnumField(TestEnum.beast);
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getIntEnumField(), roundTripped.getIntEnumField());
		Assert.assertTrue(TestEnum.beast==roundTripped.getIntEnumField());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testVarIntEnum(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setVarIntEnumField(TestEnum.fish);
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getVarIntEnumField(), roundTripped.getVarIntEnumField());
		Assert.assertTrue(TestEnum.fish==roundTripped.getVarIntEnumField());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testStringEnum(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setStringEnumField(TestEnum.cat);
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
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
		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setData(bytes);
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertArrayEquals(ArrayTool.primitiveLongArray(ids), LongByteTool.fromComparableByteArray(roundTripped.getData()));
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testUInt31(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.setIntegerField(7888);
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(bean.getIntegerField(), roundTripped.getIntegerField());
		Assert.assertTrue(7888==roundTripped.getIntegerField());
		recordKey(bean.getKey());
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
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertTrue(0==ListTool.compare(bean.getLongArrayField(), roundTripped.getLongArrayField()));
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testBooleanArray(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.appendToBooleanArrayField(true);
		bean.appendToBooleanArrayField(null);
		bean.appendToBooleanArrayField(false);
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertTrue(0==ListTool.compare(bean.getBooleanArrayField(), roundTripped.getBooleanArrayField()));
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testIntegerArray(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.appendToIntegerArrayField(Integer.MAX_VALUE);
		bean.appendToIntegerArrayField(null);
		bean.appendToIntegerArrayField(-5029);
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertTrue(0==ListTool.compare(bean.getIntegerArrayField(), roundTripped.getIntegerArrayField()));
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testDoubleArray(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		bean.appendToDoubleArrayField(Double.MAX_VALUE);
		bean.appendToDoubleArrayField(null);
		bean.appendToDoubleArrayField(null);
		bean.appendToDoubleArrayField(Double.MIN_VALUE);
		bean.appendToDoubleArrayField(-5029.02939);
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertTrue(0==ListTool.compare(bean.getDoubleArrayField(), roundTripped.getDoubleArrayField()));
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testDelimitedStringArray(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		List<String> strings = ListTool.create("abc hi!", "xxx's", "bb_3");
		bean.setDelimitedStringArrayField(strings);
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertArrayEquals(strings.toArray(), roundTripped.getDelimitedStringArrayField().toArray());
		recordKey(bean.getKey());
	}
	
	@Test 
	public void testBigLongArray(){		
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		int numLongs = 1000000;//8MB
		if(isMemcached()){ numLongs = 100000; }//800kb (under memcached default 1mb max size)
		for(int i=0; i < numLongs; ++i){ 
			bean.appendToLongArrayField(i);
		}
		mapNode.put(bean, null);
		
		ManyFieldTypeBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertTrue(0==ListTool.compare(bean.getLongArrayField(), roundTripped.getLongArrayField()));
		recordKey(bean.getKey());
	}
	
	@Test
	public void testIncrement(){
		if(!isHBase()){ return; }
		HBaseNode hBaseNode = (HBaseNode)mapNode;
		ManyFieldTypeBean bean = new ManyFieldTypeBean();
		
		//increment by 3
		Map<ManyFieldTypeBeanKey,Map<String,Long>> increments = MapTool.create();
		MapTool.increment(increments, bean.getKey(), ManyFieldTypeBean.F.incrementField, 3L);
		hBaseNode.increment(increments, null);
		ManyFieldTypeBean result1 = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(new Long(3), result1.getIncrementField());
		
		//decrement by 11
		increments.clear();
		MapTool.increment(increments, bean.getKey(), ManyFieldTypeBean.F.incrementField, -11L);
		hBaseNode.increment(increments, null);
		ManyFieldTypeBean result2 = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(new Long(-8), result2.getIncrementField());
		
		//increment by 17 (expecting 3 - 11 + 17 => 9)
		increments.clear();
		MapTool.increment(increments, bean.getKey(), ManyFieldTypeBean.F.incrementField, 17L);
		hBaseNode.increment(increments, null);
		ManyFieldTypeBean result3 = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(new Long(9), result3.getIncrementField());
		
		recordKey(bean.getKey());
	}
	
	
	/************************** tests for unmarshalling into databeans (a little out of place here **************/
	
	@After 
	public void testGetAll(){
		if(isSorted()){
			Assert.assertEquals(allKeys.size(), IterableTool.count(sortedNode.scan(null, null)).intValue());
		}
	}
	
	@After
	public void testGetMulti(){
		List<ManyFieldTypeBean> allBeans = mapNode.getMulti(allKeys, null);
		Assert.assertEquals(allKeys.size(), allBeans.size());
	}
	
	
	/************************* helpers ********************************************/
	
	protected void recordKey(ManyFieldTypeBeanKey key){
		allKeys.add(key);
	}
	
	public boolean isSorted(){
		return sorted;
	}
	
	public boolean isMemory(){
		return MemoryClientType.INSTANCE.equals(clientType);
	}

	public boolean isJdbcOrHibernate(){
		return JdbcClientType.INSTANCE.equals(clientType) || HibernateClientType.INSTANCE.equals(clientType);
	}

	public boolean isHBase(){
		return HBaseClientType.INSTANCE.equals(clientType);
	}

	public boolean isMemcached(){
		return MemcachedClientType.INSTANCE.equals(clientType);
	}
}
