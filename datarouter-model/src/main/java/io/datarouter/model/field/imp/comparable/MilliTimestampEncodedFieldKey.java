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
import io.datarouter.types.MilliTime;

public class MilliTimestampEncodedFieldKey<T> extends BaseFieldKey<T,MilliTimestampEncodedFieldKey<T>>{

	public static final int NUM_DECIMAL_SECONDS = 3;

	private final FieldCodec<T,MilliTime> codec;

	public MilliTimestampEncodedFieldKey(String name, FieldCodec<T,MilliTime> codec){
		super(name, codec.getTypeToken());
		this.codec = codec;
	}

	private MilliTimestampEncodedFieldKey(
			String name,
			FieldCodec<T,MilliTime> codec,
			String columnName,
			boolean nullable,
			FieldGeneratorType fieldGeneratorType,
			T defaultValue,
			Map<FieldKeyAttributeKey<?>,FieldKeyAttribute<?>> attributes){
		super(name, columnName, nullable, codec.getTypeToken(), fieldGeneratorType, defaultValue, attributes);
		this.codec = codec;
	}

	public MilliTimestampEncodedFieldKey<T> withColumnName(String columnName){
		return new MilliTimestampEncodedFieldKey<>(
				name,
				codec,
				columnName,
				nullable,
				fieldGeneratorType,
				defaultValue,
				attributes);
	}

	public MilliTimestampEncodedFieldKey<T> notNullable(){
		return new MilliTimestampEncodedFieldKey<>(
				name,
				codec,
				columnName,
				false,
				fieldGeneratorType,
				defaultValue,
				attributes);
	}

	public MilliTimestampEncodedFieldKey<T> withFieldGeneratorType(FieldGeneratorType fieldGeneratorTypeOverride){
		return new MilliTimestampEncodedFieldKey<>(
				name,
				codec,
				columnName,
				nullable,
				fieldGeneratorTypeOverride,
				defaultValue,
				attributes);
	}

	public FieldCodec<T,MilliTime> getCodec(){
		return codec;
	}

	@Override
	public Type getGenericType(){
		return MilliTime.class;
	}

	@Override
	public T getSampleValue(){
		return codec.getSampleValue();
	}

}
