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
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.IntegerByteTool;
import com.hotpads.util.core.bytes.ShortByteTool;

/**
 * LocalDateTime stores the value of nanoseconds in a range from 0 to 999,999,999.
 *  However, the MySql.DateTime column type cannot handle this level of granularity
 *  (it can handle at most 6 digits of fractional seconds).
 *
 *  LocalDateTimeField defaults to a truncation of the LocalDateTime nanosecond value of up to 3 fractional seconds
 *  as this corresponds to the fractional seconds granularity of System.currentTimeMillis() and LocalDateTime.now().
 *  This is the recommended use, but can be overridden to store the full value.
 *
 *  A LocalDateTime object created using LocalDateTime::of might not be equivalent to a LocalDateTime retrieved from
 *  MySql because of the truncation of nanoseconds. Use .now() or use the LocalDateTime::of method that
 *  ignores the nanoseconds field
 *  */

public class LocalDateTimeField extends BaseField<LocalDateTime>{

	private static final int NUM_BYTES = 15;
	private static final int TOTAL_NUM_FRACTIONAL_SECONDS = 9;
	public static final String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

	private final LocalDateTimeFieldKey key;

	public LocalDateTimeField(LocalDateTimeFieldKey key, LocalDateTime value){
		this(null, key, value);
	}

	public LocalDateTimeField(String prefix, LocalDateTimeFieldKey key, LocalDateTime value){
		super(prefix, value);
		this.key = key;
		this.setValue(getTruncatedLocalDateTime(value));
	}

	public LocalDateTime getTruncatedLocalDateTime(LocalDateTime value){
		if(value == null){
			return null;
		}
		int divideBy = (int) Math.pow(10, TOTAL_NUM_FRACTIONAL_SECONDS - getNumFractionalSeconds());
		if(divideBy < 1){
			throw new RuntimeException("numFractionalSeconds is greater or equal to 9");
		}
		int numNanoSeconds = (value.getNano() / divideBy) * divideBy;
		return value.withNano(numNanoSeconds);
	}

	public int getNumFractionalSeconds(){
		return ((LocalDateTimeFieldKey) getKey()).getNumFractionalSeconds();
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
		if(value == null){
			return null;
		}
		return value.format(formatter);
	}

	@Override
	public LocalDateTime parseStringEncodedValueButDoNotSet(String str){
		if(DrStringTool.isEmpty(str) || "null".equals(str)){
			return null;
		}
		LocalDateTime dateTime = LocalDateTime.parse(str, formatter);
		return dateTime;
	}

	@Override
	public byte[] getBytes(){
		if(value == null){
			return null;
		}
		byte[] bytes = new byte[NUM_BYTES];
		int offset = 0;
		offset += IntegerByteTool.toComparableBytes(value.getYear(), bytes, offset);
		offset += ShortByteTool.toComparableBytes((short) value.getMonthValue(), bytes, offset);
		offset += ShortByteTool.toComparableBytes((short) value.getDayOfMonth(), bytes, offset);
		bytes[offset++] = (byte) value.getHour();
		bytes[offset++] = (byte) value.getMinute();
		bytes[offset++] = (byte) value.getSecond();
		offset += IntegerByteTool.toComparableBytes(value.getNano(), bytes, offset);
		return bytes;
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return NUM_BYTES;
	}

	@Override
	public LocalDateTime fromBytesButDoNotSet(byte[] bytes, int offset){
		int year = IntegerByteTool.fromComparableBytes(bytes, offset);
		offset += 4;
		int month = ShortByteTool.fromComparableBytes(bytes, offset);
		offset += 2;
		int day = ShortByteTool.fromComparableBytes(bytes, offset);
		offset += 2;
		int hour = bytes[offset++];
		int minute = bytes[offset++];
		int second = bytes[offset++];
		int nano = IntegerByteTool.fromComparableBytes(bytes, offset);
		return LocalDateTime.of(year, month, day, hour, minute, second, nano);
	}

	public static class LocalDateFieldTester{
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

		@Test
		public void testGetTruncatedLocalDateTime(){
			String dateStr = "2014-06-24 03:20:14.210998531";
			DateTimeFormatter formatterWithNano = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
			LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatterWithNano);
			LocalDateTime localDateTimeTest = LocalDateTime.parse(dateStr, formatterWithNano);
			LocalDateTimeField field1 = new LocalDateTimeField(new LocalDateTimeFieldKey("test")
					.overrideNumFractionalSeconds(3), localDateTime);
			LocalDateTimeField field2 = new LocalDateTimeField(new LocalDateTimeFieldKey("test")
					.overrideNumFractionalSeconds(7), localDateTime);
			LocalDateTimeField field3 = new LocalDateTimeField(new LocalDateTimeFieldKey("test")
					.overrideNumFractionalSeconds(0), localDateTime);
			LocalDateTimeField field4 = new LocalDateTimeField(new LocalDateTimeFieldKey("test")
					.overrideNumFractionalSeconds(9), localDateTime);
			Assert.assertEquals(field1.getTruncatedLocalDateTime(localDateTime), localDateTimeTest.withNano(210000000));
			Assert.assertEquals(field2.getTruncatedLocalDateTime(localDateTime), localDateTimeTest.withNano(210998500));
			Assert.assertEquals(field3.getTruncatedLocalDateTime(localDateTime), localDateTimeTest.withNano(0));
			Assert.assertEquals(field4.getTruncatedLocalDateTime(localDateTime), localDateTimeTest.withNano(210998531));
		}
	}

}
