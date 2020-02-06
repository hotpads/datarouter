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
package io.datarouter.model.field.imp.array;

import java.util.List;

import io.datarouter.model.field.BaseListField;
import io.datarouter.model.field.Field;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.array.LongArray;
import io.datarouter.util.bytes.IntegerByteTool;
import io.datarouter.util.bytes.LongByteTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.serialization.GsonTool;

public class UInt63ArrayField extends BaseListField<Long,List<Long>,UInt63ArrayFieldKey>{

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
		return value == null ? null : LongByteTool.getUInt63ByteArray(value);
	}

	@Override
	public List<Long> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		int numBytes = ArrayTool.length(bytes) - byteOffset;
		return new LongArray(LongByteTool.fromUInt63ByteArray(bytes, byteOffset, numBytes));
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		return bytes == null ? 0 : IntegerByteTool.fromUInt31Bytes(bytes, byteOffset) + 4;
	}

	@Override
	public byte[] getBytesWithSeparator(){
		if(value == null){
			return IntegerByteTool.getUInt31Bytes(0);
		}
		// prepend the length (in bytes) as a positive integer (not bitwise comparable =()
		//TODO replace with varint
		byte[] dataBytes = LongByteTool.getUInt63ByteArray(value);
		byte[] allBytes = new byte[4 + dataBytes.length];
		System.arraycopy(IntegerByteTool.getUInt31Bytes(dataBytes.length), 0, allBytes, 0, 4);
		System.arraycopy(dataBytes, 0, allBytes, 4, dataBytes.length);
		return allBytes;
	}

	@Override
	public List<Long> fromBytesWithSeparatorButDoNotSet(byte[] bytes, int byteOffset){
		int numBytes = numBytesWithSeparator(bytes, byteOffset) - 4;
		return new LongArray(LongByteTool.fromUInt63ByteArray(bytes, byteOffset + 4, numBytes));
	}
}
