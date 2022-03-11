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

import java.util.List;

import io.datarouter.bytes.LongArray;
import io.datarouter.bytes.codec.array.longarray.RawLongArrayCodec;
import io.datarouter.gson.serialization.GsonTool;
import io.datarouter.model.field.BaseListField;
import io.datarouter.model.field.Field;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.collection.ListTool;

@Deprecated//Use ByteArrayField
public class RawLongArrayField extends BaseListField<Long,List<Long>,RawLongArrayFieldKey>{

	private static final RawLongArrayCodec RAW_LONG_ARRAY_CODEC = RawLongArrayCodec.INSTANCE;

	public RawLongArrayField(RawLongArrayFieldKey key, List<Long> value){
		super(key, value);
	}

	//TODO should we even bother?
	@Override
	public int compareTo(Field<List<Long>> other){
		return ListTool.compare(this.value, other.getValue());
	}

	@Override
	public List<Long> parseStringEncodedValueButDoNotSet(String value){
		return GsonTool.GSON.fromJson(value, getKey().getValueType());
	}

	@Override
	public byte[] getBytes(){
		if(value == null){
			return null;
		}
		LongArray longArray = value instanceof LongArray
				? (LongArray)value
				: new LongArray(value);
		return RAW_LONG_ARRAY_CODEC.encode(longArray.getPrimitiveArray());
	}

	@Override
	public List<Long> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		int numBytes = ArrayTool.length(bytes) - byteOffset;
		return new LongArray(RAW_LONG_ARRAY_CODEC.decode(bytes, byteOffset, numBytes));
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] getBytesWithSeparator(){
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Long> fromBytesWithSeparatorButDoNotSet(byte[] bytes, int byteOffset){
		throw new UnsupportedOperationException();
	}

}
