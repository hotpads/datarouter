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
import java.util.Arrays;
import java.util.List;

import io.datarouter.binarydto.dto.BaseBinaryDto;
import io.datarouter.binarydto.internal.BinaryDtoAllocator;
import io.datarouter.binarydto.internal.BinaryDtoFieldCache;
import io.datarouter.binarydto.internal.BinaryDtoFieldSchema;
import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.varint.VarIntTool;

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
public class BinaryDtoIndexedCodec<T extends BaseBinaryDto<T>>
implements Codec<T,byte[]>{

	private static final int TOKENS_PER_FIELD = 3;// index, length, value

	public final BinaryDtoFieldCache<T> fieldCache;

	private BinaryDtoIndexedCodec(BinaryDtoFieldCache<T> fieldCache){
		this.fieldCache = fieldCache;
	}

	/**
	 * For max performance you can keep the reference to the returned object.
	 * It will avoid a ConcurrentHashMap lookup and object allocation on future calls.
	 */
	public static <T extends BaseBinaryDto<T>>
	BinaryDtoIndexedCodec<T> of(Class<T> dtoClass){
		BinaryDtoFieldCache<T> fieldCache = BinaryDtoFieldCache.of(dtoClass);
		return new BinaryDtoIndexedCodec<>(fieldCache);
	}

	/*---------- encode ------------*/

	@Override
	public byte[] encode(T dto){
		List<byte[]> tokens = new ArrayList<>(TOKENS_PER_FIELD * fieldCache.fieldSchemaByIndex.size());
		for(int i = 0; i < fieldCache.fieldSchemaByIndex.size(); ++i){
			BinaryDtoFieldSchema<?> fieldSchema = fieldCache.fieldSchemaByIndex.get(i);
			if(fieldSchema != null){
				if(fieldSchema.isNull(dto)){
					if(!fieldSchema.isNullable()){
						String message = String.format(
								"field=%s of class=%s can't contain nulls",
								fieldSchema.getName(),
								dto.getClass().getCanonicalName());
						throw new IllegalArgumentException(message);
					}
				}else{
					byte[] fieldBytes = fieldSchema.encodeIndexed(dto);
					tokens.add(VarIntTool.encode(i));
					tokens.add(VarIntTool.encode(fieldBytes.length));
					tokens.add(fieldBytes);
				}
			}
		}
		return ByteTool.concat(tokens);
	}

	@Override
	public T decode(byte[] bytes){
		return decode(bytes, 0, bytes.length);
	}

	public T decode(byte[] bytes, int offset, int length){
		int to = offset + length;
		T dto = BinaryDtoAllocator.allocate(fieldCache.dtoClass);
		int cursor = offset;
		while(cursor < to){
			int index = VarIntTool.decodeInt(bytes, cursor);
			cursor += VarIntTool.length(index);
			int fieldLength = VarIntTool.decodeInt(bytes, cursor);
			cursor += VarIntTool.length(fieldLength);
			int valueFrom = cursor;
			int valueTo = cursor + fieldLength;
			cursor = valueTo;
			BinaryDtoFieldSchema<?> fieldSchema = index < fieldCache.fieldSchemaByIndex.size()
					? fieldCache.fieldSchemaByIndex.get(index)
					: null;
			if(fieldSchema != null){
				byte[] fieldBytes = Arrays.copyOfRange(bytes, valueFrom, valueTo);
				fieldSchema.decodeIndexed(dto, fieldBytes);
			}
		}
		return dto;
	}

}
