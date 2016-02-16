package com.hotpads.datarouter.test.node.basic.manyfield;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DatarouterTestModuleFactory;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.arrays.LongArray;

@Guice(moduleFactory=DatarouterTestModuleFactory.class)
public abstract class BaseManyFieldIntegrationTests{

	/***************************** fields **************************************/

	@Inject
	private Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;

	protected MapStorageNode<ManyFieldBeanKey,ManyFieldBean> mapNode;

	@Deprecated //currently unused, but not ready to delete
	private final List<ManyFieldBeanKey> allKeys = new ArrayList<>();

	/***************************** constructors **************************************/

	public void setup(ClientId clientId, boolean useFielder){
		ManyFieldTestRouter router = new ManyFieldTestRouter(datarouter, nodeFactory, clientId, useFielder);
		mapNode = router.manyFieldTypeBean();

		resetTable();
	}

	private void resetTable(){
		if(!isMemcached()){
			mapNode.deleteAll(null);
		}
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
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
	public void testDelete(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setShortField((short)12);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped, bean);

		mapNode.delete(bean.getKey(), null);
		Assert.assertNull(mapNode.get(bean.getKey(), null));
	}

	@Test
	public void testNullKey(){
		if (!isJdbcOrHibernate()){
			return;
		}
		ManyFieldBean bean = new ManyFieldBean();
		mapNode.put(bean, null);
		AssertJUnit.assertNotNull(bean.getKey().getId());
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
			AssertJUnit.assertSame(bean, roundTripped);
		}else{
			AssertJUnit.assertNotSame(bean, roundTripped);
		}
		AssertJUnit.assertEquals(bean.getBooleanField(), roundTripped.getBooleanField());

		//test false value
		bean.setBooleanField(false);
		mapNode.put(bean, null);
		ManyFieldBean roundTrippedFalse = mapNode.get(bean.getKey(), null);
		if(isMemory()){
			AssertJUnit.assertSame(bean, roundTripped);
		}else{
			AssertJUnit.assertNotSame(bean, roundTripped);
		}
		AssertJUnit.assertEquals(bean.getBooleanField(), roundTrippedFalse.getBooleanField());

