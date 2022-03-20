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
package io.datarouter.model.field.imp.comparable;

import java.util.Map;

import io.datarouter.model.field.BaseFieldKey;
import io.datarouter.model.field.FieldKeyAttribute;
import io.datarouter.model.field.FieldKeyAttributeKey;
import io.datarouter.model.field.codec.IntegerFieldCodec;
import io.datarouter.model.field.encoding.FieldGeneratorType;

public class IntegerEncodedFieldKey<T> extends BaseFieldKey<T,IntegerEncodedFieldKey<T>>{

	private final IntegerFieldCodec<T> codec;

	public IntegerEncodedFieldKey(
			String name,
			IntegerFieldCodec<T> codec){
		super(name, codec.getValueClass());
		this.codec = codec;
	}

	private IntegerEncodedFieldKey(
			String name,
			IntegerFieldCodec<T> codec,
			String columnName,
			boolean nullable,
			FieldGeneratorType fieldGeneratorType,
			T defaultValue,
			Map<FieldKeyAttributeKey<?>,FieldKeyAttribute<?>> attributes){
		super(name, columnName, nullable, codec.getValueClass(), fieldGeneratorType, defaultValue, attributes);
		this.codec = codec;
	}

	public IntegerEncodedFieldKey<T> withColumnName(String columnName){
		return new IntegerEncodedFieldKey<>(
				name,
				codec,
				columnName,
				nullable,
				fieldGeneratorType,
				defaultValue,
				attributes);
	}

	public IntegerEncodedFieldKey<T> notNullable(){
		return new IntegerEncodedFieldKey<>(
				name,
				codec,
				columnName,
				false,
				fieldGeneratorType,
				defaultValue,
				attributes);
	}

	public IntegerEncodedFieldKey<T> withFieldGeneratorType(FieldGeneratorType fieldGeneratorTypeOverride){
		return new IntegerEncodedFieldKey<>(
				name,
				codec,
				columnName,
				nullable,
				fieldGeneratorTypeOverride,
				defaultValue,
				attributes);
	}

	@Override
	public IntegerEncodedField<T> createValueField(T value){
		return new IntegerEncodedField<>(this, value);
	}

	public IntegerFieldCodec<T> getCodec(){
		return codec;
	}

}
