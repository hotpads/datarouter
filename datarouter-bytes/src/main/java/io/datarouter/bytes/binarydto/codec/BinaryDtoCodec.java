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
import io.datarouter.scanner.Scanner;

public class BinaryDtoCodec<T extends BinaryDto>{

	private static final Map<Class<? extends BinaryDto>,BinaryDtoCodec<?>> CACHE = new ConcurrentHashMap<>();

	public final Class<T> dtoClass;
	public final List<Field> fields;
	public final List<? extends BinaryDtoFieldSchema<?>> fieldSchemas;

	private BinaryDtoCodec(Class<T> dtoClass){
		this.dtoClass = dtoClass;
		fields = new ArrayList<>();
		T dto = BinaryDtoAllocator.allocate(dtoClass);
		var metadataParser = new BinaryDtoMetadataParser<>(dto);
		fieldSchemas = metadataParser.scanFieldsOrdered()
				.each(field -> field.setAccessible(true))
				.each(fields::add)
				.map(BinaryDtoFieldSchema::new)
				.list();
	}

	@SuppressWarnings("unchecked")
	public static <T extends BinaryDto> BinaryDtoCodec<T> of(Class<? extends T> dtoClass){
		//Can't use computeIfAbsent here because it prohibits recursive calls to this method.  We may therefore
		// generate a few extra codecs.
		BinaryDtoCodec<?> codec = CACHE.get(dtoClass);
		if(codec == null){
			codec = new BinaryDtoCodec<>(dtoClass);
			CACHE.put(dtoClass, codec);
		}
		return (BinaryDtoCodec<T>) codec;
	}

	public List<Field> getFieldsOrdered(){
		return fields;
	}

	/*---------- encode ------------*/

	public byte[] encode(T dto){
		return Scanner.of(fieldSchemas)
				.map(field -> field.encodeField(dto))
				.listTo(ByteTool::concat);
	}

	public byte[] encodePrefix(T dto, int numFields){
		return Scanner.of(fieldSchemas)
				.limit(numFields)
				.map(field -> field.encodeField(dto))
				.listTo(ByteTool::concat);
	}

	/*---------- decode ------------*/

	public T decode(byte[] bytes){
		return decodeWithLength(bytes, 0).value;
	}

	public T decode(byte[] bytes, int offset){
		return decodeWithLength(bytes, offset).value;
	}

	public LengthAndValue<T> decodeWithLength(byte[] bytes){
		return decodeWithLength(bytes, 0);
	}

	public LengthAndValue<T> decodeWithLength(byte[] bytes, int offset){
		T dto = BinaryDtoAllocator.allocate(dtoClass);
		int cursor = offset;
		for(BinaryDtoFieldSchema<?> field : fieldSchemas){
			cursor += field.decodeField(dto, bytes, cursor);
		}
		int length = cursor - offset;
		return new LengthAndValue<>(length, dto);
	}

	public T decodePrefix(byte[] bytes, int numFields){
		T dto = BinaryDtoAllocator.allocate(dtoClass);
		int cursor = 0;
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

	/*---------- copy ------------*/

	public T deepCopy(T dto){
		byte[] bytes = encode(dto);
		return decode(bytes);
	}

}
