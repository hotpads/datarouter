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

import io.datarouter.model.field.BaseFieldKey;
import io.datarouter.model.field.FieldKeyAttribute;
import io.datarouter.model.field.FieldKeyAttributeKey;
import io.datarouter.model.field.codec.ByteArrayFieldCodec;
import io.datarouter.model.field.encoding.FieldGeneratorType;
import io.datarouter.model.util.CommonFieldSizes;

public class ByteArrayEncodedFieldKey<T> extends BaseFieldKey<T,ByteArrayEncodedFieldKey<T>>{

	private final ByteArrayFieldCodec<T> codec;
	private final int size;

	public ByteArrayEncodedFieldKey(
			String name,
			ByteArrayFieldCodec<T> codec){
		super(name, codec.getTypeToken());
		this.codec = codec;
		this.size = CommonFieldSizes.MAX_KEY_LENGTH;
	}

	private ByteArrayEncodedFieldKey(
			String name,
			ByteArrayFieldCodec<T> codec,
			String columnName,
			boolean nullable,
			FieldGeneratorType fieldGeneratorType,
			T defaultValue,
			int size,
			Map<FieldKeyAttributeKey<?>,FieldKeyAttribute<?>> attributes){
		super(name, columnName, nullable, codec.getTypeToken(), fieldGeneratorType, defaultValue, attributes);
		this.codec = codec;
		this.size = size;
	}

	public ByteArrayEncodedFieldKey<T> withSize(int size){
		return new ByteArrayEncodedFieldKey<>(
				name,
				codec,
				columnName,
				nullable,
				fieldGeneratorType,
				defaultValue,
				size,
				attributes);
	}

	@Override
	public boolean isFixedLength(){
		return false;
	}

	public int getSize(){
		return size;
	}

	public ByteArrayFieldCodec<T> getCodec(){
		return codec;
	}

}
