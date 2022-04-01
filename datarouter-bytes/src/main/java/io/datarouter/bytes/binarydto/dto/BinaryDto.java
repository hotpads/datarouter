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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.datarouter.bytes.ToStringTool;
import io.datarouter.bytes.binarydto.codec.BinaryDtoCodec;
import io.datarouter.bytes.binarydto.internal.BinaryDtoFieldSchema;
import io.datarouter.bytes.binarydto.internal.BinaryDtoReflectionTool;
import io.datarouter.scanner.Scanner;

public abstract class BinaryDto<T extends BinaryDto<T>>
implements Comparable<T>{

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
		return Scanner.of(getFieldsOrdered())
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
					String valueString = ToStringTool.toString(value);
					return String.format("%s=%s", nameAndValue.name, valueString);
				})
				.collect(Collectors.joining(", ", getClass().getSimpleName() + " [", "]"));
	}

	@Override
	public int compareTo(T that){
		Objects.requireNonNull(that);
		if(!getClass().equals(that.getClass())){
			String message = String.format("Cannot compare %s to %s",
					getClass().getCanonicalName(),
					that.getClass().getCanonicalName());
			throw new IllegalArgumentException(message);
		}
		BinaryDtoCodec<T> codec = BinaryDtoCodec.of(getClass());
		for(BinaryDtoFieldSchema<?> fieldSchema : codec.fieldSchemas){
			int fieldDiff = fieldSchema.compareFieldValuesAsIfEncoded(this, that);
			if(fieldDiff != 0){
				return fieldDiff;
			}
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	public final List<Field> getFieldsOrdered(){
		return BinaryDtoCodec.of(getClass()).getFieldsOrdered();
	}

	public final Object[] getFieldValuesArray(){
		List<Field> fields = getFieldsOrdered();
		int numFields = fields.size();
		var values = new Object[numFields];
		for(int i = 0; i < numFields; ++i){
			Field field = fields.get(i);
			values[i] = BinaryDtoReflectionTool.getUnchecked(field, this);
		}
		return values;
	}

	public final Scanner<String> scanFieldNames(){
		return Scanner.of(getFieldsOrdered())
				.map(Field::getName);
	}

	public final Scanner<Object> scanFieldValues(){
		return Scanner.of(getFieldsOrdered())
				.map(field -> BinaryDtoReflectionTool.getUnchecked(field, this));
	}

	public final Scanner<FieldNameAndValue> scanFieldNamesAndValues(){
		return Scanner.of(getFieldsOrdered())
				.map(field -> new FieldNameAndValue(
						field.getName(),
						BinaryDtoReflectionTool.getUnchecked(field, this)));
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
