package com.hotpads.datarouter.test.node.basic.manyfield;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;
import com.hotpads.datarouter.test.TestDatarouterProperties;
import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.bytes.LongByteTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.collections.arrays.LongArray;

@Guice(moduleFactory = DatarouterStorageTestModuleFactory.class)
public abstract class BaseManyFieldIntegrationTests{

	/***************************** fields **************************************/

	@Inject
	private TestDatarouterProperties datarouterProperties;
	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterSettings datarouterSettings;
	@Inject
	private NodeFactory nodeFactory;

	protected MapStorageNode<ManyFieldBeanKey,ManyFieldBean> mapNode;

	@Deprecated //currently unused, but not ready to delete
	private final List<ManyFieldBeanKey> allKeys = new ArrayList<>();

	/***************************** constructors **************************************/

	public void setup(ClientId clientId){
		ManyFieldTestRouter router = new ManyFieldTestRouter(datarouterProperties, datarouter, datarouterSettings,
				nodeFactory, clientId);
		mapNode = router.manyFieldTypeBean();

		resetTable();
	}

	private void resetTable(){
		if(!isMemcached() && !isRedis()){
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

	public boolean isJdbc(){
		return false;
	}

	public boolean isHBase(){
		return false;
	}

	public boolean isMemcached(){
		return false;
	}

	public boolean isRedis(){
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
		if(!isJdbc()){
			return;
		}
		ManyFieldBean bean = new ManyFieldBean();
		mapNode.put(bean, null);
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
			Assert.assertSame(roundTripped, bean);
		}else{
			Assert.assertNotSame(roundTripped, bean);
		}
		Assert.assertEquals(roundTripped.getBooleanField(), bean.getBooleanField());

		//test false value
		bean.setBooleanField(false);
		mapNode.put(bean, null);
		ManyFieldBean roundTrippedFalse = mapNode.get(bean.getKey(), null);
		if(isMemory()){
			Assert.assertSame(roundTrippedFalse, bean);
		}else{
			Assert.assertNotSame(roundTrippedFalse, bean);
		}
		Assert.assertEquals(roundTrippedFalse.getBooleanField(), bean.getBooleanField());

		recordKey(bean.getKey());
	}

	@Test
	public void testByte(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setByteField((byte)-57);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		if(isMemory()){
			Assert.assertSame(roundTripped, bean);
		}else{
			Assert.assertNotSame(roundTripped, bean);
		}
		Assert.assertEquals(roundTripped.getByteField(), bean.getByteField());
		recordKey(bean.getKey());
	}

	@Test
	public void testShort(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setShortField((short)-57);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		if(isMemory()){
			Assert.assertSame(roundTripped, bean);
		}else{
			Assert.assertNotSame(roundTripped, bean);
		}
		Assert.assertEquals(roundTripped.getShortField(), bean.getShortField());
		recordKey(bean.getKey());
	}

	@Test
	public void testInteger(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setIntegerField(-100057);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped.getIntegerField(), bean.getIntegerField());

		bean.setIntegerField(12345);
		mapNode.put(bean, null);
		roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped.getIntegerField(), bean.getIntegerField());

