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

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.LengthAndValue;
import io.datarouter.bytes.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;

public class BinaryDtoFieldSchema<F,V>{

	private final Field field;
	private final boolean isNullable;
	private final BinaryDtoBaseFieldCodec<F> codec;

	@SuppressWarnings("unchecked")
	public BinaryDtoFieldSchema(Field field){
		this.field = field;
		BinaryDtoFieldMetadataParser<F> fieldMetadataParser = new BinaryDtoFieldMetadataParser<>(field);
		isNullable = fieldMetadataParser.isNullable();
		codec = (BinaryDtoBaseFieldCodec<F>)BinaryDtoFieldCodecs.getCodecForField(field);
	}

	public byte[] encodeField(Object dto){
		F fieldValue = getFieldValue(dto);
		if(isNullable){
			if(fieldValue == null){
				return BinaryDtoNullFieldTool.NULL_INDICATOR_TRUE_ARRAY;
			}else{
				return ByteTool.concatenate2(
						BinaryDtoNullFieldTool.NULL_INDICATOR_FALSE_ARRAY,
						codec.encode(fieldValue));
			}
		}else{
			if(fieldValue == null){
				String message = String.format("field=%s of class=%s can't contain nulls",
						field.getName(),
						dto.getClass().getCanonicalName());
				throw new IllegalArgumentException(message);
			}
			return codec.encode(fieldValue);
		}
	}

	public int decodeField(Object object, byte[] bytes, final int offset){
		int cursor = offset;
		F fieldValue = null;
		if(isNullable){
			boolean isNull = BinaryDtoNullFieldTool.decodeNullIndicator(bytes[cursor]);
			cursor += BinaryDtoNullFieldTool.NULL_INDICATOR_LENGTH;
			if(!isNull){
				if(codec.isFixedLength()){
					fieldValue = codec.decode(bytes, cursor);
					cursor += codec.fixedLength();
				}else{
					LengthAndValue<F> lengthAndValue = codec.decodeWithLength(bytes, cursor);
					cursor += lengthAndValue.length;
					fieldValue = lengthAndValue.value;
				}
			}
		}else{
			if(codec.isFixedLength()){
				fieldValue = codec.decode(bytes, cursor);
				cursor += codec.fixedLength();
			}else{
				LengthAndValue<F> lengthAndValue = codec.decodeWithLength(bytes, cursor);
				cursor += lengthAndValue.length;
				fieldValue = lengthAndValue.value;
			}
		}
		BinaryDtoReflectionTool.setUnchecked(field, object, fieldValue);
		return cursor - offset;
	}

	@SuppressWarnings("unchecked")
	private F getFieldValue(Object dto){
		return (F)BinaryDtoReflectionTool.getUnchecked(field, dto);
	}

}
