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
import io.datarouter.bytes.codec.intcodec.UInt31Codec;
import io.datarouter.model.field.BaseListField;
import io.datarouter.model.field.Field;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.serialization.GsonTool;

public class UInt7ArrayField extends BaseListField<Byte,List<Byte>,UInt7ArrayFieldKey>{

	private static final UInt31Codec U_INT_31_CODEC = UInt31Codec.INSTANCE;

	public UInt7ArrayField(UInt7ArrayFieldKey key, List<Byte> value){
		super(key, value);
	}

	@Override
	public List<Byte> parseStringEncodedValueButDoNotSet(String value){
		return GsonTool.GSON.fromJson(value, getKey().getValueType());
	}

	@Override
	public byte[] getBytes(){
		return this.value == null ? null : ByteTool.getUInt7Bytes(this.value);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		return U_INT_31_CODEC.decode(bytes, byteOffset);
	}

	@Override
	public List<Byte> fromBytesWithSeparatorButDoNotSet(byte[] bytes, int byteOffset){
		int numBytes = numBytesWithSeparator(bytes, byteOffset) - 4;
		return ByteTool.getArrayList2(ByteTool.fromUInt7ByteArray(bytes, byteOffset + 4, numBytes));
	}

	@Override
	public List<Byte> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		int numBytes = ArrayTool.length(bytes) - byteOffset;
		return ByteTool.getArrayList2(ByteTool.fromUInt7ByteArray(bytes, byteOffset, numBytes));
	}

	@Override
	public int compareTo(Field<List<Byte>> other){
		return ListTool.compare(this.value, other.getValue());
	}

}
