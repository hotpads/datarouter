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
package io.datarouter.bytes.binarydto.internal;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import io.datarouter.bytes.binarydto.dto.BinaryDtoField;
import io.datarouter.bytes.binarydto.dto.BinaryDtoField.BinaryDtoInvalidCodec;
import io.datarouter.bytes.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;

public class BinaryDtoFieldMetadataParser<T>{

	public final Field field;

	public BinaryDtoFieldMetadataParser(Field field){
		this.field = field;
	}

	public Field getField(){
		return field;
	}

	public String getName(){
		return field.getName();
	}

	public boolean hasIndex(){
		return optIndex().isPresent();
	}

	public Optional<Integer> optIndex(){
		BinaryDtoField annotation = field.getAnnotation(BinaryDtoField.class);
		if(annotation == null){
			return Optional.empty();
		}
		int index = annotation.index();
		if(index == BinaryDtoField.DEFAULT_INDEX){
			return Optional.empty();
		}
		return Optional.of(annotation.index());
	}

	public int getRequiredIndex(){
		return optIndex().orElseThrow(() -> {
			String message = String.format("Field %s is missing index annotation", field.getName());
			throw new IllegalArgumentException(message);
		});
	}

	public boolean isNullable(){
		BinaryDtoField annotation = field.getAnnotation(BinaryDtoField.class);
		if(field.getType().isPrimitive()){
			//TODO reject primitives specified as nullable
			return false;
		}
		if(annotation == null){
			return BinaryDtoField.DEFAULT_NULLABLE;
		}
		return annotation.nullable();
	}

	public boolean isNullableItems(){
		BinaryDtoField annotation = field.getAnnotation(BinaryDtoField.class);
		if(annotation == null){
			return BinaryDtoField.DEFAULT_NULLABLE_ITEMS;
		}
		return annotation.nullableItems();
	}

	public boolean isPrimitive(){
		return field.getType().isPrimitive();
	}

	public boolean isList(){
		return field.getType() == List.class;
	}

	public boolean isArray(){
		return field.getType().isArray();
	}

	public boolean isPrimitiveArray(){
		return isArray() && field.getType().getComponentType().isPrimitive();
	}

	public boolean isObjectArray(){
		return isArray() && !isPrimitiveArray();
	}

	public boolean isEnum(){
		return Enum.class.isAssignableFrom(field.getType());
	}

	public Class<?> getObjectArrayItemClass(){
		return field.getType().getComponentType();
	}

	@SuppressWarnings("unchecked")
	public <I> Class<I> getListItemClass(){
		ParameterizedType parameterizedType = (ParameterizedType)field.getGenericType();
		Type type = parameterizedType.getActualTypeArguments()[0];
		try{
			return (Class<I>)Class.forName(type.getTypeName());
		}catch(ClassNotFoundException e){
			throw new RuntimeException(e);
		}
	}

	public Optional<BinaryDtoBaseFieldCodec<?>> optCodec(){
		return optCodecClass()
				.map(BinaryDtoReflectionTool::newInstanceUnchecked);
	}

	private Optional<Class<? extends BinaryDtoBaseFieldCodec<?>>> optCodecClass(){
		BinaryDtoField annotation = field.getAnnotation(BinaryDtoField.class);
		if(annotation == null){
			return Optional.empty();
		}
		return annotation.codec() == BinaryDtoInvalidCodec.class
				? Optional.empty()
				: Optional.of(annotation.codec());
	}

}
