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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;
import io.datarouter.gson.serialization.GsonTool;
import io.datarouter.model.field.BaseListField;
import io.datarouter.model.field.Field;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.collection.ListTool;

@Deprecated//Use ByteArrayField
public class UInt7ArrayField extends BaseListField<Byte,List<Byte>,UInt7ArrayFieldKey>{
	private static final Logger logger = LoggerFactory.getLogger(UInt7ArrayField.class);

	private static final RawIntCodec RAW_INT_CODEC = RawIntCodec.INSTANCE;
	private static final AtomicBoolean UNCALLED_SEPARATOR_METHODS = new AtomicBoolean(true);

	public UInt7ArrayField(UInt7ArrayFieldKey key, List<Byte> value){
		super(key, value);
	}

	@Override
	public List<Byte> parseStringEncodedValueButDoNotSet(String value){
		return GsonTool.GSON.fromJson(value, getKey().getValueType());
	}

	@Override
	public byte[] getBytes(){
		return value == null ? null : ByteTool.fromBoxedBytesNoNegatives(value);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		if(UNCALLED_SEPARATOR_METHODS.getAndSet(false)){
			logger.warn("", new Exception());
		}
		return RAW_INT_CODEC.decode(bytes, byteOffset);
	}

	@Override
	public List<Byte> fromBytesWithSeparatorButDoNotSet(byte[] bytes, int byteOffset){
		if(UNCALLED_SEPARATOR_METHODS.getAndSet(false)){
			logger.warn("", new Exception());
		}
		int numBytes = numBytesWithSeparator(bytes, byteOffset) - 4;
		byte[] primitiveBytes = fromUInt7ByteArray(bytes, byteOffset + 4, numBytes);
		return ByteTool.toBoxedBytes(primitiveBytes);
	}

	@Override
	public List<Byte> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		if(UNCALLED_SEPARATOR_METHODS.getAndSet(false)){
			logger.warn("", new Exception());
		}
		int numBytes = ArrayTool.length(bytes) - byteOffset;
		byte[] primitiveBytes = fromUInt7ByteArray(bytes, byteOffset, numBytes);
		return ByteTool.toBoxedBytes(primitiveBytes);
	}

	@Override
	public int compareTo(Field<List<Byte>> other){
		return ListTool.compare(value, other.getValue());
	}

	private static byte[] fromUInt7ByteArray(byte[] bytes, int from, int length){
		int to = from + length;
		return Arrays.copyOfRange(bytes, from, to);
	}

}
