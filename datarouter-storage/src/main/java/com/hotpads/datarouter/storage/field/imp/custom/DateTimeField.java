package com.hotpads.datarouter.storage.field.imp.custom;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldKey;
import com.hotpads.util.core.bytes.LongByteTool;

public class DateTimeField extends BaseField<LocalDateTime>{

	public static final String pattern = "yyyy-MM-dd HH:mm:ss";
	public static final int BACKWARDS_COMPATIBLE_NUM_DECIMAL_SECONDS = 0;

	private final DateTimeFieldKey key;
	private final int numDecimalSeconds;

	public DateTimeField(DateTimeFieldKey key, LocalDateTime value){
		this(null, key, value);
	}

	public DateTimeField(String prefix, DateTimeFieldKey key, LocalDateTime value){
		super(prefix, value);
		this.key = key;
		this.numDecimalSeconds = BACKWARDS_COMPATIBLE_NUM_DECIMAL_SECONDS;
	}

	public Integer getNumDecimalSeconds(){
		return numDecimalSeconds;
	}

	@Override
	public FieldKey<LocalDateTime> getKey(){
		return key;
	}

	@Override
	public String getValueString(){
		return value.toString();
	}

	@Override
	public int compareTo(Field<LocalDateTime> other){
		if(other == null){
			return -1;
		}
		return value.compareTo(other.getValue());
	}

	@Override
	public String getStringEncodedValue(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		return value.format(formatter);
	}

	@Override
	public LocalDateTime parseStringEncodedValueButDoNotSet(String value){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		LocalDateTime dateTime = LocalDateTime.parse(value, formatter);
		return dateTime;
	}

	@Override
	public byte[] getBytes(){
		if(value == null){
			return null;
		}
		return LongByteTool.getUInt63Bytes(value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 8;
	}

	@Override
	public LocalDateTime fromBytesButDoNotSet(byte[] bytes, int offset){
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(LongByteTool.fromUInt63Bytes(bytes, offset)),
				ZoneId.systemDefault());
	}

	public static class DateFieldTester{
		@Test
		public void testParseStringEncodedValueButDoNotSet(){
			String dateStr = "2016-06-22 19:20:14";
			String dateStr2 = "2015-06-22 00:00:00";
			DateTimeField field = new DateTimeField(new DateTimeFieldKey("test"), null);
			ZonedDateTime date = field.parseStringEncodedValueButDoNotSet(dateStr).atZone(ZoneId.systemDefault());
			Assert.assertEquals(date.toInstant().toEpochMilli(), 1466648414000L);
			ZonedDateTime date2 = field.parseStringEncodedValueButDoNotSet(dateStr2).atZone(ZoneId.systemDefault());
			Assert.assertEquals(date2.toInstant().toEpochMilli(), 1434956400000L);
			Assert.assertNotNull(date);
		}
	}

}
