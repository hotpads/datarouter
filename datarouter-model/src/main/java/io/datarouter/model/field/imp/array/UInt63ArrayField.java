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

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.LongArray;
import io.datarouter.bytes.codec.array.longarray.UInt63ArrayCodec;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;
import io.datarouter.gson.serialization.GsonTool;
import io.datarouter.model.field.BaseListField;
import io.datarouter.model.field.Field;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.collection.ListTool;

@Deprecated//Use ByteArrayField
public class UInt63ArrayField extends BaseListField<Long,List<Long>,UInt63ArrayFieldKey>{

	private static final RawIntCodec RAW_INT_CODEC = RawIntCodec.INSTANCE;
	private static final UInt63ArrayCodec U_INT_63_ARRAY_CODEC = UInt63ArrayCodec.INSTANCE;

	public UInt63ArrayField(UInt63ArrayFieldKey key, List<Long> value){
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
		return U_INT_63_ARRAY_CODEC.encode(longArray.getPrimitiveArray());
	}

	@Override
	public List<Long> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		int numBytes = ArrayTool.length(bytes) - byteOffset;
		return new LongArray(U_INT_63_ARRAY_CODEC.decode(bytes, byteOffset, numBytes));
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		return bytes == null ? 0 : RAW_INT_CODEC.decode(bytes, byteOffset) + 4;
	}

	@Override
	public byte[] getBytesWithSeparator(){
		if(value == null){
			return RAW_INT_CODEC.encode(0);
		}
		LongArray longArray = value instanceof LongArray
				? (LongArray)value
				: new LongArray(value);
		byte[] dataBytes = U_INT_63_ARRAY_CODEC.encode(longArray.getPrimitiveArray());
		byte[] lengthBytes = RAW_INT_CODEC.encode(dataBytes.length);
		return ByteTool.concat(lengthBytes, dataBytes);
	}

	@Override
	public List<Long> fromBytesWithSeparatorButDoNotSet(byte[] bytes, int byteOffset){
		int numBytes = numBytesWithSeparator(bytes, byteOffset) - 4;
		return new LongArray(U_INT_63_ARRAY_CODEC.decode(bytes, byteOffset + 4, numBytes));
	}

}
