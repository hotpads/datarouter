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
package io.datarouter.model.field.imp.comparable;

import java.nio.charset.StandardCharsets;

import io.datarouter.model.field.BasePrimitiveField;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.bytes.StringByteTool;
import io.datarouter.util.string.StringTool;

public class CharacterField extends BasePrimitiveField<Character>{

	public CharacterField(CharacterFieldKey key, Character value){
		super(key, value);
	}

	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return value.toString();
	}

	@Override
	public Character parseStringEncodedValueButDoNotSet(String str){
		if(StringTool.isEmpty(str)){
			return null;
		}
		return str.charAt(0);
	}

	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value == null ? null : StringByteTool.getUtf8Bytes(value.toString());
	}

	@Override
	public byte[] getBytesWithSeparator(){
		byte[] dataBytes = getBytes();
		if(ArrayTool.isEmpty(dataBytes)){
			return new byte[]{StringField.SEPARATOR};
		}
		byte[] allBytes = new byte[dataBytes.length + 1];
		System.arraycopy(dataBytes, 0, allBytes, 0, dataBytes.length);
		allBytes[allBytes.length - 1] = StringField.SEPARATOR;
		return allBytes;
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		//TODO this should be reviewed for correctness
		for(int i = offset; i < bytes.length; ++i){
			if(bytes[i] == StringField.SEPARATOR){
				return i - offset + 1;//plus 1 for the separator
			}
		}
		throw new IllegalArgumentException("separator not found");
	}

	@Override
	public Character fromBytesButDoNotSet(byte[] bytes, int offset){
		int length = bytes.length - offset;
		return new String(bytes, offset, length, StandardCharsets.UTF_8).charAt(0);
	}

}
