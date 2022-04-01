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

import java.util.Map;

import com.google.gson.reflect.TypeToken;

import io.datarouter.enums.StringEnum;
import io.datarouter.model.field.BaseFieldKey;
import io.datarouter.model.field.FieldKeyAttribute;
import io.datarouter.model.field.FieldKeyAttributeKey;
import io.datarouter.model.field.encoding.FieldGeneratorType;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.util.lang.ReflectionTool;

public class StringEnumFieldKey<E extends StringEnum<E>>
extends BaseFieldKey<E,StringEnumFieldKey<E>>{

	private static final int DEFAULT_MAX_SIZE = CommonFieldSizes.DEFAULT_LENGTH_VARCHAR;

	private final int size;
	private final Class<E> enumClass;
	private final E sampleValue;

	public StringEnumFieldKey(String name, Class<E> enumClass){
		super(name, TypeToken.get(enumClass));
		this.size = DEFAULT_MAX_SIZE;
		this.enumClass = enumClass;
		this.sampleValue = ReflectionTool.create(enumClass);
	}

	private StringEnumFieldKey(
			String name,
			E sampleValue,
			String columnName,
			boolean nullable,
			Class<E> enumClass,
			FieldGeneratorType fieldGeneratorType,
			E defaultValue,
			int size,
			Map<FieldKeyAttributeKey<?>,FieldKeyAttribute<?>> attributes){
		super(name, columnName, nullable, TypeToken.get(enumClass), fieldGeneratorType, defaultValue, attributes);
		this.size = size;
		this.enumClass = enumClass;
		this.sampleValue = sampleValue;
	}

	public StringEnumFieldKey<E> withSize(int sizeOverride){
		return new StringEnumFieldKey<>(
				name,
				sampleValue,
				columnName,
				nullable,
				enumClass,
				fieldGeneratorType,
				defaultValue,
				sizeOverride,
				attributes);
	}

	public StringEnumFieldKey<E> withColumnName(String columnNameOverride){
		return new StringEnumFieldKey<>(
				name,
				sampleValue,
				columnNameOverride,
				nullable,
				enumClass,
				fieldGeneratorType,
				defaultValue,
				size,
				attributes);
	}

	public StringEnumFieldKey<E> withDefaultValue(E defaultValueOverride){
		return new StringEnumFieldKey<>(
				name,
				sampleValue,
				columnName,
				nullable,
				enumClass,
				fieldGeneratorType,
				defaultValueOverride,
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

	public E getSampleValue(){
		return sampleValue;
	}

}