		bean.setIntegerField(-77);
		int exceptions = 0;
		try{
			//hibernate should error with this PutMethod
			mapNode.put(bean, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
		}catch(Exception e){
			++exceptions;
			mapNode.put(bean, new Config().setPutMethod(PutMethod.INSERT_OR_UPDATE));
		}
		int expectedExceptions;
		if(isJdbc()){
			expectedExceptions = 1;
		}else{
			expectedExceptions = 0;
		}
		Assert.assertEquals(exceptions, expectedExceptions);
		roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped.getIntegerField(), bean.getIntegerField());
		Assert.assertEquals(roundTripped.getIntegerField().intValue(), -77);
		recordKey(bean.getKey());
	}

	@Test
	public void testLong(){
		ManyFieldBean bean = new ManyFieldBean();
		long negative6Billion = 3 * (long)Integer.MIN_VALUE;
		bean.setLongField(negative6Billion);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped.getLongField(), bean.getLongField());
		Assert.assertTrue(negative6Billion == roundTripped.getLongField());
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
		Assert.assertEquals(roundTripped.getFloatField(), bean.getFloatField());
		Assert.assertTrue(val == roundTripped.getFloatField());
		recordKey(bean.getKey());
	}

	@Test
	public void testNullPrimitive(){
		ManyFieldBean bean = new ManyFieldBean();
		Float val = null;
		bean.setFloatField(val);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped.getFloatField(), bean.getFloatField());
		Assert.assertEquals(roundTripped.getFloatField(), val);
		recordKey(bean.getKey());
	}

	@Test
	public void testDouble(){
		ManyFieldBean bean = new ManyFieldBean();
		double val = -100057.3456f;
		bean.setDoubleField(val);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped.getDoubleField(), bean.getDoubleField());
		Assert.assertTrue(val == roundTripped.getDoubleField());
		recordKey(bean.getKey());
	}

	@Test
	public void testLongDate(){
		ManyFieldBean bean = new ManyFieldBean();
		Date val = new Date();
		bean.setLongDateField(val);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped.getLongDateField(), bean.getLongDateField());
		Assert.assertTrue(val.equals(roundTripped.getLongDateField()));
		recordKey(bean.getKey());
	}

	@Test
	public void testLocalDateTime(){
		ManyFieldBean bean = new ManyFieldBean();
		// LocalDateTime.now() uses the system clock as default so it will always get fractional seconds up to 3 digits
		// (i.e. milliseconds) and no more.
		LocalDateTime val = LocalDateTime.now();
		bean.setDateTimeField(val);
		mapNode.put(bean, null);
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped.getDateTimeField(), bean.getDateTimeField());
		Assert.assertEquals(roundTripped.getDateTimeField(), val);

		// LocalDateTime.of can set the value of nanoseconds in a range from 0 to 999,999,999
		// MySql.DateTime cannot handle this level of granularity and will truncate the fractional second value
		// so the value of the LocalDateTime retrieved from the database will not be equal to the LocalDateTime saved
		LocalDateTime valOutOfBounds = LocalDateTime.of(2015, 12, 24, 2, 3, 4, 50);
		bean.setDateTimeField(valOutOfBounds);
		mapNode.put(bean, null);
		ManyFieldBean roundTripped2 = mapNode.get(bean.getKey(), null);
		if(isMemory()){
			Assert.assertEquals(roundTripped2.getDateTimeField(), bean.getDateTimeField());
			Assert.assertEquals(roundTripped2.getDateTimeField(), valOutOfBounds);
		}else{
			Assert.assertNotEquals(roundTripped2.getDateTimeField(), bean.getDateTimeField());
			Assert.assertNotEquals(roundTripped2.getDateTimeField(), valOutOfBounds);
		}
		/* LocalDateTime.of can set the value of nanoseconds in a range from 0 to 999,999,999
		MySql.DateTime cannot handle this level of granularity and will truncate the fractional second value
		to 3 digits so the value of the LocalDateTime retrieved from the database will not be equal to the
		LocalDateTime saved */
		LocalDateTime localDateTimeWithNano = LocalDateTime.of(2015, 12, 24, 2, 3, 4, 423060750);
		LocalDateTime localDateTimeTruncated = LocalDateTime.of(2015, 12, 24, 2, 3, 4, 423000000);
		bean.setDateTimeField(localDateTimeWithNano);
		mapNode.put(bean, null);
		ManyFieldBean roundTripped3 = mapNode.get(bean.getKey(), null);
		Assert.assertNotEquals(localDateTimeWithNano, localDateTimeTruncated);
		if(isMemory()){
			Assert.assertEquals(roundTripped3.getDateTimeField(), localDateTimeWithNano);
		}else{
			Assert.assertEquals(roundTripped3.getDateTimeField(), localDateTimeTruncated);
		}

		recordKey(bean.getKey());
	}

	@Test
	public void testCharacter(){
		ManyFieldBean bean = new ManyFieldBean();
		Character charQ = 'Q';
		bean.setCharacterField(charQ);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped.getCharacterField(), bean.getCharacterField());
		Assert.assertEquals(roundTripped.getCharacterField(), charQ);
		recordKey(bean.getKey());
	}

	@Test
	public void testString(){
		ManyFieldBean bean = new ManyFieldBean();
		String multiByteUtf8Char = "😀";
		String val = "abcdef" + multiByteUtf8Char;
		bean.setStringField(val);
		bean.setStringByteField(StringByteTool.getByteArray(val, StringByteTool.CHARSET_UTF8));
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped.getStringField(), bean.getStringField());
		String roundTrippedByteString = new String(roundTripped.getStringByteField(), StringByteTool.CHARSET_UTF8);
		Assert.assertEquals(val, roundTrippedByteString);
		recordKey(bean.getKey());
	}


	@Test
	public void testByteArray(){
		ManyFieldBean bean = new ManyFieldBean();
	//	byte[] value = new byte[]{ 0x1,0x5,-0x8,0x7f,0x25,0x6a,-0x80,-0x12	};
		byte[] value = new byte[]{1, 5, -128, 127, 25, 66, -80, -12};

		bean.setByteArrayField(value);
		mapNode.put(bean, null);
		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped.getByteArrayField(), value);
		recordKey(bean.getKey());
	}

	@Test
	public void testVarInt(){

		ManyFieldBean bean0 = new ManyFieldBean();
		bean0.setVarIntField(0);
		mapNode.put(bean0, null);

		ManyFieldBean roundTripped0 = mapNode.get(bean0.getKey(), null);
		if(isMemory()){
			Assert.assertSame(roundTripped0, bean0);
		}else{
			Assert.assertNotSame(roundTripped0, bean0);
		}
		Assert.assertEquals(roundTripped0.getVarIntField(), bean0.getVarIntField());
		recordKey(bean0.getKey());

		//1234567
		ManyFieldBean bean1234567 = new ManyFieldBean();
		bean1234567.setVarIntField(1234567);
		mapNode.put(bean1234567, null);

		ManyFieldBean roundTripped1234567 = mapNode.get(bean1234567.getKey(), null);
		if(isMemory()){
			Assert.assertSame(roundTripped1234567, bean1234567);
		}else{
			Assert.assertNotSame(roundTripped1234567, bean1234567);
		}
		Assert.assertEquals(roundTripped1234567.getVarIntField(), bean1234567.getVarIntField());
		recordKey(bean1234567.getKey());

		//Integer.MAX_VALUE
		ManyFieldBean beanMax = new ManyFieldBean();
		beanMax.setVarIntField(Integer.MAX_VALUE);
		mapNode.put(beanMax, null);

		ManyFieldBean roundTrippedMax = mapNode.get(beanMax.getKey(), null);
		if(isMemory()){
			Assert.assertSame(roundTrippedMax, beanMax);
		}else{
			Assert.assertNotSame(roundTrippedMax, beanMax);
		}
		Assert.assertEquals(roundTrippedMax.getVarIntField(), beanMax.getVarIntField());
		recordKey(beanMax.getKey());
	}

	@Test
	public void testIntegerEnum(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setIntEnumField(TestEnum.beast);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped.getIntEnumField(), bean.getIntEnumField());
		Assert.assertTrue(TestEnum.beast == roundTripped.getIntEnumField());
		recordKey(bean.getKey());
	}

	@Test
	public void testVarIntEnum(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setVarIntEnumField(TestEnum.fish);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped.getVarIntEnumField(), bean.getVarIntEnumField());
		Assert.assertTrue(TestEnum.fish == roundTripped.getVarIntEnumField());
		recordKey(bean.getKey());
	}

	@Test
	public void testStringEnum(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setStringEnumField(TestEnum.cat);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped.getStringEnumField(), bean.getStringEnumField());
		Assert.assertTrue(TestEnum.cat == roundTripped.getStringEnumField());
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
		ArrayAsserts.assertArrayEquals(LongByteTool.fromComparableByteArray(roundTripped.getData()),
				DrArrayTool.primitiveLongArray(ids));
		recordKey(bean.getKey());
	}

	@Test
	public void testUInt31(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.setIntegerField(7888);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertEquals(roundTripped.getIntegerField(), bean.getIntegerField());
		Assert.assertTrue(7888 == roundTripped.getIntegerField());
		recordKey(bean.getKey());
	}

	@Test
	public void testLongArray(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.appendToLongArrayField(Long.MAX_VALUE);
		bean.appendToLongArrayField(Integer.MAX_VALUE);
		bean.appendToLongArrayField(Short.MAX_VALUE);
		bean.appendToLongArrayField(Byte.MAX_VALUE);
		bean.appendToLongArrayField(5);
		bean.appendToLongArrayField(0);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertTrue(0 == DrListTool.compare(bean.getLongArrayField(), roundTripped.getLongArrayField()));
		recordKey(bean.getKey());
	}

	@Test
	public void testBooleanArray(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.appendToBooleanArrayField(true);
		bean.appendToBooleanArrayField(null);
		bean.appendToBooleanArrayField(false);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertTrue(0 == DrListTool.compare(bean.getBooleanArrayField(), roundTripped
				.getBooleanArrayField()));
		recordKey(bean.getKey());
	}

	@Test
	public void testIntegerArray(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.appendToIntegerArrayField(Integer.MAX_VALUE);
		bean.appendToIntegerArrayField(null);
		bean.appendToIntegerArrayField(-5029);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertTrue(0 == DrListTool.compare(bean.getIntegerArrayField(), roundTripped
				.getIntegerArrayField()));
		recordKey(bean.getKey());
	}

	@Test
	public void testDoubleArray(){
		ManyFieldBean bean = new ManyFieldBean();
		bean.appendToDoubleArrayField(Double.MAX_VALUE);
		bean.appendToDoubleArrayField(null);
		bean.appendToDoubleArrayField(null);
		bean.appendToDoubleArrayField(Double.MIN_VALUE);
		bean.appendToDoubleArrayField(-5029.02939);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		Assert.assertTrue(0 == DrListTool.compare(bean.getDoubleArrayField(), roundTripped.getDoubleArrayField()));
		recordKey(bean.getKey());
	}

	@Test
	public void testDelimitedStringArray(){
		ManyFieldBean bean = new ManyFieldBean();
		List<String> strings = DrListTool.create("abc hi!", "xxx's", "bb_3");
		bean.setDelimitedStringArrayField(strings);
		mapNode.put(bean, null);

		ManyFieldBean roundTripped = mapNode.get(bean.getKey(), null);
		ArrayAsserts.assertArrayEquals(roundTripped.getDelimitedStringArrayField().toArray(), strings.toArray());
		recordKey(bean.getKey());
	}

	/************************* helpers ********************************************/

	protected void recordKey(ManyFieldBeanKey key){
		allKeys.add(key);
	}
}
