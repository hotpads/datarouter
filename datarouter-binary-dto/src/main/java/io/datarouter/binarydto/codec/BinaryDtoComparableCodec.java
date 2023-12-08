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
package io.datarouter.binarydto.codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.datarouter.binarydto.dto.ComparableBinaryDto;
import io.datarouter.binarydto.internal.BinaryDtoAllocator;
import io.datarouter.binarydto.internal.BinaryDtoFieldSchema;
import io.datarouter.binarydto.internal.BinaryDtoNullFieldTool;
import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.LengthAndValue;

public class BinaryDtoComparableCodec<T extends ComparableBinaryDto<T>>
implements Codec<T,byte[]>{

	private static final Map<Class<? extends ComparableBinaryDto<?>>,BinaryDtoComparableCodec<?>> CACHE
			= new ConcurrentHashMap<>();

	private static final int MAX_TOKENS_PER_FIELD = 2;// nullable, possible value

	public final BinaryDtoFieldCache<T> fieldCache;

	private BinaryDtoComparableCodec(Class<T> dtoClass){
		fieldCache = new BinaryDtoFieldCache<>(dtoClass);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T extends ComparableBinaryDto<T>>
	BinaryDtoComparableCodec<T> of(Class<? extends T> dtoClass){
		//Can't use computeIfAbsent here because it prohibits recursive calls to this method.  We may therefore
		// generate a few extra throwaway codecs.
		BinaryDtoComparableCodec<?> codec = CACHE.get(dtoClass);
		if(codec == null){
			codec = new BinaryDtoComparableCodec(dtoClass);
			CACHE.put(dtoClass, codec);
		}
		return (BinaryDtoComparableCodec<T>) codec;
	}

	/*---------- encode ------------*/

	@Override
	public byte[] encode(T dto){
		return encodePrefix(dto, fieldCache.fieldSchemaByIndex.size());
	}

	/*---------- encode prefix ------------*/

	public byte[] encodePrefix(T dto, int numFields){
		List<byte[]> tokens = new ArrayList<>(MAX_TOKENS_PER_FIELD * fieldCache.fieldSchemaByIndex.size());
		for(int i = 0; i < numFields; ++i){
			BinaryDtoFieldSchema<?> fieldSchema = fieldCache.fieldSchemaByIndex.get(i);
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
		return decodeWithLength(bytes, 0).value();
	}

	public T decode(byte[] bytes, int offset){
		return decodeWithLength(bytes, offset).value();
	}

	public LengthAndValue<T> decodeWithLength(byte[] bytes, int offset){
		T dto = BinaryDtoAllocator.allocate(fieldCache.dtoClass);
		int cursor = offset;
		for(int index = 0; index < fieldCache.fieldSchemaByIndex.size(); ++index){
			BinaryDtoFieldSchema<?> fieldSchema = fieldCache.fieldSchemaByIndex.get(index);
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
			BinaryDtoFieldSchema<?> fieldSchema = fieldCache.fieldSchemaByIndex.get(index);
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
		T dto = BinaryDtoAllocator.allocate(fieldCache.dtoClass);
		int cursor = 0;
		for(int index = 0; index < numFields; ++index){
			BinaryDtoFieldSchema<?> fieldSchema = fieldCache.fieldSchemaByIndex.get(index);
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
