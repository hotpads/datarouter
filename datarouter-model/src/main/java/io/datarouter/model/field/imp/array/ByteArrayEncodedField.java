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
package io.datarouter.model.field.imp.array;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import io.datarouter.model.field.BaseField;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.FieldCodec;

public class ByteArrayEncodedField<T> extends BaseField<T>{

	private final ByteArrayEncodedFieldKey<T> key;

	public ByteArrayEncodedField(ByteArrayEncodedFieldKey<T> key, T value){
		super(null, value);
		this.key = key;
	}

	@Override
	public ByteArrayEncodedFieldKey<T> getKey(){
		return key;
	}

	@Override
	public String getStringEncodedValue(){
		return Optional.ofNullable(value)
				.map(getCodec()::encode)
				.map(Base64.getEncoder()::encodeToString)
				.orElse(null);
	}

	@Override
	public T parseStringEncodedValueButDoNotSet(String stringValue){
		byte[] bytesValue = Base64.getDecoder().decode(stringValue);
		return getCodec().decode(bytesValue);
	}

	@Override
	public byte[] getBytes(){
		return getCodec().encode(value);
	}

	@Override
	public byte[] getBytesWithSeparator(){
		//Would need to be implemented with something like TerminatedStringCodec
		throw new UnsupportedOperationException();
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		//Would need to be implemented with something like TerminatedStringCodec
		throw new UnsupportedOperationException();
	}

	@Override
	public T fromBytesWithSeparatorButDoNotSet(byte[] bytes, int offset){
		//Would need to be implemented with something like TerminatedStringCodec
		throw new UnsupportedOperationException();
	}

	@Override
	public T fromBytesButDoNotSet(byte[] bytes, int offset){
		byte[] bytesToDecode = offset == 0
				? bytes
				: Arrays.copyOfRange(bytes, offset, bytes.length);
		return getCodec().decode(bytesToDecode);
	}

	@Override
	public int compareTo(Field<T> other){
		return key.getCodec().getComparator().compare(value, other.getValue());
	}

	public FieldCodec<T,byte[]> getCodec(){
		return key.getCodec();
	}

}
