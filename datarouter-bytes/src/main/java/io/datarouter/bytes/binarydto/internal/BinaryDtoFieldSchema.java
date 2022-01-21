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

public class BinaryDtoFieldSchema<F>{

	public final Field field;
	private final boolean isNullable;
	private final BinaryDtoBaseFieldCodec<F> codec;

	@SuppressWarnings("unchecked")
	public BinaryDtoFieldSchema(Field field){
		this.field = field;
		var fieldMetadataParser = new BinaryDtoFieldMetadataParser<>(field);
		isNullable = fieldMetadataParser.isNullable();
		codec = (BinaryDtoBaseFieldCodec<F>)BinaryDtoFieldCodecs.getCodecForField(field);
	}

	public byte[] encodeField(Object dto){
		F fieldValue = getFieldValue(dto);
		if(!isNullable && fieldValue == null){
			String message = String.format(
					"field=%s of class=%s can't contain nulls",
					field.getName(),
					dto.getClass().getCanonicalName());
			throw new IllegalArgumentException(message);
		}
		return encodeFieldValue(fieldValue);
	}

	private byte[] encodeFieldValue(F fieldValue){
		if(isNullable){
			if(fieldValue == null){
				return BinaryDtoNullFieldTool.NULL_INDICATOR_TRUE_ARRAY;
			}else{
				return ByteTool.concat(
						BinaryDtoNullFieldTool.NULL_INDICATOR_FALSE_ARRAY,
						codec.encode(fieldValue));
			}
		}else{
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

	public int compareFieldValuesAsIfEncoded(Object left, Object right){
		F leftValue = getFieldValue(left);
		F rightValue = getFieldValue(right);
		if(leftValue == null && rightValue == null){
			return 0;
		}else if(leftValue == null){
			return -1;
		}else if(rightValue == null){
			return 1;
		}else{
			return codec.compareAsIfEncoded(leftValue, rightValue);
		}
	}

	@SuppressWarnings("unchecked")
	private F getFieldValue(Object dto){
		return (F)BinaryDtoReflectionTool.getUnchecked(field, dto);
	}

}
