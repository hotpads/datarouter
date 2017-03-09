package com.hotpads.datarouter.storage.field.imp;

import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.LongByteTool;

public class DateField extends BasePrimitiveField<Date>{
	public static final int
		BACKWARDS_COMPATIBLE_NUM_DECIMAL_SECONDS = 0,
		DEFAULT_DECIMAL_SECONDS = 3;//match java's millisecond precision

	private final int numDecimalSeconds;

	/**
	 * Defines a date field with milliseconds precision
	 */
	public static DateField createWithMillis(DateFieldKey key, Date value){
		return new DateField(null, key, value, DEFAULT_DECIMAL_SECONDS);
	}

	/**
	 * Defines a date field with seconds precision
	 */
	public DateField(DateFieldKey key, Date value){
		this(null, key, value);
	}

	public DateField(String prefix, DateFieldKey key, Date value){
		super(prefix, key, value);
		this.numDecimalSeconds = BACKWARDS_COMPATIBLE_NUM_DECIMAL_SECONDS;
	}

	@Deprecated
	public DateField(String name, Date value){
		this(null, name, value);
	}

	@Deprecated
	public DateField(String prefix, String name, Date value){
		this(prefix, new DateFieldKey(name), value);
	}

	private DateField(String prefix, DateFieldKey key, Date value, int numDecimalSeconds){
		super(prefix, key, value);
		this.numDecimalSeconds = numDecimalSeconds;
	}

	public int getNumDecimalSeconds(){
		return numDecimalSeconds;
	}

	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return DrDateTool.getInternetDate(value);
	}

	@Override
	public Date parseStringEncodedValueButDoNotSet(String str){
		if(DrStringTool.isEmpty(str) || "null".equals(str)){
			return null;
		}
		return DrDateTool.parseUserInputDate(str,null);
	}

	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		if(value == null){
			return null;
		}
		return LongByteTool.getUInt63Bytes(value.getTime());
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 8;
	}

	@Override
	public Date fromBytesButDoNotSet(byte[] bytes, int offset){
		return new Date(LongByteTool.fromUInt63Bytes(bytes, offset));
	}

	public static class DateFieldTester{
		@Test
		public void testParseStringEncodedValueButDoNotSet(){
			String dateStr = "2016-06-22T19:20:14Z";
			String dateStr2 = "Mon Jun 22 00:00:00 PDT 2015";
			DateField field = new DateField("test", null);
			Date date = field.parseStringEncodedValueButDoNotSet(dateStr);
			Assert.assertEquals(date.getTime(), 1466648414000L);
			Date date2 = field.parseStringEncodedValueButDoNotSet(dateStr2);
			Assert.assertEquals(date2.getTime(), 1434956400000L);
			Assert.assertNotNull(date);
		}
	}
}
