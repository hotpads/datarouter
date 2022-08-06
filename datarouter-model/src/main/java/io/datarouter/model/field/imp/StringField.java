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
		logInvalidSize();
	}

	public StringField(String prefix, StringFieldKey key, String value){
		super(prefix, value);
		this.key = key;
		logInvalidSize();
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
	public byte[] getValueBytes(){
		return value == null ? null : StringCodec.UTF_8.encode(value);
	}

	@Override
	public byte[] getKeyBytesWithSeparator(){
		//TODO someday don't put the separator after the last field, but that would break all currently persisted keys
		byte[] dataBytes = getValueBytes();
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
	public int numKeyBytesWithSeparator(byte[] bytes, int offset){
		for(int i = offset; i < bytes.length; ++i){
			if(bytes[i] == SEPARATOR){
				return i - offset + 1;//plus 1 for the separator
			}
		}
		int numBytes = bytes.length - offset;
		return numBytes >= 0 ? numBytes : 0;
	}

	@Override
	public String fromValueBytesButDoNotSet(byte[] bytes, int offset){
		int length = bytes.length - offset;
		return new String(bytes, offset, length, StandardCharsets.UTF_8);
	}

	@Override
	public String fromKeyBytesWithSeparatorButDoNotSet(byte[] bytes, int offset){
		int lengthWithPossibleSeparator = numKeyBytesWithSeparator(bytes, offset);
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

	@Override
	public void validate(){
		validateSize();
	}

	public int getSize(){
		return key.getSize();
	}

	private void logInvalidSize(){
		if(key.shouldLogInvalidSize() && isSizeInvalid()){
			logger.warn(makeInvalidSizeMessage(), new Exception());
		}
	}

	private void validateSize(){
		if(key.shouldValidateSize() && isSizeInvalid()){
			throw new IllegalArgumentException(makeInvalidSizeMessage());
		}
	}

	private boolean isSizeInvalid(){
		return value != null && value.length() > key.getSize();
	}

	private String makeInvalidSizeMessage(){
		return String.format(
				"value length=%s exceeds field size=%s for column=%s printableValue=%s",
				value.length(),
				key.getSize(),
				key.getColumnName(),
				toLogSafeValue(value));
	}

	private static String toLogSafeValue(String input){
		int trimmedLength = Math.min(input.length(), 1_000);
		String trimmedValue = input.substring(0, trimmedLength);
		return StringTool.removeNonStandardCharacters(trimmedValue);
	}

}
