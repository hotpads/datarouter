/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.storage.test.node.basic.manyfield;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.test.node.basic.manyfield.ManyFieldBean.ManyFieldTypeBeanFielder;

public abstract class BaseManyFieldIntegrationTests{

	@Inject
	private Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;

	protected DatarouterManyFieldTestDao dao;

	public void setup(ClientId clientId, Supplier<ManyFieldTypeBeanFielder> fielderSupplier){
		this.dao = new DatarouterManyFieldTestDao(datarouter, nodeFactory, clientId, fielderSupplier);
		resetTable();
	}

	private void resetTable(){
		try{
			dao.deleteAll();
		}catch(UnsupportedOperationException e){
			// some storage can't do that
		}
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	@Test
	public void testDelete(){
		var bean = new ManyFieldBean();
		bean.setShortField((short)12);
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped, bean);

		dao.delete(bean.getKey());
		Assert.assertNull(dao.get(bean.getKey()));
	}

	@Test
	public void testBoolean(){
		var bean = new ManyFieldBean();

		//test true value
		bean.setBooleanField(true);
		dao.put(bean);
		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertNotSame(roundTripped, bean);
		Assert.assertEquals(roundTripped.getBooleanField(), bean.getBooleanField());

		//test false value
		bean.setBooleanField(false);
		dao.put(bean);
		ManyFieldBean roundTrippedFalse = dao.get(bean.getKey());
		Assert.assertNotSame(roundTrippedFalse, bean);
		Assert.assertEquals(roundTrippedFalse.getBooleanField(), bean.getBooleanField());

		//test null value
		bean.setBooleanField(null);
		dao.put(bean);
		ManyFieldBean roundTrippedNull = dao.get(bean.getKey());
		Assert.assertNull(roundTrippedNull.getBooleanField());
	}

	@Test
	public void testByte(){
		var bean = new ManyFieldBean();
		bean.setByteField((byte)-57);
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertNotSame(roundTripped, bean);
		Assert.assertEquals(roundTripped.getByteField(), bean.getByteField());
	}

	@Test
	public void testShort(){
		var bean = new ManyFieldBean();
		bean.setShortField((short)-57);
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertNotSame(roundTripped, bean);
		Assert.assertEquals(roundTripped.getShortField(), bean.getShortField());
	}

	@Test
	public void testInteger(){
		var bean = new ManyFieldBean();
		bean.setIntegerField(-100057);
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getIntegerField(), bean.getIntegerField());

