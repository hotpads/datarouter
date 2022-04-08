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

import io.datarouter.bytes.codec.array.longarray.ComparableLongArrayCodec;
import io.datarouter.model.field.BaseField;
import io.datarouter.model.field.Field;

@Deprecated
public class LongArrayField extends BaseField<long[]>{

	private static final ComparableLongArrayCodec COMPARABLE_LONG_ARRAY_CODEC = ComparableLongArrayCodec.INSTANCE;

	private final LongArrayFieldKey key;

	public LongArrayField(LongArrayFieldKey key, long[] value){
		super(null, value);
		this.key = key;
	}

	@Override
	public LongArrayFieldKey getKey(){
		return key;
	}

	@Override
	public String getValueString(){
		return Arrays.toString(value);
	}

	@Override
	public int getValueHashCode(){
		return Arrays.hashCode(value);
	}

	@Override
	public int compareTo(Field<long[]> field){
		return Arrays.compare(value, field.getValue());
	}

	@Override
	public String getStringEncodedValue(){
		return Arrays.toString(value);
	}

	@Override
	public long[] parseStringEncodedValueButDoNotSet(String value){
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] getBytes(){
		return value == null ? null : COMPARABLE_LONG_ARRAY_CODEC.encode(value);
	}

	@Override
	public long[] fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		return COMPARABLE_LONG_ARRAY_CODEC.decode(bytes);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		throw new UnsupportedOperationException();
	}

}
