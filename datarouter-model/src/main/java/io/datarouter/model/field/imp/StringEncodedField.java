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
package io.datarouter.model.field.imp;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.bytes.codec.stringcodec.TerminatedStringCodec;
import io.datarouter.model.field.BaseField;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldKey;
import io.datarouter.model.field.codec.FieldCodec;

public class StringEncodedField<T> extends BaseField<T>{

	private final StringEncodedFieldKey<T> key;

	public StringEncodedField(StringEncodedFieldKey<T> key, T value){
		super(null, value);
		this.key = key;
	}

	public StringEncodedField(String prefix, StringEncodedFieldKey<T> key, T value){
		super(prefix, value);
		this.key = key;
	}

	@Override
	public FieldKey<T> getKey(){
		return key;
	}

	@Override
	public Object getGenericValue(){
		return getStringEncodedValue();
	}

	@Override
	public int compareTo(Field<T> other){
		return key.getCodec().getComparator().compare(value, other.getValue());
	}

	public int getSize(){
		return key.getSize();
	}

	/*-------------- BinaryValueField -----------*/

	@Override
	public byte[] getValueBytes(){
		return Optional.ofNullable(value)
				.map(key.getCodec()::encode)
				.map(StringCodec.UTF_8::encode)
				.orElse(null);
	}

	@Override
	public int getApproximateValueBytesLength(){
		byte[] valueBytes = getValueBytes();
		return valueBytes == null ? 0 : valueBytes.length;
	}

	@Override
	public T fromValueBytesButDoNotSet(byte[] bytes, int offset){
		String stringValue = StringCodec.UTF_8.decode(bytes, offset);
		return key.getCodec().decode(stringValue);
	}

	/*------------- BinaryKeyField ------------*/

	@Override
	public int numKeyBytesWithSeparator(byte[] bytes, int offset){
		return TerminatedStringCodec.UTF_8.decode(bytes, offset).length();
	}

	@Override
	public byte[] getKeyBytesWithSeparator(){
		String stringValue = key.getCodec().encode(value);
		return TerminatedStringCodec.UTF_8.encode(stringValue);
	}

	@Override
	public T fromKeyBytesWithSeparatorButDoNotSet(byte[] bytes, int offset){
		String stringValue = TerminatedStringCodec.UTF_8.decode(bytes, offset).value();
		return key.getCodec().decode(stringValue);
	}

	/*------------- StringEncodedField ----------*/

	@Override
	public String getStringEncodedValue(){
		return key.getCodec().encode(value);
	}

	@Override
	public T parseStringEncodedValueButDoNotSet(String string){
		return key.getCodec().decode(string);
	}

	public FieldCodec<T,String> getCodec(){
		return key.getCodec();
	}

	@Override
	public Optional<String> findAuxiliaryHumanReadableString(DateTimeFormatter dateTimeFormatter, ZoneId zoneId){
		return Optional.ofNullable(getValue())
				.flatMap(value -> getCodec().findAuxiliaryHumanReadableString(value, dateTimeFormatter, zoneId));
	}

}
