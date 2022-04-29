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
import io.datarouter.bytes.VarIntTool;
import io.datarouter.bytes.binarydto.dto.BinaryDto;
import io.datarouter.bytes.binarydto.internal.BinaryDtoAllocator;
import io.datarouter.bytes.binarydto.internal.BinaryDtoFieldSchema;
import io.datarouter.bytes.binarydto.internal.BinaryDtoMetadataParser;
import io.datarouter.scanner.Scanner;

/**
 * For encoding non-key fields.
 *
 * The output is not comparable, but it allows adding and removing fields while staying backwards compatible with data
 * serialized by previous dto versions.
 *
 * Note that index re-use is dangerous unless you are certain that persisted data is free of the previous index. There
 * is not much benefit to reusing an index, only a tiny bit if you are exceeding index 127, which is where the varint
 * encoding starts taking a second byte.
 *
 * Fields are made of 3 pieces:
 * - index, as specified in the field's annotation or inferred by alphabetical field order
 * - length, the number of bytes in the value section.
 *     - We need this to know how many bytes to skip in case the field is removed from the dto.
 * - value, the actual value bytes for the field
 */
public class BinaryDtoValueCodec<T extends BinaryDto<T>>{

	private static final Map<Class<? extends BinaryDto<?>>,BinaryDtoValueCodec<?>> CACHE = new ConcurrentHashMap<>();

	private static final int TOKENS_PER_FIELD = 3;// index, length, value

	public final Class<T> dtoClass;
	public final List<Field> fields;
	public final List<? extends BinaryDtoFieldSchema<?>> fieldSchemas;

	private BinaryDtoValueCodec(Class<T> dtoClass){
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
	public static <T extends BinaryDto<T>> BinaryDtoValueCodec<T> of(Class<? extends T> dtoClass){
		//Can't use computeIfAbsent here because it prohibits recursive calls to this method.  We may therefore
		// generate a few extra codecs.
		BinaryDtoValueCodec<?> codec = CACHE.get(dtoClass);
		if(codec == null){
			codec = new BinaryDtoValueCodec(dtoClass);
			CACHE.put(dtoClass, codec);
		}
		return (BinaryDtoValueCodec<T>) codec;
	}

	public List<Field> getFieldsOrdered(){
		return fields;
	}

	/*---------- encode ------------*/

	public byte[] encode(T dto){
		List<byte[]> tokens = new ArrayList<>(TOKENS_PER_FIELD * fieldSchemas.size());
		for(int i = 0; i < fieldSchemas.size(); ++i){
			BinaryDtoFieldSchema<?> fieldSchema = fieldSchemas.get(i);
			if(fieldSchema != null && !fieldSchema.isNull(dto)){
				byte[] fieldBytes = fieldSchema.encodeValue(dto);
				tokens.add(VarIntTool.encode(i));
				tokens.add(VarIntTool.encode(fieldBytes.length));
				tokens.add(fieldBytes);
			}
		}
		return ByteTool.concat(tokens);
	}

}
