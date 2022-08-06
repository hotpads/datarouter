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

import java.lang.reflect.Type;
import java.util.Map;

import io.datarouter.model.field.BaseFieldKey;
import io.datarouter.model.field.FieldKeyAttribute;
import io.datarouter.model.field.FieldKeyAttributeKey;
import io.datarouter.model.field.codec.FieldCodec;
import io.datarouter.model.field.encoding.FieldGeneratorType;

public class LongEncodedFieldKey<T> extends BaseFieldKey<T,LongEncodedFieldKey<T>>{

	private final FieldCodec<T,Long> codec;

	public LongEncodedFieldKey(
			String name,
			FieldCodec<T,Long> codec){
		super(name, codec.getTypeToken());
		this.codec = codec;
	}

	private LongEncodedFieldKey(
			String name,
			FieldCodec<T,Long> codec,
			String columnName,
			boolean nullable,
			FieldGeneratorType fieldGeneratorType,
			T defaultValue,
			Map<FieldKeyAttributeKey<?>,FieldKeyAttribute<?>> attributes){
		super(name, columnName, nullable, codec.getTypeToken(), fieldGeneratorType, defaultValue, attributes);
		this.codec = codec;
	}

	public LongEncodedFieldKey<T> withColumnName(String columnName){
		return new LongEncodedFieldKey<>(
				name,
				codec,
				columnName,
				nullable,
				fieldGeneratorType,
				defaultValue,
				attributes);
	}

	public LongEncodedFieldKey<T> notNullable(){
		return new LongEncodedFieldKey<>(
				name,
				codec,
				columnName,
				false,
				fieldGeneratorType,
				defaultValue,
				attributes);
	}

	public LongEncodedFieldKey<T> withFieldGeneratorType(FieldGeneratorType fieldGeneratorTypeOverride){
		return new LongEncodedFieldKey<>(
				name,
				codec,
				columnName,
				nullable,
				fieldGeneratorTypeOverride,
				defaultValue,
				attributes);
	}

	public FieldCodec<T,Long> getCodec(){
		return codec;
	}

	@Override
	public Type getGenericType(){
		return Long.class;
	}

	@Override
	public T getSampleValue(){
		return codec.getSampleValue();
	}

}
