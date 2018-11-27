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

import java.util.Arrays;
import java.util.Base64;

import org.apache.commons.codec.binary.Hex;

import io.datarouter.model.field.BaseField;
import io.datarouter.model.field.Field;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.bytes.ByteTool;
import io.datarouter.util.bytes.IntegerByteTool;

public class ByteArrayField extends BaseField<byte[]>{

	private ByteArrayFieldKey key;

	public ByteArrayField(ByteArrayFieldKey key, byte[] value){
		super(null, value);
		this.key = key;
	}

	@Override
	public ByteArrayFieldKey getKey(){
		return key;
	}

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return Base64.getEncoder().encodeToString(value);
	}

	@Override
	public byte[] parseStringEncodedValueButDoNotSet(String stringValue){
		return Base64.getDecoder().decode(stringValue);
	}

	@Override
	public byte[] getBytes(){
		return value == null ? null : ByteTool.flipToAndFromComparableByteArray(this.value);
	}

	@Override
	public byte[] getBytesWithSeparator(){
		if(this.value == null){
			return null;
		}
		// prepend the length as a positive integer (not bitwise comparable =)
		// TODO replace with varint
		// TODO write directly to the allBytes array
		byte[] dataBytes = ByteTool.flipToAndFromComparableByteArray(value);
		byte[] allBytes = new byte[4 + ArrayTool.length(dataBytes)];
		System.arraycopy(IntegerByteTool.getUInt31Bytes(0), 0, allBytes, 4, 4);
		System.arraycopy(dataBytes, 0, allBytes, 4, ArrayTool.length(dataBytes));
		return allBytes;
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return IntegerByteTool.fromUInt31Bytes(bytes, offset);// should we be adding 4 here?
	}

	@Override
	public byte[] fromBytesWithSeparatorButDoNotSet(byte[] bytes, int offset){
		int numBytes = numBytesWithSeparator(bytes, offset) - 4;
		return ByteTool.flipToAndFromComparableByteArray(bytes, offset + 4, numBytes);
	}

	@Override
	public byte[] fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		int length = bytes.length - byteOffset;
		return ByteTool.flipToAndFromComparableByteArray(bytes, byteOffset, length);
	}

	@Override
	public String getValueString(){
		return value == null ? "null" : Hex.encodeHexString(value);
	}

	@Override
	public int getValueHashCode(){
		return Arrays.hashCode(value);
	}

	@Override
	public int compareTo(Field<byte[]> other){
		return ByteTool.bitwiseCompare(this.value, other.getValue());
	}

}
