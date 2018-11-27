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
package io.datarouter.model.field.imp;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import io.datarouter.model.field.BaseField;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldKey;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.bytes.StringByteTool;

public class StringField extends BaseField<String>{

	public static final byte SEPARATOR = 0;

	private final StringFieldKey key;

	public StringField(StringFieldKey key, String value){
		super(null, value);
		this.key = key;
	}

	public StringField(String prefix, StringFieldKey key, String value){
		super(prefix, value);
		this.key = key;
	}

	@Override
	public FieldKey<String> getKey(){
		return key;
	}

	@Override
	public int compareTo(Field<String> other){
		if(other == null){
			return -1;
		}
		return ComparableTool.nullFirstCompareTo(this.getValue(), other.getValue());
	}

	@Override
	public String getStringEncodedValue(){
		return value;
	}

	@Override
	public String parseStringEncodedValueButDoNotSet(String string){
		return string;
	}

	@Override
	public byte[] getBytes(){
		byte[] bytes = StringByteTool.getUtf8Bytes(value);
		return bytes;
	}

	@Override
	public byte[] getBytesWithSeparator(){
		//TODO someday don't put the separator after the last field, but that would break all currently persisted keys
		byte[] dataBytes = getBytes();
		if(ArrayTool.containsUnsorted(dataBytes, SEPARATOR)){
			throw new IllegalArgumentException("String cannot contain separator byteVal=" + SEPARATOR + ", stringBytes="
					+ Arrays.toString(dataBytes) + ", string=" + value);
		}
		if(ArrayTool.isEmpty(dataBytes)){
			return new byte[]{SEPARATOR};
		}
		byte[] allBytes = new byte[dataBytes.length + 1];
		System.arraycopy(dataBytes, 0, allBytes, 0, dataBytes.length);
		allBytes[allBytes.length - 1] = SEPARATOR;// Ascii "null" will compare first in lexicographical bytes comparison
		return allBytes;
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		for(int i = offset; i < bytes.length; ++i){
			if(bytes[i] == SEPARATOR){
				return i - offset + 1;//plus 1 for the separator
			}
		}
		int numBytes = bytes.length - offset;
		return numBytes >= 0 ? numBytes : 0; //not sure where the separator went.  schema change or corruption?
//		throw new IllegalArgumentException("separator not found for bytes:"+new String(bytes));
	}

	@Override
	public String fromBytesButDoNotSet(byte[] bytes, int offset){
		int length = bytes.length - offset;
		return new String(bytes, offset, length, StandardCharsets.UTF_8);
	}

	@Override
	public String fromBytesWithSeparatorButDoNotSet(byte[] bytes, int offset){
		int lengthIncludingSeparator = numBytesWithSeparator(bytes, offset);
		if(lengthIncludingSeparator <= 0){
			throw new RuntimeException("lengthIncludingSeparator=" + lengthIncludingSeparator + ", but should be >= 1");
		}
		boolean lastByteIsSeparator = bytes[offset + lengthIncludingSeparator - 1] == SEPARATOR;
		int lengthWithoutSeparator = lengthIncludingSeparator;
		if(lastByteIsSeparator){
			--lengthWithoutSeparator;
		}
		if(lengthWithoutSeparator == -1){
			lengthWithoutSeparator = 0;
		}
		return new String(bytes, offset, lengthWithoutSeparator, StandardCharsets.UTF_8);
	}

	public int getSize(){
		return key.getSize();
	}

}