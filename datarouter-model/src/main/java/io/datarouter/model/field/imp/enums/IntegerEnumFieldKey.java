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
package io.datarouter.model.field.imp.enums;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.reflect.TypeToken;

import io.datarouter.enums.IntegerEnum;
import io.datarouter.model.field.BaseFieldKey;
import io.datarouter.model.field.FieldKeyAttribute;
import io.datarouter.model.field.FieldKeyAttributeKey;
import io.datarouter.model.field.encoding.FieldGeneratorType;
import io.datarouter.util.lang.ReflectionTool;

/**
 * @deprecated  Use IntegerEncodedFieldKey with IntegerMappedEnumFieldCodec and MappedEnum
 */
@Deprecated
public class IntegerEnumFieldKey<E extends IntegerEnum<E>>
extends BaseFieldKey<E,IntegerEnumFieldKey<E>>{

	private final Class<E> enumClass;
	private final E sampleValue;

	public IntegerEnumFieldKey(String name, Class<E> enumClass){
		super(name, TypeToken.get(enumClass));
		this.enumClass = enumClass;
		this.sampleValue = ReflectionTool.create(enumClass);
	}

	public IntegerEnumFieldKey(
			String name,
			String columnName,
			boolean nullable,
			Class<E> enumClass,
			FieldGeneratorType fieldGeneratorType,
			E defaultValue,
			E sampleValue,
			Map<FieldKeyAttributeKey<?>,FieldKeyAttribute<?>> attributes){
		super(name, columnName, nullable, TypeToken.get(enumClass), fieldGeneratorType, defaultValue, attributes);
		this.enumClass = enumClass;
		this.sampleValue = sampleValue;
	}

	public IntegerEnumFieldKey<E> withColumnName(String columnNameOverride){
		return new IntegerEnumFieldKey<>(
				name,
				columnNameOverride,
				nullable,
				enumClass,
				fieldGeneratorType,
				defaultValue,
				sampleValue,
				attributes);
	}

	@Override
	public E getSampleValue(){
		return sampleValue;
	}

	@Override
	public Type getGenericType(){
		return String.class;
	}

	@Override
	public Optional<String> findDocString(){
		@SuppressWarnings("unchecked")
		Enum<?>[] enums = ((Class<Enum<?>>)enumClass).getEnumConstants();
		String doc = Arrays.stream(enums)
				.map(symbol -> ((IntegerEnum<?>)symbol).getPersistentInteger() + ": " + symbol.name())
				.collect(Collectors.joining(", ", "[ ", " ]"));
		return Optional.of(doc);
	}

}
