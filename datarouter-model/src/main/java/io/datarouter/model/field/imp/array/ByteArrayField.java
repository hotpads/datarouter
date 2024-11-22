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

import io.datarouter.bytes.TerminatedByteArrayTool;
import io.datarouter.bytes.codec.bytestringcodec.HexByteStringCodec;
import io.datarouter.model.field.BaseField;
import io.datarouter.model.field.Field;

public class ByteArrayField extends BaseField<byte[]>{

	private final ByteArrayFieldKey key;

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
	public byte[] getValueBytes(){
		return value;
	}

	@Override
	public int getApproximateValueBytesLength(){
		return value == null ? 0 : value.length;
	}

	@Override
	public byte[] getKeyBytesWithSeparator(){
		return TerminatedByteArrayTool.escapeAndTerminate(value);
	}

	@Override
	public int numKeyBytesWithSeparator(byte[] bytes, int offset){
		return TerminatedByteArrayTool.lengthWithTerminator(bytes, offset);
	}

	@Override
	public byte[] fromKeyBytesWithSeparatorButDoNotSet(byte[] bytes, int offset){
		return TerminatedByteArrayTool.unescapeAndUnterminate(bytes, offset);
	}

	@Override
	public byte[] fromValueBytesButDoNotSet(byte[] bytes, int offset){
		if(offset == 0){
			return bytes;
		}
		return Arrays.copyOfRange(bytes, offset, bytes.length);
	}

	@Override
	public String getValueString(){
		return value == null ? "null" : HexByteStringCodec.INSTANCE.encode(value);
	}

	@Override
	public int getValueHashCode(){
		return Arrays.hashCode(value);
	}

	@Override
	public int compareTo(Field<byte[]> other){
		return Arrays.compareUnsigned(value, other.getValue());
	}

}
