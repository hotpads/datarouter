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
package io.datarouter.model.field.imp.custom;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.datarouter.bytes.codec.intcodec.ComparableIntCodec;
import io.datarouter.bytes.codec.shortcodec.ComparableShortCodec;
import io.datarouter.model.field.BasePrimitiveField;
import io.datarouter.model.util.FractionalSecondTool;
import io.datarouter.util.string.StringTool;

/**
 * LocalDateTime stores the value of nanoseconds in a range from 0 to 999,999,999.
 * However, the MySQL DATETIME column type cannot handle this level of granularity
 * (it can handle at most 6 digits of fractional seconds).
 *
 * LocalDateTimeField defaults to a truncation of the LocalDateTime nanosecond value of up to 6 fractional seconds.
 * This is the recommended use, but can be overridden to store the full value.
 *
 * A LocalDateTime object created using LocalDateTime::of might not be equivalent to a LocalDateTime retrieved from
 * MySQL because of the truncation of nanoseconds. Use .now() or use the LocalDateTime::of method that
 * ignores the nanoseconds field
 *
 */
public class LocalDateTimeField extends BasePrimitiveField<LocalDateTime,LocalDateTimeFieldKey>{

	private static final ComparableIntCodec COMPARABLE_INT_CODEC = ComparableIntCodec.INSTANCE;
	private static final ComparableShortCodec COMPARABLE_SHORT_CODEC = ComparableShortCodec.INSTANCE;
	private static final int NUM_BYTES = 15;
	public static final String pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS";
	public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

	public LocalDateTimeField(LocalDateTimeFieldKey key, LocalDateTime value){
		this(null, key, value);
	}

	public LocalDateTimeField(String prefix, LocalDateTimeFieldKey key, LocalDateTime value){
		super(prefix, key, FractionalSecondTool.truncate(value, key.getNumFractionalSeconds()));
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
	public byte[] getValueBytes(){
		if(value == null){
			return null;
		}
		byte[] bytes = new byte[NUM_BYTES];
		int offset = 0;
		offset += COMPARABLE_INT_CODEC.encode(value.getYear(), bytes, offset);
		offset += COMPARABLE_SHORT_CODEC.encode((short) value.getMonthValue(), bytes, offset);
		offset += COMPARABLE_SHORT_CODEC.encode((short) value.getDayOfMonth(), bytes, offset);
		bytes[offset++] = (byte) value.getHour();
		bytes[offset++] = (byte) value.getMinute();
		bytes[offset++] = (byte) value.getSecond();
		offset += COMPARABLE_INT_CODEC.encode(value.getNano(), bytes, offset);
		return bytes;
	}

	@Override
	public int numKeyBytesWithSeparator(byte[] bytes, int offset){
		return NUM_BYTES;
	}

	@Override
	public LocalDateTime fromValueBytesButDoNotSet(byte[] bytes, int offset){
		int year = COMPARABLE_INT_CODEC.decode(bytes, offset);
		offset += 4;
		int month = COMPARABLE_SHORT_CODEC.decode(bytes, offset);
		offset += 2;
		int day = COMPARABLE_SHORT_CODEC.decode(bytes, offset);
		offset += 2;
		int hour = bytes[offset++];
		int minute = bytes[offset++];
		int second = bytes[offset++];
		int nano = COMPARABLE_INT_CODEC.decode(bytes, offset);
		return LocalDateTime.of(year, month, day, hour, minute, second, nano);
	}

}
