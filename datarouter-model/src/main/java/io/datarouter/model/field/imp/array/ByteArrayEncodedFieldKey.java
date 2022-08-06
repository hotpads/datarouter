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

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

import io.datarouter.model.field.BaseFieldKey;
import io.datarouter.model.field.FieldKeyAttribute;
import io.datarouter.model.field.FieldKeyAttributeKey;
import io.datarouter.model.field.codec.FieldCodec;
import io.datarouter.model.field.encoding.FieldGeneratorType;
import io.datarouter.model.util.CommonFieldSizes;

public class ByteArrayEncodedFieldKey<T> extends BaseFieldKey<T,ByteArrayEncodedFieldKey<T>>{

	private final FieldCodec<T,byte[]> codec;
	private final int size;

	public ByteArrayEncodedFieldKey(
			String name,
			FieldCodec<T,byte[]> codec){
		super(name, codec.getTypeToken());
		this.codec = codec;
		this.size = CommonFieldSizes.MAX_KEY_LENGTH;
	}

	private ByteArrayEncodedFieldKey(
			String name,
			FieldCodec<T,byte[]> codec,
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

	public ByteArrayEncodedFieldKey<T> withSize(int sizeOverride){
		return new ByteArrayEncodedFieldKey<>(
				name,
				codec,
				columnName,
				nullable,
				fieldGeneratorType,
				defaultValue,
				sizeOverride,
				attributes);
	}

	public ByteArrayEncodedFieldKey<T> withColumnName(String columnNameOverride){
		return new ByteArrayEncodedFieldKey<>(
				name,
				codec,
				columnNameOverride,
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

	@Override
	public Optional<Integer> findSize(){
		return Optional.of(size);
	}

	public int getSize(){
		return size;
	}

	public FieldCodec<T,byte[]> getCodec(){
		return codec;
	}

	@Override
	public Type getGenericType(){
		return byte[].class;
	}

	@Override
	public T getSampleValue(){
		return codec.getSampleValue();
	}

}
