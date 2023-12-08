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

public class LongEncodedField<T> extends BaseField<T>{

	private static final ComparableLongCodec COMPARABLE_LONG_CODEC = ComparableLongCodec.INSTANCE;

	private final LongEncodedFieldKey<T> key;

	public LongEncodedField(String prefix, LongEncodedFieldKey<T> key, T value){
		super(prefix, value);
		this.key = key;
	}

	public LongEncodedField(LongEncodedFieldKey<T> key, T value){
		super(null, value);
		this.key = key;
	}

	@Override
	public String getStringEncodedValue(){
		Long longValue = key.getCodec().encode(value);
		return longValue == null ? null : Long.toString(longValue);
	}

	@Override
	public T parseStringEncodedValueButDoNotSet(String stringValue){
		if(stringValue == null){
			return null;
		}
		long longValue = Long.valueOf(stringValue);
		return key.getCodec().decode(longValue);
	}

	@Override
	public byte[] getValueBytes(){
		Long longValue = key.getCodec().encode(value);
		return longValue == null ? null : COMPARABLE_LONG_CODEC.encode(longValue);
	}

	@Override
	public int numKeyBytesWithSeparator(byte[] bytes, int offset){
		return COMPARABLE_LONG_CODEC.length();
	}

	@Override
	public T fromValueBytesButDoNotSet(byte[] bytes, int offset){
		long longValue = COMPARABLE_LONG_CODEC.decode(bytes, offset);
		return key.getCodec().decode(longValue);
	}

	@Override
	public FieldKey<T> getKey(){
		return key;
	}

	@Override
	public Object getGenericValue(){
		return key.getCodec().encode(value);
	}

	@Override
	public int compareTo(Field<T> other){
		return key.getCodec().getComparator().compare(value, other.getValue());
	}

	public FieldCodec<T,Long> getCodec(){
		return key.getCodec();
	}

	@Override
	public Optional<String> findAuxiliaryHumanReadableString(DateTimeFormatter dateTimeFormatter, ZoneId zoneId){
		return Optional.ofNullable(getValue())
				.flatMap(value -> getCodec().findAuxiliaryHumanReadableString(value, dateTimeFormatter, zoneId));
	}

}
