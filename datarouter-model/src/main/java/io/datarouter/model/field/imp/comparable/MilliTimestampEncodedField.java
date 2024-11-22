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
package io.datarouter.model.field.imp.comparable;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import io.datarouter.bytes.codec.longcodec.ComparableLongCodec;
import io.datarouter.model.field.BaseField;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldKey;
import io.datarouter.model.field.codec.FieldCodec;
import io.datarouter.types.MilliTime;

public class MilliTimestampEncodedField<T> extends BaseField<T>{

	private static final ComparableLongCodec COMPARABLE_LONG_CODEC = ComparableLongCodec.INSTANCE;

	// Limits taken from com.google.protobuf.util.Timestamps
	static final long EPOCH_MILLI_MIN = -62_135_596_800_000L; // 0001-01-01T00:00:00Z
	static final long EPOCH_MILLI_MAX = 253_402_300_799_000L; // 9999-12-31T23:59:59Z

	private final MilliTimestampEncodedFieldKey<T> key;

	public MilliTimestampEncodedField(String prefix, MilliTimestampEncodedFieldKey<T> key, T value){
		super(prefix, value);
		this.key = key;
	}

	public MilliTimestampEncodedField(MilliTimestampEncodedFieldKey<T> key, T value){
		super(null, value);
		this.key = key;
	}

	@Override
	public FieldKey<T> getKey(){
		return key;
	}

	public FieldCodec<T,MilliTime> getCodec(){
		return key.getCodec();
	}

	@Override
	public Optional<String> findAuxiliaryHumanReadableString(DateTimeFormatter dateTimeFormatter, ZoneId zoneId){
		return Optional.ofNullable(getValue())
				.flatMap(value -> getCodec().findAuxiliaryHumanReadableString(value, dateTimeFormatter, zoneId));
	}

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		MilliTime milliTime = key.getCodec().encode(value);
		long epochMilli = milliTime.toEpochMilli();
		return Long.toString(epochMilli);
	}

	@Override
	public T parseStringEncodedValueButDoNotSet(String stringValue){
		long epochMilli = Long.valueOf(stringValue);
		validate(epochMilli);
		MilliTime milliTime = MilliTime.ofEpochMilli(epochMilli);
		return key.getCodec().decode(milliTime);
	}

	@Override
	public byte[] getValueBytes(){
		MilliTime encodedValue = key.getCodec().encode(value);
		if(encodedValue == null){
			return null;
		}
		long epochMilli = encodedValue.toEpochMilli();
		validate(epochMilli);
		return COMPARABLE_LONG_CODEC.encode(epochMilli);
	}

	@Override
	public int getApproximateValueBytesLength(){
		return value == null ? 0 : COMPARABLE_LONG_CODEC.length();
	}

	@Override
	public T fromValueBytesButDoNotSet(byte[] bytes, int byteOffset){
		long epochMilli = COMPARABLE_LONG_CODEC.decode(bytes, byteOffset);
		validate(epochMilli);
		MilliTime milliTime = MilliTime.ofEpochMilli(epochMilli);
		return key.getCodec().decode(milliTime);
	}

	@Override
	public int numKeyBytesWithSeparator(byte[] bytes, int byteOffset){
		return COMPARABLE_LONG_CODEC.length();
	}

	@Override
	public int compareTo(Field<T> other){
		return key.getCodec().getComparator().compare(value, other.getValue());
	}

	/*---------- validate ----------*/

	@Override
	public void validate(){
		MilliTime milliTime = key.getCodec().encode(value);
		if(milliTime != null){
			validate(milliTime.toEpochMilli());
		}
	}

	private static long validate(long epochMilli){
		if(!isValid(epochMilli)){
			String message = "Invalid epochMilli=%s, min=%s, max=%s"
					.formatted(epochMilli, EPOCH_MILLI_MIN, EPOCH_MILLI_MAX);
			throw new IllegalArgumentException(message);
		}
		return epochMilli;
	}

	private static boolean isValid(long epochMilli){
		return epochMilli >= EPOCH_MILLI_MIN && epochMilli <= EPOCH_MILLI_MAX;
	}

}
