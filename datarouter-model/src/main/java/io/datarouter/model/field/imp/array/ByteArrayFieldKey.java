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

import java.util.Map;

import com.google.gson.reflect.TypeToken;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.model.field.BaseFieldKey;
import io.datarouter.model.field.FieldKeyAttribute;
import io.datarouter.model.field.FieldKeyAttributeKey;
import io.datarouter.model.field.encoding.FieldGeneratorType;
import io.datarouter.model.util.CommonFieldSizes;

public class ByteArrayFieldKey extends BaseFieldKey<byte[],ByteArrayFieldKey>{

	private final int size;

	public ByteArrayFieldKey(String name){
		super(name, TypeToken.get(byte[].class));
		this.size = CommonFieldSizes.MAX_KEY_LENGTH;
	}

	private ByteArrayFieldKey(
			String name,
			String columnName,
			boolean nullable,
			FieldGeneratorType fieldGeneratorType,
			byte[] defaultValue,
			int size,
			Map<FieldKeyAttributeKey<?>,FieldKeyAttribute<?>> attributes){
		super(name, columnName, nullable, TypeToken.get(byte[].class), fieldGeneratorType, defaultValue, attributes);
		this.size = size;
	}

	public ByteArrayFieldKey withSize(int size){
		return new ByteArrayFieldKey(name, columnName, nullable, fieldGeneratorType, defaultValue, size, attributes);
	}

	@Override
	public boolean isFixedLength(){
		return false;
	}

	public int getSize(){
		return size;
	}

	@Override
	public byte[] getSampleValue(){
		return EmptyArray.BYTE;
	}

}
