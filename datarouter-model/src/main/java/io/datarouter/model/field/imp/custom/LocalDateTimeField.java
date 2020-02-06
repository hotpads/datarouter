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
package io.datarouter.model.field.imp.custom;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.datarouter.model.field.BasePrimitiveField;
import io.datarouter.util.bytes.IntegerByteTool;
import io.datarouter.util.bytes.ShortByteTool;
import io.datarouter.util.string.StringTool;

/**
 * LocalDateTime stores the value of nanoseconds in a range from 0 to 999,999,999.
 * However, the MySQL DATETIME column type cannot handle this level of granularity
 * (it can handle at most 6 digits of fractional seconds).
 *
 * LocalDateTimeField defaults to a truncation of the LocalDateTime nanosecond value of up to 3 fractional seconds
 * as this corresponds to the fractional seconds granularity of System.currentTimeMillis() and LocalDateTime.now().
 * This is the recommended use, but can be overridden to store the full value.
 *
 * A LocalDateTime object created using LocalDateTime::of might not be equivalent to a LocalDateTime retrieved from
 * MySQL because of the truncation of nanoseconds. Use .now() or use the LocalDateTime::of method that
 * ignores the nanoseconds field
 *
 */
public class LocalDateTimeField extends BasePrimitiveField<LocalDateTime,LocalDateTimeFieldKey>{

	private static final int NUM_BYTES = 15;
	private static final int TOTAL_NUM_FRACTIONAL_SECONDS = 9;
	public static final String pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS";
	public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

	public LocalDateTimeField(LocalDateTimeFieldKey key, LocalDateTime value){
		this(null, key, value);
	}

	public LocalDateTimeField(String prefix, LocalDateTimeFieldKey key, LocalDateTime value){
		super(prefix, key, value);
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
		int numNanoSeconds = value.getNano() / divideBy * divideBy;
		return value.withNano(numNanoSeconds);
	}

	public int getNumFractionalSeconds(){
		return ((LocalDateTimeFieldKey) getKey()).getNumFractionalSeconds();
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
		if(StringTool.isNullOrEmpty(str)){
			return null;
		}
		return LocalDateTime.parse(str, formatter);
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

}
