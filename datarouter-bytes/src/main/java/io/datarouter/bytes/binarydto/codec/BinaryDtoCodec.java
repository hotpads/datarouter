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

import java.util.List;
import java.util.stream.Collectors;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.LengthAndValue;
import io.datarouter.bytes.binarydto.dto.BaseBinaryDto;
import io.datarouter.bytes.binarydto.internal.BinaryDtoAllocator;
import io.datarouter.bytes.binarydto.internal.BinaryDtoFieldSchema;
import io.datarouter.bytes.binarydto.internal.BinaryDtoMetadataParser;
import io.datarouter.scanner.Scanner;

public class BinaryDtoCodec<T extends BaseBinaryDto>{

	public final Class<T> dtoClass;
	public final List<BinaryDtoFieldSchema<?,?>> fieldSchemas;

	public BinaryDtoCodec(Class<T> dtoClass){
		this.dtoClass = dtoClass;
		T dto = BinaryDtoAllocator.allocate(dtoClass);
		BinaryDtoMetadataParser<T> metadataParser = new BinaryDtoMetadataParser<>(dto);
		fieldSchemas = metadataParser.scanFieldsOrdered()
				.each(field -> field.setAccessible(true))
				.map(BinaryDtoFieldSchema::new)
				.collect(Collectors.toList());
	}

	public byte[] encode(T dto){
		return Scanner.of(fieldSchemas)
				.map(field -> field.encodeField(dto))
				.listTo(ByteTool::concatenate);
	}

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
		for(BinaryDtoFieldSchema<?,?> field : fieldSchemas){
			cursor += field.decodeField(dto, bytes, cursor);
		}
		int length = cursor - offset;
		return new LengthAndValue<>(length, dto);
	}

	public T deepCopy(T dto){
		byte[] bytes = encode(dto);
		return decode(bytes);
	}

}
