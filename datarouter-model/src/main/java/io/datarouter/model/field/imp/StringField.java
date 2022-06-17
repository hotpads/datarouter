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
package io.datarouter.model.field.imp;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.model.field.BaseField;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.FieldKey;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.string.StringTool;

public class StringField extends BaseField<String>{
	private static final Logger logger = LoggerFactory.getLogger(StringField.class);

	public static final byte SEPARATOR = 0;

	private final StringFieldKey key;

	public StringField(StringFieldKey key, String value){
		super(null, value);
		this.key = key;
		validateSize(value);
	}

	public StringField(String prefix, StringFieldKey key, String value){
		super(prefix, value);
		this.key = key;
		validateSize(value);
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
		return value == null ? null : StringCodec.UTF_8.encode(value);
	}

	@Override
	public byte[] getBytesWithSeparator(){
		//TODO someday don't put the separator after the last field, but that would break all currently persisted keys
		byte[] dataBytes = getBytes();
		if(ArrayTool.containsUnsorted(dataBytes, SEPARATOR)){
			throw new IllegalArgumentException("String cannot contain separator byteVal=" + SEPARATOR + ", stringBytes="
					+ Arrays.toString(dataBytes) + ", string=" + value + ", key=" + key);
		}
		if(ArrayTool.isEmpty(dataBytes)){
			return new byte[]{SEPARATOR};
		}
		byte[] allBytes = new byte[dataBytes.length + 1];
		System.arraycopy(dataBytes, 0, allBytes, 0, dataBytes.length);
		allBytes[allBytes.length - 1] = SEPARATOR;// Ascii "null" will compare first in lexicographical bytes comparison
		return allBytes;
	}

	//warning: this tolerates a missing separator, returning the length without it
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		for(int i = offset; i < bytes.length; ++i){
			if(bytes[i] == SEPARATOR){
				return i - offset + 1;//plus 1 for the separator
			}
		}
		int numBytes = bytes.length - offset;
		return numBytes >= 0 ? numBytes : 0;
	}

	@Override
	public String fromBytesButDoNotSet(byte[] bytes, int offset){
		int length = bytes.length - offset;
		return new String(bytes, offset, length, StandardCharsets.UTF_8);
	}

	@Override
	public String fromBytesWithSeparatorButDoNotSet(byte[] bytes, int offset){
		int lengthWithPossibleSeparator = numBytesWithSeparator(bytes, offset);
		if(lengthWithPossibleSeparator == 0){
			return "";
		}
		int lastByteIndex = offset + lengthWithPossibleSeparator - 1;
		boolean lastByteIsSeparator = bytes[lastByteIndex] == SEPARATOR;
		int lengthWithoutSeparator = lastByteIsSeparator
				? lengthWithPossibleSeparator - 1
				: lengthWithPossibleSeparator;
		return new String(bytes, offset, lengthWithoutSeparator, StandardCharsets.UTF_8);
	}

	public int getSize(){
		return key.getSize();
	}

	private String validateSize(String value){
		if(value == null){
			return value;
		}
		if(value.length() <= key.getSize()){
			return value;
		}
		if(key.shouldValidateSize()){
			String trimmedValue = value.substring(0, Math.min(value.length(), 1_000));
			String loggableValue = StringTool.removeNonStandardCharacters(trimmedValue);
			//TODO throw exception, otherwise log
			logger.warn("value length={} exceeds field size={} for column={} loggableValue={}",
					value.length(),
					key.getSize(),
					key.getColumnName(),
					loggableValue,
					new Exception());
		}
		return value;
	}

}
