package com.hotpads.datarouter.storage.field.imp.custom;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldKey;
import com.hotpads.util.core.bytes.IntegerByteTool;

/*  LocalDateTime stores the value of nanoseconds in a range from 0 to 999,999,999 However, the MySql.DateTime column
 *  type cannot handle this level of granularity (it can handle at most 6 digits of fractional seconds).
 *  LocalDateTimeField enforces a truncation of the LocalDateTime nanosecond value of up to
 *  6 fractional seconds (microseconds) with a default of 3 fractional seconds (milliseconds) as this corresponds
 *  to the fractional seconds granularity of System.currentTimeMillis() and LocalDateTime.now().
 *  This is the recommended use.
 *  A LocalDateTime object created using LocalDateTime::of might not be equivalent to a LocalDateTime retrieved from
 *  MySql because of the truncation of nanoseconds */

public class LocalDateTimeField extends BaseField<LocalDateTime>{

	public static final String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final int TOTAL_NUM_FRACTIONAL_SECONDS = 9;
	public static final int BACKWARDS_COMPATIBLE_NUM_FRACTIONAL_SECONDS = 3;
	private static final int numBytes = 28;
	public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

	private final LocalDateTimeFieldKey key;
	private final int numFractionalSeconds;

	public LocalDateTimeField(LocalDateTimeFieldKey key, LocalDateTime value){
		this(null, key, value);
	}

	public LocalDateTimeField(String prefix, LocalDateTimeFieldKey key, LocalDateTime value){
		super(prefix, value);
		this.key = key;
		this.numFractionalSeconds = BACKWARDS_COMPATIBLE_NUM_FRACTIONAL_SECONDS;
		this.setValue(getTruncatedLocalDateTime(value));
	}

	public int getNumFractionalSeconds(){
		return numFractionalSeconds;
	}

	public LocalDateTime getTruncatedLocalDateTime(LocalDateTime value){
		if(value == null){
			return null;
		}
		int divideBy = (int) Math.pow(10, TOTAL_NUM_FRACTIONAL_SECONDS - getNumFractionalSeconds());
		int numNanoSeconds = (value.getNano()/divideBy) * divideBy;
		return value.withNano(numNanoSeconds);
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
		return value.format(formatter);
	}

	@Override
	public LocalDateTime parseStringEncodedValueButDoNotSet(String value){
		LocalDateTime dateTime = LocalDateTime.parse(value, formatter);
		return dateTime;
	}

	@Override
	public byte[] getBytes(){
		if(value == null){
			return null;
		}
		byte[] bytes = new byte[numBytes];
		int offset = 0;
		offset += IntegerByteTool.toRawBytes(value.getYear(), bytes, offset);
		offset += IntegerByteTool.toRawBytes(value.getMonthValue(), bytes, offset);
		offset += IntegerByteTool.toRawBytes(value.getDayOfMonth(), bytes, offset);
		offset += IntegerByteTool.toRawBytes(value.getHour(), bytes, offset);
		offset += IntegerByteTool.toRawBytes(value.getMinute(), bytes, offset);
		offset += IntegerByteTool.toRawBytes(value.getSecond(), bytes, offset);
		offset += IntegerByteTool.toRawBytes(value.getNano(), bytes, offset);
		return bytes;
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return numBytes;
	}

	@Override
	public LocalDateTime fromBytesButDoNotSet(byte[] bytes, int offset){
		int year = IntegerByteTool.fromRawBytes(bytes, offset);
		offset += 4;
		int month = IntegerByteTool.fromRawBytes(bytes, offset);
		offset += 4;
		int day = IntegerByteTool.fromRawBytes(bytes, offset);
		offset += 4;
		int hour = IntegerByteTool.fromRawBytes(bytes, offset);
		offset += 4;
		int minute = IntegerByteTool.fromRawBytes(bytes, offset);
		offset += 4;
		int second = IntegerByteTool.fromRawBytes(bytes, offset);
		offset += 4;
		int nano = IntegerByteTool.fromRawBytes(bytes, offset);
		return LocalDateTime.of(year, month, day, hour, minute, second, nano);
	}

	public static class DateFieldTester{
		@Test
		public void testParseStringEncodedValueButDoNotSet(){
			String dateStr = "2016-06-22 19:20:14.100";
			String dateStr2 = "2015-06-22 00:00:00.200";
			LocalDateTimeField field = new LocalDateTimeField(new LocalDateTimeFieldKey("test"), null);
			ZonedDateTime date = field.parseStringEncodedValueButDoNotSet(dateStr).atZone(ZoneId.systemDefault());
			Assert.assertEquals(date.toInstant().toEpochMilli(), 1466648414100L);
			ZonedDateTime date2 = field.parseStringEncodedValueButDoNotSet(dateStr2).atZone(ZoneId.systemDefault());
			Assert.assertEquals(date2.toInstant().toEpochMilli(), 1434956400200L);
			Assert.assertNotNull(date);
		}

		@Test
		public void testParseStringEncodedValueButDoNotSetWithTimeZone(){
			String dateStrEst = "2016-06-22 19:20:14.100"; //EST
			String dateStrPst = "2016-06-22 16:20:14.100"; //PST
			LocalDateTimeField field = new LocalDateTimeField(new LocalDateTimeFieldKey("test"), null);
			ZonedDateTime zonedDateTimeEst = field.parseStringEncodedValueButDoNotSet(dateStrEst)
					.atZone(ZoneId.of("America/New_York"));
			ZonedDateTime zonedDateTimePst = field.parseStringEncodedValueButDoNotSet(dateStrPst)
					.atZone(ZoneId.of("America/Los_Angeles"));
			Assert.assertEquals(zonedDateTimeEst.toInstant(), zonedDateTimePst.toInstant());
			Assert.assertEquals(zonedDateTimePst.toInstant().toEpochMilli(), 1466637614100L);
			Assert.assertEquals(zonedDateTimeEst.toInstant().toEpochMilli(), 1466637614100L);
		}

		@Test
		public void testfromBytesButDoNotSet(){
			String dateStr1 = "2016-06-22 19:20:14.100123456";
			String dateStr2 = "2014-06-24 03:20:14.210998531";
			DateTimeFormatter formatterWithNano = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");;
			LocalDateTime localDateTime1 = LocalDateTime.parse(dateStr1, formatterWithNano);
			LocalDateTime localDateTime2 = LocalDateTime.parse(dateStr2, formatterWithNano);
			LocalDateTimeField field1 = new LocalDateTimeField(new LocalDateTimeFieldKey("test"), localDateTime1);
			LocalDateTimeField field2 = new LocalDateTimeField(new LocalDateTimeFieldKey("test"), localDateTime2);
			LocalDateTime localDateTimeFromBytes1 = field1.fromBytesButDoNotSet(field1.getBytes(), 0);
			LocalDateTime localDateTimeFromBytes2 = field2.fromBytesButDoNotSet(field2.getBytes(), 0);
			Assert.assertEquals(field1.getTruncatedLocalDateTime(localDateTime1), localDateTimeFromBytes1);
			Assert.assertEquals(field2.getTruncatedLocalDateTime(localDateTime2), localDateTimeFromBytes2);
		}
	}

}
