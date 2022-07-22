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
package io.datarouter.binarydto.dto;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.internal.BinaryDtoReflectionTool;
import io.datarouter.bytes.ToStringTool;
import io.datarouter.scanner.Scanner;

public abstract class BaseBinaryDto<T extends BaseBinaryDto<T>>{

	/*---------- codec -------------*/

	protected final BinaryDtoIndexedCodec<T> indexedCodec(){
		return BinaryDtoIndexedCodec.of(getClass());
	}

	@SuppressWarnings("unchecked")
	public final byte[] encodeIndexed(){
		return indexedCodec().encode((T)this);
	}

	public final T cloneIndexed(){
		return indexedCodec().decode(encodeIndexed());
	}

	/*----------- fields -----------*/

	public abstract List<Field> getFieldsOrdered();

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

	public static class FieldNameAndValue{
		public final String name;
		public final Object value;

		public FieldNameAndValue(String name, Object value){
			this.name = name;
			this.value = value;
		}
	}

	/*------------ Object ------------*/

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

}
