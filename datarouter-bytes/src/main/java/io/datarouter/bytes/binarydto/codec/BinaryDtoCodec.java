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
package io.datarouter.bytes.binarydto.codec;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.LengthAndValue;
import io.datarouter.bytes.binarydto.dto.BinaryDto;
import io.datarouter.bytes.binarydto.internal.BinaryDtoAllocator;
import io.datarouter.bytes.binarydto.internal.BinaryDtoFieldSchema;
import io.datarouter.bytes.binarydto.internal.BinaryDtoMetadataParser;
import io.datarouter.bytes.binarydto.internal.BinaryDtoNullFieldTool;
import io.datarouter.scanner.Scanner;

public class BinaryDtoCodec<T extends BinaryDto<T>>{

	private static final Map<Class<? extends BinaryDto<?>>,BinaryDtoCodec<?>> CACHE = new ConcurrentHashMap<>();

	private static final int MAX_TOKENS_PER_FIELD = 2;// nullable, possible value

	public final Class<T> dtoClass;
	public final List<Field> fields;
	public final List<? extends BinaryDtoFieldSchema<?>> fieldSchemas;

	private BinaryDtoCodec(Class<T> dtoClass){
		this.dtoClass = dtoClass;
		T dto = BinaryDtoAllocator.allocate(dtoClass);
		var metadataParser = new BinaryDtoMetadataParser<>(dto);
		fields = metadataParser.listFields();
		fieldSchemas = Scanner.of(fields)
				.map(field -> {
					if(field == null){
						return null;
					}
					field.setAccessible(true);
					BinaryDtoFieldSchema<?> fieldSchema = new BinaryDtoFieldSchema<>(field);
					return fieldSchema;
				})
				.list();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T extends BinaryDto<T>> BinaryDtoCodec<T> of(Class<? extends T> dtoClass){
		//Can't use computeIfAbsent here because it prohibits recursive calls to this method.  We may therefore
		// generate a few extra codecs.
		BinaryDtoCodec<?> codec = CACHE.get(dtoClass);
		if(codec == null){
			codec = new BinaryDtoCodec(dtoClass);
			CACHE.put(dtoClass, codec);
		}
		return (BinaryDtoCodec<T>) codec;
	}

	public List<Field> getFieldsOrdered(){
		return fields;
	}

	/*---------- encode ------------*/

	public byte[] encode(T dto){
		return encodePrefix(dto, fieldSchemas.size());
	}

	/*---------- encode prefix ------------*/

	public byte[] encodePrefix(T dto, int numFields){
		List<byte[]> tokens = new ArrayList<>(MAX_TOKENS_PER_FIELD * fieldSchemas.size());
		for(int i = 0; i < numFields; ++i){
			BinaryDtoFieldSchema<?> fieldSchema = fieldSchemas.get(i);
			if(fieldSchema != null){
				if(fieldSchema.isNullable()){
					if(fieldSchema.isNull(dto)){
						tokens.add(BinaryDtoNullFieldTool.NULL_INDICATOR_TRUE_ARRAY);
					}else{
						tokens.add(BinaryDtoNullFieldTool.NULL_INDICATOR_FALSE_ARRAY);
						tokens.add(fieldSchema.encodeValue(dto));
					}
				}else{
					if(fieldSchema.isNull(dto)){
						String message = String.format(
								"field=%s of class=%s can't contain nulls",
								fieldSchema.getName(),
								dto.getClass().getCanonicalName());
						throw new IllegalArgumentException(message);
					}else{
						tokens.add(fieldSchema.encodeValue(dto));
					}
				}
			}
		}
		return ByteTool.concat(tokens);
	}

	/*---------- decode ------------*/

	public T decode(byte[] bytes){
		return decodeWithLength(bytes, 0).value;
	}

	public LengthAndValue<T> decodeWithLength(byte[] bytes, int offset){
		T dto = BinaryDtoAllocator.allocate(dtoClass);
		int cursor = offset;
		for(BinaryDtoFieldSchema<?> fieldSchema : fieldSchemas){
			cursor += fieldSchema.decodeField(dto, bytes, cursor);
		}
		int length = cursor - offset;
		return new LengthAndValue<>(length, dto);
	}

	/*---------- decode prefix ------------*/

	public int decodePrefixLength(byte[] bytes, int offset, int numFields){
		int cursor = offset;
		int numFieldsDecoded = 0;
		for(BinaryDtoFieldSchema<?> field : fieldSchemas){
			cursor += field.decodeFieldLength(bytes, cursor);
			++numFieldsDecoded;
			if(numFieldsDecoded == numFields){
				break;
			}
		}
		return cursor - offset;
	}

	public T decodePrefix(byte[] bytes, int offset, int numFields){
		T dto = BinaryDtoAllocator.allocate(dtoClass);
		int cursor = offset;
		int numFieldsDecoded = 0;
		for(BinaryDtoFieldSchema<?> field : fieldSchemas){
			cursor += field.decodeField(dto, bytes, cursor);
			++numFieldsDecoded;
			if(numFieldsDecoded == numFields){
				break;
			}
		}
		return dto;
	}

}
