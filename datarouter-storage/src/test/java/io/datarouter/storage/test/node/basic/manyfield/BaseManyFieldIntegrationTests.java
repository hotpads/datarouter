/*
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import io.datarouter.model.util.FractionalSecondTool;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.test.node.basic.manyfield.ManyFieldBean.ManyFieldTypeBeanFielder;
import io.datarouter.types.MilliTime;
import jakarta.inject.Inject;

public abstract class BaseManyFieldIntegrationTests{

	@Inject
	private Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;

	protected DatarouterManyFieldTestDao dao;

	public void setup(ClientId clientId, Supplier<ManyFieldTypeBeanFielder> fielderSupplier,
			Optional<String> tableName){
		this.dao = new DatarouterManyFieldTestDao(datarouter, nodeFactory, clientId, fielderSupplier, tableName);
		resetTable();
	}

	private void resetTable(){
		try{
			dao.deleteAll();
		}catch(UnsupportedOperationException e){
			// some storage can't do that
		}
	}

	private ManyFieldBean putAndGet(ManyFieldBean bean){
		dao.put(bean);
		return dao.get(bean.getKey());
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	@Test
	public void testDelete(){
		var bean = new ManyFieldBean();
		bean.setShortField((short)12);

		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertEquals(roundTripped, bean);

		dao.delete(bean.getKey());
		Assert.assertNull(dao.get(bean.getKey()));
	}

	@Test
	public void testBoolean(){
		var bean = new ManyFieldBean();

		//test true value
		bean.setBooleanField(true);
		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertNotSame(roundTripped, bean);
		Assert.assertEquals(roundTripped.getBooleanField(), bean.getBooleanField());

		//test false value
		bean.setBooleanField(false);
		ManyFieldBean roundTrippedFalse = putAndGet(bean);
		Assert.assertNotSame(roundTrippedFalse, bean);
		Assert.assertEquals(roundTrippedFalse.getBooleanField(), bean.getBooleanField());

		//test null value
		bean.setBooleanField(null);
		ManyFieldBean roundTrippedNull = putAndGet(bean);
		Assert.assertNull(roundTrippedNull.getBooleanField());
	}

	@Test
	public void testShort(){
		var bean = new ManyFieldBean();
		bean.setShortField((short)-57);

		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertNotSame(roundTripped, bean);
		Assert.assertEquals(roundTripped.getShortField(), bean.getShortField());
	}

	@Test
	public void testInteger(){
		var bean = new ManyFieldBean();
		bean.setIntegerField(-100057);

		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertEquals(roundTripped.getIntegerField(), bean.getIntegerField());

		bean.setIntegerField(12345);
		roundTripped = putAndGet(bean);
		Assert.assertEquals(roundTripped.getIntegerField(), bean.getIntegerField());

		bean.setIntegerField(-77);
		roundTripped = putAndGet(bean);
		Assert.assertEquals(roundTripped.getIntegerField(), bean.getIntegerField());
		Assert.assertEquals(roundTripped.getIntegerField().intValue(), -77);
	}

	@Test
	public void testEnumToInteger(){
		var bean = new ManyFieldBean();
		bean.setEnumToIntegerField(TestEnum.BEAST);

		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertSame(roundTripped.getEnumToIntegerField(), TestEnum.BEAST);
	}

	@Test
	public void testLong(){
		var bean = new ManyFieldBean();
		long negative6Billion = 3 * (long)Integer.MIN_VALUE;
		bean.setLongField(negative6Billion);

		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertEquals(roundTripped.getLongField(), bean.getLongField());
		Assert.assertEquals((long)roundTripped.getLongField(), negative6Billion);
	}

	@Test
	public void testFloat(){
		var bean = new ManyFieldBean();
		float val = -157.34f;
		bean.setFloatField(val);

		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertNotNull(roundTripped);
		Assert.assertEquals(roundTripped.getFloatField(), bean.getFloatField());
		Assert.assertEquals(roundTripped.getFloatField(), val);
	}

	@Test
	public void testNullPrimitive(){
		var bean = new ManyFieldBean();
		Float val = null;
		bean.setFloatField(val);

		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertEquals(roundTripped.getFloatField(), bean.getFloatField());
		Assert.assertEquals(roundTripped.getFloatField(), val);
	}

	@Test
	public void testDouble(){
		var bean = new ManyFieldBean();
		double val = -100057.3456f;
		bean.setDoubleField(val);

		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertEquals(roundTripped.getDoubleField(), bean.getDoubleField());
		Assert.assertEquals(roundTripped.getDoubleField(), val);
	}

	@Test
	public void testMilliTimeToLong(){
		var bean = new ManyFieldBean();
		MilliTime val = MilliTime.now();
		bean.setMilliTimeToLongField(val);

		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertEquals(roundTripped.getMilliTimeToLongField(), bean.getMilliTimeToLongField());
		Assert.assertEquals(val, roundTripped.getMilliTimeToLongField());
	}

	@Test
	public void testMilliTimestamp(){
		var bean = new ManyFieldBean();
		MilliTime val = MilliTime.now();
		bean.setMilliDatetimeField(val);

		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertEquals(roundTripped.getMilliDatetimeField(), bean.getMilliDatetimeField());
		Assert.assertEquals(val, roundTripped.getMilliDatetimeField());
	}

	@Test
	public void testLocalTime(){
		var bean = new ManyFieldBean();
		LocalTime val = LocalTime.now();
		bean.setLocalTimeField(val);

		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertEquals(roundTripped.getLocalTimeField(), bean.getLocalTimeField());
		Assert.assertEquals(roundTripped.getLocalTimeField(), val);
	}

	@Test
	public void testLocalDate(){
		var bean = new ManyFieldBean();
		LocalDate val = LocalDate.now();
		bean.setLocalDateField(val);

		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertEquals(roundTripped.getLocalDateField(), bean.getLocalDateField());
		Assert.assertEquals(roundTripped.getLocalDateField(), val);
	}

	@Test
	public void testLocalDateTime(){
		var bean = new ManyFieldBean();
		// LocalDateTime.now() uses the system clock as default so it will always get fractional seconds up to 3 digits
		// (i.e. milliseconds) and no more.
		LocalDateTime val = LocalDateTime.now();
		LocalDateTime truncatedVal = FractionalSecondTool.truncate(val, ManyFieldBean.FieldKeys.localDateTimeField
				.getNumFractionalSeconds());
		bean.setDateTimeField(val);
		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertEquals(roundTripped.getDateTimeField(), truncatedVal);

		// LocalDateTime.of can set the value of nanoseconds in a range from 0 to 999,999,999
		// MySql.DateTime cannot handle this level of granularity and will truncate the fractional second value
		// so the value of the LocalDateTime retrieved from the database will not be equal to the LocalDateTime saved
		LocalDateTime valOutOfBounds = LocalDateTime.of(2015, 12, 24, 2, 3, 4, 50);
		bean.setDateTimeField(valOutOfBounds);
		ManyFieldBean roundTripped2 = putAndGet(bean);
		Assert.assertNotEquals(roundTripped2.getDateTimeField(), bean.getDateTimeField());
		Assert.assertNotEquals(roundTripped2.getDateTimeField(), valOutOfBounds);
		/* LocalDateTime.of can set the value of nanoseconds in a range from 0 to 999,999,999
		MySql.DateTime cannot handle this level of granularity and will truncate the fractional second value
		to 3 digits so the value of the LocalDateTime retrieved from the database will not be equal to the
		LocalDateTime saved */
		LocalDateTime localDateTimeWithNano = LocalDateTime.of(2015, 12, 24, 2, 3, 4, 423060750);
		LocalDateTime localDateTimeTruncated = LocalDateTime.of(2015, 12, 24, 2, 3, 4, 423060000);
		bean.setDateTimeField(localDateTimeWithNano);
		ManyFieldBean roundTripped3 = putAndGet(bean);
		Assert.assertNotEquals(localDateTimeWithNano, localDateTimeTruncated);
		Assert.assertEquals(roundTripped3.getDateTimeField(), localDateTimeTruncated);
	}

	@Test
	public void testInstant(){
		Instant instant = Instant.now();
		Instant truncatedInstant = FractionalSecondTool.truncate(instant, ManyFieldBean.FieldKeys.instantField
				.getNumFractionalSeconds());
		var bean = new ManyFieldBean();
		bean.setInstantField(instant);

		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertEquals(roundTripped.getInstantField(), truncatedInstant);
	}

	@Test
	public void testString(){
		var bean = new ManyFieldBean();
		String multiByteUtf8Char = "😀";
		String val = "abcdef" + multiByteUtf8Char;
		bean.setStringField(val);
		bean.setStringByteField(val.getBytes(StandardCharsets.UTF_8));

		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertEquals(roundTripped.getStringField(), bean.getStringField());
		String roundTrippedByteString = new String(roundTripped.getStringByteField(), StandardCharsets.UTF_8);
		Assert.assertEquals(val, roundTrippedByteString);
	}

	@Test
	public void testEnumToString(){
		var bean = new ManyFieldBean();
		bean.setEnumToStringField(TestEnum.BEAST);
		Assert.assertSame(putAndGet(bean).getEnumToStringField(), TestEnum.BEAST);
		bean.setEnumToStringField(null);
		Assert.assertSame(putAndGet(bean).getEnumToStringField(), null);
	}


	@Test
	public void testByteArray(){
		var bean = new ManyFieldBean();
		byte[] value = {1, 5, -128, 127, 25, 66, -80, -12};

		bean.setByteArrayField(value);
		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertEquals(roundTripped.getByteArrayField(), value);
	}

	@Test
	public void testIntListToByteArray(){
		var bean = new ManyFieldBean();

		//check null
		bean.setIntListToByteArrayField(null);
		ManyFieldBean rt1 = putAndGet(bean);
		Assert.assertNull(rt1.getIntListToByteArrayField());

		//check empty list differs from null
		bean.setIntListToByteArrayField(List.of());
		ManyFieldBean rt2 = putAndGet(bean);
		Assert.assertEquals(rt2.getIntListToByteArrayField(), new ArrayList<>());

		//check normal values
		List<Integer> values = List.of(-1, 0, 5);
		bean.setIntListToByteArrayField(values);
		ManyFieldBean rt3 = putAndGet(bean);
		Assert.assertEquals(rt3.getIntListToByteArrayField(), values);

		//check return to null
		bean.setIntListToByteArrayField(null);
		ManyFieldBean rt4 = putAndGet(bean);
		Assert.assertNull(rt4.getIntListToByteArrayField());
	}

	@Test
	public void testIntListToLazyByteArray(){
		var bean = new ManyFieldBean();

		//check null databean field (don't initialize it)
		ManyFieldBean rt1 = putAndGet(bean);
		Assert.assertNull(rt1.getIntListToLazyByteArrayField());

		//check null list
		bean.setIntListToLazyByteArrayField(null);
		ManyFieldBean rt2 = putAndGet(bean);
		Assert.assertNull(rt2.getIntListToLazyByteArrayField());

		//check empty list differs from null
		bean.setIntListToLazyByteArrayField(List.of());
		ManyFieldBean rt3 = putAndGet(bean);
		Assert.assertEquals(rt3.getIntListToLazyByteArrayField(), new ArrayList<>());

		//check normal values
		List<Integer> values = List.of(-1, 0, 5);
		bean.setIntListToLazyByteArrayField(values);
		ManyFieldBean rt4 = putAndGet(bean);
		Assert.assertEquals(rt4.getIntListToLazyByteArrayField(), values);

		//check return to null
		bean.setIntListToLazyByteArrayField(null);
		ManyFieldBean rt5 = putAndGet(bean);
		Assert.assertNull(rt5.getIntListToLazyByteArrayField());
	}

	@Test
	public void testBlob(){
		byte[] bytes = "This string is encoded as bytes.".getBytes();
		var bean = new ManyFieldBean();
		bean.setData(bytes);
		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertEquals(roundTripped.getData(), bytes);
	}

	@Test
	public void testCsv(){
		var bean = new ManyFieldBean();
		List<String> strings = List.of("2abc hi!", "x2xx's", "bb2_3");
		bean.setCsvField(strings);

		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertEquals(roundTripped.getCsvField().toArray(), strings.toArray());

		//assert mutability of returned list
		roundTripped.appendToCsvField("later");

		//check for illegal strings
		bean.setCsvBytesField(List.of("good", "b,ad"));
		Assert.assertThrows(IllegalArgumentException.class, () -> putAndGet(bean));
	}

	@Test
	public void testCsvBytes(){
		var bean = new ManyFieldBean();
		List<String> strings = List.of("abc hi!", "xxx's", "bb_3");
		bean.setCsvBytesField(strings);

		ManyFieldBean roundTripped = putAndGet(bean);
		Assert.assertEquals(roundTripped.getCsvBytesField().toArray(), strings.toArray());

		//assert mutability of returned list
		roundTripped.appendToCsvBytesField("later");

		//check for illegal strings
		bean.setCsvBytesField(List.of("good", "b,ad"));
		Assert.assertThrows(IllegalArgumentException.class, () -> putAndGet(bean));
	}

}
