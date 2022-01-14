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
package io.datarouter.bytes.binarydto.dto;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import io.datarouter.bytes.binarydto.internal.BinaryDtoMetadataParser;
import io.datarouter.bytes.binarydto.internal.BinaryDtoReflectionTool;
import io.datarouter.scanner.Scanner;

public abstract class BaseBinaryDto{

	@Override
	public final boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		return scanFields()
				.allMatch(field -> Objects.deepEquals(
						BinaryDtoReflectionTool.getUnchecked(field, this),
						BinaryDtoReflectionTool.getUnchecked(field, obj)));
	}

	@Override
	public final int hashCode(){
		Object[] fieldValues = getFieldValuesArray();
		return Arrays.deepHashCode(fieldValues);
	}

	@Override
	public final String toString(){
		return scanFieldNamesAndValues()
				.map(nameAndValue -> {
					Object value = nameAndValue.value;
					String valueString;
					if(value == null){
						valueString = null;
					}else if(value.getClass().isArray()){
						Class<?> clazz = value.getClass();
						if(clazz == byte[].class){
							valueString = Arrays.toString((byte[])value);
						}else if(clazz == boolean[].class){
							valueString = Arrays.toString((boolean[])value);
						}else if(clazz == short[].class){
							valueString = Arrays.toString((short[])value);
						}else if(clazz == char[].class){
							valueString = Arrays.toString((char[])value);
						}else if(clazz == int[].class){
							valueString = Arrays.toString((int[])value);
						}else if(clazz == float[].class){
							valueString = Arrays.toString((float[])value);
						}else if(clazz == long[].class){
							valueString = Arrays.toString((long[])value);
						}else if(clazz == double[].class){
							valueString = Arrays.toString((double[])value);
						}else{
							valueString = Arrays.deepToString((Object[])value);
						}
					}else{
						valueString = value.toString();
					}
					return String.format("%s=%s", nameAndValue.name, valueString);
				})
				.collect(Collectors.joining(", ", getClass().getSimpleName() + " [", "]"));
	}

	@SuppressWarnings("unchecked")
	public final Scanner<Field> scanFields(){
		return new BinaryDtoMetadataParser(this).scanFieldsOrdered();
	}

	public final Scanner<String> scanFieldNames(){
		return scanFields()
				.map(Field::getName);
	}

	public final Scanner<Object> scanFieldValues(){
		return scanFields()
				.map(field -> BinaryDtoReflectionTool.getUnchecked(field, this));
	}

	public final Scanner<FieldNameAndValue> scanFieldNamesAndValues(){
		return scanFields()
				.map(field -> new FieldNameAndValue(
						field.getName(),
						BinaryDtoReflectionTool.getUnchecked(field, this)));
	}

	public final Object[] getFieldValuesArray(){
		return scanFieldValues()
				.toArray();
	}

	private static class FieldNameAndValue{
		public final String name;
		public final Object value;

		public FieldNameAndValue(String name, Object value){
			this.name = name;
			this.value = value;
		}
	}

}