		bean.setIntegerField(12345);
		dao.put(bean);
		roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getIntegerField(), bean.getIntegerField());

		bean.setIntegerField(-77);
		dao.put(bean);
		roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getIntegerField(), bean.getIntegerField());
		Assert.assertEquals(roundTripped.getIntegerField().intValue(), -77);
	}

	@Test
	public void testLong(){
		var bean = new ManyFieldBean();
		long negative6Billion = 3 * (long)Integer.MIN_VALUE;
		bean.setLongField(negative6Billion);
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getLongField(), bean.getLongField());
		Assert.assertTrue(negative6Billion == roundTripped.getLongField());
	}

	@Test
	public void testFloat(){
		var bean = new ManyFieldBean();
		float val = -157.34f;
		bean.setFloatField(val);
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertNotNull(roundTripped);
		Assert.assertEquals(roundTripped.getFloatField(), bean.getFloatField());
		Assert.assertTrue(val == roundTripped.getFloatField());
	}

	@Test
	public void testNullPrimitive(){
		var bean = new ManyFieldBean();
		Float val = null;
		bean.setFloatField(val);
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getFloatField(), bean.getFloatField());
		Assert.assertEquals(roundTripped.getFloatField(), val);
	}

	@Test
	public void testDouble(){
		var bean = new ManyFieldBean();
		double val = -100057.3456f;
		bean.setDoubleField(val);
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getDoubleField(), bean.getDoubleField());
		Assert.assertTrue(val == roundTripped.getDoubleField());
	}

	@Test
	public void testLongDate(){
		var bean = new ManyFieldBean();
		Date val = new Date();
		bean.setLongDateField(val);
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getLongDateField(), bean.getLongDateField());
		Assert.assertTrue(val.equals(roundTripped.getLongDateField()));
	}

	@Test
	public void testLocalDate(){
		var bean = new ManyFieldBean();
		LocalDate val = LocalDate.now();
		bean.setLocalDateField(val);
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getLocalDateField(), bean.getLocalDateField());
		Assert.assertTrue(val.equals(roundTripped.getLocalDateField()));
	}

	@Test
	public void testLocalDateTime(){
		var bean = new ManyFieldBean();
		// LocalDateTime.now() uses the system clock as default so it will always get fractional seconds up to 3 digits
		// (i.e. milliseconds) and no more.
		LocalDateTime val = LocalDateTime.now();
		bean.setDateTimeField(val);
		dao.put(bean);
		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getDateTimeField(), bean.getDateTimeField());
		Assert.assertEquals(roundTripped.getDateTimeField(), val);

		// LocalDateTime.of can set the value of nanoseconds in a range from 0 to 999,999,999
		// MySql.DateTime cannot handle this level of granularity and will truncate the fractional second value
		// so the value of the LocalDateTime retrieved from the database will not be equal to the LocalDateTime saved
		LocalDateTime valOutOfBounds = LocalDateTime.of(2015, 12, 24, 2, 3, 4, 50);
		bean.setDateTimeField(valOutOfBounds);
		dao.put(bean);
		ManyFieldBean roundTripped2 = dao.get(bean.getKey());
		Assert.assertNotEquals(roundTripped2.getDateTimeField(), bean.getDateTimeField());
		Assert.assertNotEquals(roundTripped2.getDateTimeField(), valOutOfBounds);
		/* LocalDateTime.of can set the value of nanoseconds in a range from 0 to 999,999,999
		MySql.DateTime cannot handle this level of granularity and will truncate the fractional second value
		to 3 digits so the value of the LocalDateTime retrieved from the database will not be equal to the
		LocalDateTime saved */
		LocalDateTime localDateTimeWithNano = LocalDateTime.of(2015, 12, 24, 2, 3, 4, 423060750);
		LocalDateTime localDateTimeTruncated = LocalDateTime.of(2015, 12, 24, 2, 3, 4, 423060000);
		bean.setDateTimeField(localDateTimeWithNano);
		dao.put(bean);
		ManyFieldBean roundTripped3 = dao.get(bean.getKey());
		Assert.assertNotEquals(localDateTimeWithNano, localDateTimeTruncated);
		Assert.assertEquals(roundTripped3.getDateTimeField(), localDateTimeTruncated);
	}

	@Test
	public void testInstant(){
		Instant instant = Instant.now();
		var bean = new ManyFieldBean();
		bean.setInstantField(instant);
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getInstantField(), bean.getInstantField());
		Assert.assertEquals(roundTripped.getInstantField(), instant);
	}

	@Test
	public void testString(){
		var bean = new ManyFieldBean();
		String multiByteUtf8Char = "😀";
		String val = "abcdef" + multiByteUtf8Char;
		bean.setStringField(val);
		bean.setStringByteField(val.getBytes(StandardCharsets.UTF_8));
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getStringField(), bean.getStringField());
		String roundTrippedByteString = new String(roundTripped.getStringByteField(), StandardCharsets.UTF_8);
		Assert.assertEquals(val, roundTrippedByteString);
	}


	@Test
	public void testByteArray(){
		var bean = new ManyFieldBean();
		byte[] value = new byte[]{1, 5, -128, 127, 25, 66, -80, -12};

		bean.setByteArrayField(value);
		dao.put(bean);
		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getByteArrayField(), value);
	}

	@Test
	public void testIntegerEnum(){
		var bean = new ManyFieldBean();
		bean.setIntEnumField(TestEnum.beast);
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getIntEnumField(), bean.getIntEnumField());
		Assert.assertEquals(roundTripped.getIntEnumField(), TestEnum.beast);
	}

	@Test
	public void testStringEnum(){
		var bean = new ManyFieldBean();
		bean.setStringEnumField(TestEnum.cat);
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getStringEnumField(), bean.getStringEnumField());
		Assert.assertEquals(roundTripped.getStringEnumField(), TestEnum.cat);
	}

	@Test
	public void testBlob(){
		byte[] bytes = "This string is encoded as bytes.".getBytes();
		var bean = new ManyFieldBean();
		bean.setData(bytes);
		dao.put(bean);
		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getData(), bytes);
	}

	@Test
	public void testLongArray(){
		var bean = new ManyFieldBean();
		bean.appendToLongArrayField(Long.MAX_VALUE);
		bean.appendToLongArrayField(Integer.MAX_VALUE);
		bean.appendToLongArrayField(Short.MAX_VALUE);
		bean.appendToLongArrayField(Byte.MAX_VALUE);
		bean.appendToLongArrayField(5);
		bean.appendToLongArrayField(0);
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getLongArrayField(), bean.getLongArrayField());

		roundTripped.appendToLongArrayField(3);//assert mutability of returned list
	}

	@Test
	public void testBooleanArray(){
		var bean = new ManyFieldBean();
		bean.appendToBooleanArrayField(true);
		bean.appendToBooleanArrayField(null);
		bean.appendToBooleanArrayField(false);
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getBooleanArrayField(), bean.getBooleanArrayField());

		roundTripped.appendToBooleanArrayField(true);//assert mutability of returned list
	}

	@Test
	public void testIntegerArray(){
		var bean = new ManyFieldBean();
		bean.appendToIntegerArrayField(Integer.MAX_VALUE);
		bean.appendToIntegerArrayField(null);
		bean.appendToIntegerArrayField(-5029);
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getIntegerArrayField(), bean.getIntegerArrayField());

		roundTripped.appendToIntegerArrayField(3);//assert mutability of returned list
	}

	@Test
	public void testDoubleArray(){
		var bean = new ManyFieldBean();
		bean.appendToDoubleArrayField(Double.MAX_VALUE);
		bean.appendToDoubleArrayField(null);
		bean.appendToDoubleArrayField(null);
		bean.appendToDoubleArrayField(Double.MIN_VALUE);
		bean.appendToDoubleArrayField(-5029.02939);
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getDoubleArrayField(), bean.getDoubleArrayField());

		roundTripped.appendToDoubleArrayField(3.0);//assert mutability of returned list
	}

	@Test
	public void testDelimitedStringArray(){
		var bean = new ManyFieldBean();
		List<String> strings = List.of("abc hi!", "xxx's", "bb_3");
		bean.setDelimitedStringArrayField(strings);
		dao.put(bean);

		ManyFieldBean roundTripped = dao.get(bean.getKey());
		Assert.assertEquals(roundTripped.getDelimitedStringArrayField().toArray(), strings.toArray());

		roundTripped.appendToDelimitedStringArrayField("later");//assert mutability of returned list
	}

}