		recordKey(bean.getKey());
	}

	@Test
	public void testByte(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setByteField((byte)-57);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		if(isMemory()){
			AssertJUnit.assertSame(bean, roundTripped);
		}else{
			AssertJUnit.assertNotSame(bean, roundTripped);
		}
		AssertJUnit.assertEquals(bean.getByteField(), roundTripped.getByteField());
		recordKey(bean.getKey());
	}

	@Test
	public void testShort(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setShortField((short)-57);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		if(isMemory()){
			AssertJUnit.assertSame(bean, roundTripped);
		}else{
			AssertJUnit.assertNotSame(bean, roundTripped);
		}
		AssertJUnit.assertEquals(bean.getShortField(), roundTripped.getShortField());
		recordKey(bean.getKey());
	}

	@Test
	public void testInteger(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setIntegerField(-100057);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(bean.getIntegerField(), roundTripped.getIntegerField());

		bean.setIntegerField(12345);
		mapNode.put(bean, null);
		roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(bean.getIntegerField(), roundTripped.getIntegerField());

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
		AssertJUnit.assertEquals(expectedExceptions, exceptions);
		roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(bean.getIntegerField(), roundTripped.getIntegerField());
		AssertJUnit.assertEquals(roundTripped.getIntegerField().intValue(), -77);
		recordKey(bean.getKey());
	}

	@Test
	public void testLong(){
		ManyFieldBean bean = new ManyFieldBean();
		long negative6Billion = 3*(long)Integer.MIN_VALUE;
		bean.setLongField(negative6Billion);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(bean.getLongField(), roundTripped.getLongField());
		AssertJUnit.assertTrue(negative6Billion==roundTripped.getLongField());
		recordKey(bean.getKey());
	}

	@Test
	public void testFloat(){
		ManyFieldBean bean = new ManyFieldBean();
		float val = -157.34f;
		bean.setFloatField(val);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertNotNull(roundTripped);
		AssertJUnit.assertEquals(bean.getFloatField(), roundTripped.getFloatField());
		AssertJUnit.assertTrue(val==roundTripped.getFloatField());
		recordKey(bean.getKey());
	}

	@Test
	public void testNullPrimitive(){
		ManyFieldBean bean = new ManyFieldBean();
		Float val = null;
		bean.setFloatField(val);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(bean.getFloatField(), roundTripped.getFloatField());
		AssertJUnit.assertTrue(val==roundTripped.getFloatField());
		recordKey(bean.getKey());
	}

	@Test
	public void testDouble(){
		ManyFieldBean bean = new ManyFieldBean();
		double val = -100057.3456f;
		bean.setDoubleField(val);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(bean.getDoubleField(), roundTripped.getDoubleField());
		AssertJUnit.assertTrue(val==roundTripped.getDoubleField());
		recordKey(bean.getKey());
	}

	@Test
	public void testLongDate(){
		if(isHibernate()){
			return;
		}
		ManyFieldBean bean = new ManyFieldBean();
		Date val = new Date();
		bean.setLongDateField(val);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(bean.getLongDateField(), roundTripped.getLongDateField());
		AssertJUnit.assertTrue(val.equals(roundTripped.getLongDateField()));
		recordKey(bean.getKey());
	}

	@Test
	public void testCharacter(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setCharacterField('Q');
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(bean.getCharacterField(), roundTripped.getCharacterField());
		AssertJUnit.assertTrue('Q'==roundTripped.getCharacterField());
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
		AssertJUnit.assertEquals(bean.getStringField(), roundTripped.getStringField());
		String roundTrippedByteString = new String(roundTripped.getStringByteField(), StringByteTool.CHARSET_UTF8);
		AssertJUnit.assertEquals(val, roundTrippedByteString);
		recordKey(bean.getKey());
	}


	@Test
	public void testByteArray(){
		ManyFieldBean bean = new ManyFieldBean();
	//	byte[] value = new byte[]{ 0x1,0x5,-0x8,0x7f,0x25,0x6a,-0x80,-0x12	};
		byte[] value = new byte[]{ 1,5,-128,127,25,66,-80,-12};

		bean.setByteArrayField(value);
		mapNode.put(bean, null);
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(value, roundTripped.getByteArrayField());
		recordKey(bean.getKey());
	}

	@Test
	public void testVarInt(){

		ManyFieldBean bean0 = new ManyFieldBean();
		bean0.setVarIntField(0);
		mapNode.put(bean0, null);

		ManyFieldBean roundTripped0 = mapNode.get(bean0.getKey(), null);
		if(isMemory()){
			AssertJUnit.assertSame(bean0, roundTripped0);
		}else{
			AssertJUnit.assertNotSame(bean0, roundTripped0);
		}
		AssertJUnit.assertEquals(bean0.getVarIntField(), roundTripped0.getVarIntField());
		recordKey(bean0.getKey());

		//1234567
		ManyFieldBean bean1234567 = new ManyFieldBean();
		bean1234567.setVarIntField(1234567);
		mapNode.put(bean1234567, null);

		ManyFieldBean roundTripped1234567 = mapNode.get(bean1234567.getKey(), null);
		if(isMemory()){
			AssertJUnit.assertSame(bean1234567, roundTripped1234567);
		}else{
			AssertJUnit.assertNotSame(bean1234567, roundTripped1234567);
		}
		AssertJUnit.assertEquals(bean1234567.getVarIntField(), roundTripped1234567.getVarIntField());
		recordKey(bean1234567.getKey());

		//Integer.MAX_VALUE
		ManyFieldBean beanMax = new ManyFieldBean();
		beanMax.setVarIntField(Integer.MAX_VALUE);
		mapNode.put(beanMax, null);

		ManyFieldBean roundTrippedMax = mapNode.get(beanMax.getKey(), null);
		if(isMemory()){
			AssertJUnit.assertSame(beanMax, roundTrippedMax);
		}else{
			AssertJUnit.assertNotSame(beanMax, roundTrippedMax);
		}
		AssertJUnit.assertEquals(beanMax.getVarIntField(), roundTrippedMax.getVarIntField());
		recordKey(beanMax.getKey());
	}

	@Test
	public void testIntegerEnum(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setIntEnumField(TestEnum.beast);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(bean.getIntEnumField(), roundTripped.getIntEnumField());
		AssertJUnit.assertTrue(TestEnum.beast==roundTripped.getIntEnumField());
		recordKey(bean.getKey());
	}

	@Test
	public void testVarIntEnum(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setVarIntEnumField(TestEnum.fish);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(bean.getVarIntEnumField(), roundTripped.getVarIntEnumField());
		AssertJUnit.assertTrue(TestEnum.fish==roundTripped.getVarIntEnumField());
		recordKey(bean.getKey());
	}

	@Test
	public void testStringEnum(){
		if(isHibernate()){
			return;
		}
		ManyFieldBean bean = new ManyFieldBean();
		bean.setStringEnumField(TestEnum.cat);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(bean.getStringEnumField(), roundTripped.getStringEnumField());
		AssertJUnit.assertTrue(TestEnum.cat==roundTripped.getStringEnumField());
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
		AssertJUnit.assertArrayEquals(DrArrayTool.primitiveLongArray(ids),
				LongByteTool.fromComparableByteArray(roundTripped.getData()));
		recordKey(bean.getKey());
	}

	@Test
	public void testUInt31(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setIntegerField(7888);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertEquals(bean.getIntegerField(), roundTripped.getIntegerField());
		AssertJUnit.assertTrue(7888==roundTripped.getIntegerField());
		recordKey(bean.getKey());
	}

	@Test
	public void testLongArray(){
		if(isHibernate()){
			return;
		}
		ManyFieldBean bean = new ManyFieldBean();
		bean.appendToLongArrayField(Long.MAX_VALUE);
		bean.appendToLongArrayField(Integer.MAX_VALUE);
		bean.appendToLongArrayField(Short.MAX_VALUE);
		bean.appendToLongArrayField(Byte.MAX_VALUE);
		bean.appendToLongArrayField(5);
		bean.appendToLongArrayField(0);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertTrue(0==DrListTool.compare(bean.getLongArrayField(), roundTripped.getLongArrayField()));
		recordKey(bean.getKey());
	}

	@Test
	public void testBooleanArray(){
		if(isHibernate()){
			return;
		}
		ManyFieldBean bean = new ManyFieldBean();
		bean.appendToBooleanArrayField(true);
		bean.appendToBooleanArrayField(null);
		bean.appendToBooleanArrayField(false);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertTrue(0==DrListTool.compare(bean.getBooleanArrayField(), roundTripped.getBooleanArrayField()));
		recordKey(bean.getKey());
	}

	@Test
	public void testIntegerArray(){
		if(isHibernate()){
			return;
		}
		ManyFieldBean bean = new ManyFieldBean();
		bean.appendToIntegerArrayField(Integer.MAX_VALUE);
		bean.appendToIntegerArrayField(null);
		bean.appendToIntegerArrayField(-5029);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertTrue(0==DrListTool.compare(bean.getIntegerArrayField(), roundTripped.getIntegerArrayField()));
		recordKey(bean.getKey());
	}



	@Test
	public void testDoubleArray(){
		if(isHibernate()){
			return;
		}
		ManyFieldBean bean = new ManyFieldBean();
		bean.appendToDoubleArrayField(Double.MAX_VALUE);
		bean.appendToDoubleArrayField(null);
		bean.appendToDoubleArrayField(null);
		bean.appendToDoubleArrayField(Double.MIN_VALUE);
		bean.appendToDoubleArrayField(-5029.02939);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertTrue(0==DrListTool.compare(bean.getDoubleArrayField(), roundTripped.getDoubleArrayField()));
		recordKey(bean.getKey());
	}

	@Test
	public void testDelimitedStringArray(){
		if(isHibernate()){
			return;
		}
		ManyFieldBean bean = new ManyFieldBean();
		List<String> strings = DrListTool.create("abc hi!", "xxx's", "bb_3");
		bean.setDelimitedStringArrayField(strings);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertArrayEquals(strings.toArray(), roundTripped.getDelimitedStringArrayField().toArray());
		recordKey(bean.getKey());
	}



	/*@Test
	public void testBigLongArray(){
		if(isHibernate()){
			return;
		}
		ManyFieldBean bean = new ManyFieldBean();
		int numLongs = 1000000;//8MB
		if(isMemcached()){
			numLongs = 100000;//800kb (under memcached default 1mb max size)
		}
		for(int i=0; i < numLongs; ++i){
			bean.appendToLongArrayField(i);
		}
		mapNode.put(bean, null);
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		AssertJUnit.assertTrue(0==DrListTool.compare(bean.getLongArrayField(), roundTripped.getLongArrayField()));
		recordKey(bean.getKey());
	}*/

	/************************* helpers ********************************************/

	protected void recordKey(ManyFieldBeanKey key){
		allKeys.add(key);
	}



}
