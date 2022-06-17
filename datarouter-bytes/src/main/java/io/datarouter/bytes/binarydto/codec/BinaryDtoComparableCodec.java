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
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.LengthAndValue;
import io.datarouter.bytes.binarydto.dto.ComparableBinaryDto;
import io.datarouter.bytes.binarydto.internal.BinaryDtoAllocator;
import io.datarouter.bytes.binarydto.internal.BinaryDtoFieldSchema;
import io.datarouter.bytes.binarydto.internal.BinaryDtoMetadataParser;
import io.datarouter.bytes.binarydto.internal.BinaryDtoNullFieldTool;
import io.datarouter.scanner.Scanner;

public class BinaryDtoComparableCodec<T extends ComparableBinaryDto<T>>
implements Codec<T,byte[]>{

	private static final Map<Class<? extends ComparableBinaryDto<?>>,BinaryDtoComparableCodec<?>> CACHE
			= new ConcurrentHashMap<>();

	private static final int MAX_TOKENS_PER_FIELD = 2;// nullable, possible value

	public final Class<T> dtoClass;
	public final List<Field> fields;
	public final List<? extends BinaryDtoFieldSchema<?>> fieldSchemas;

	private BinaryDtoComparableCodec(Class<T> dtoClass){
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
					if(!fieldSchema.isKeyCompatible()){
						String message = String.format(
								"FieldCodec=%s for field=%s is not compatible with key encoding for Dto=%s",
								fieldSchema.getCodecName(),
								fieldSchema.getName(),
								dtoClass.getCanonicalName());
						throw new IllegalArgumentException(message);
					}
					return fieldSchema;
				})
				.list();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T extends ComparableBinaryDto<T>>
	BinaryDtoComparableCodec<T> of(Class<? extends T> dtoClass){
		//Can't use computeIfAbsent here because it prohibits recursive calls to this method.  We may therefore
		// generate a few extra codecs.
		BinaryDtoComparableCodec<?> codec = CACHE.get(dtoClass);
		if(codec == null){
			codec = new BinaryDtoComparableCodec(dtoClass);
			CACHE.put(dtoClass, codec);
		}
		return (BinaryDtoComparableCodec<T>) codec;
	}

	public List<Field> getFieldsOrdered(){
		return fields;
	}

	/*---------- encode ------------*/

	@Override
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
						tokens.add(fieldSchema.encodeComparable(dto));
					}
				}else{
					if(fieldSchema.isNull(dto)){
						String message = String.format(
								"field=%s of class=%s can't contain nulls",
								fieldSchema.getName(),
								dto.getClass().getCanonicalName());
						throw new IllegalArgumentException(message);
					}else{
						tokens.add(fieldSchema.encodeComparable(dto));
					}
				}
			}
		}
		return ByteTool.concat(tokens);
	}

	/*---------- decode ------------*/

	@Override
	public T decode(byte[] bytes){
		return decodeWithLength(bytes, 0).value;
	}

	public LengthAndValue<T> decodeWithLength(byte[] bytes, int offset){
		T dto = BinaryDtoAllocator.allocate(dtoClass);
		int cursor = offset;
		for(int index = 0; index < fieldSchemas.size(); ++index){
			BinaryDtoFieldSchema<?> fieldSchema = fieldSchemas.get(index);
			boolean isNull = false;
			if(fieldSchema.isNullable()){
				isNull = BinaryDtoNullFieldTool.decodeNullIndicator(bytes[cursor]);
				cursor += BinaryDtoNullFieldTool.NULL_INDICATOR_LENGTH;
			}
			if(!isNull){
				cursor += fieldSchema.decodeComparable(dto, bytes, cursor);
			}
		}
		int length = cursor - offset;
		return new LengthAndValue<>(length, dto);
	}

	/*---------- decode prefix ------------*/

	public int decodePrefixLength(byte[] bytes, int offset, int numFields){
		int cursor = offset;
		for(int index = 0; index < numFields; ++index){
			BinaryDtoFieldSchema<?> fieldSchema = fieldSchemas.get(index);
			boolean isNull = false;
			if(fieldSchema.isNullable()){
				isNull = BinaryDtoNullFieldTool.decodeNullIndicator(bytes[cursor]);
				cursor += BinaryDtoNullFieldTool.NULL_INDICATOR_LENGTH;
			}
			if(!isNull){
				cursor += fieldSchema.decodeComparableLength(bytes, cursor);
			}
		}
		return cursor - offset;
	}

	public T decodePrefix(byte[] bytes, int numFields){
		T dto = BinaryDtoAllocator.allocate(dtoClass);
		int cursor = 0;
		for(int index = 0; index < numFields; ++index){
			BinaryDtoFieldSchema<?> fieldSchema = fieldSchemas.get(index);
			boolean isNull = false;
			if(fieldSchema.isNullable()){
				isNull = BinaryDtoNullFieldTool.decodeNullIndicator(bytes[cursor]);
				cursor += BinaryDtoNullFieldTool.NULL_INDICATOR_LENGTH;
			}
			if(!isNull){
				cursor += fieldSchema.decodeComparable(dto, bytes, cursor);
			}
		}
		return dto;
	}

}
