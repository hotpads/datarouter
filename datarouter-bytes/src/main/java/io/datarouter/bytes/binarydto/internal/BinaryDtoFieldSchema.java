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

	public String getName(){
		return field.getName();
	}

	public boolean isNullable(){
		return isNullable;
	}

	public boolean isNull(Object dto){
		return getFieldValue(dto) == null;
	}

	public boolean isFixedLength(){
		return codec.isFixedLength();
	}

	public byte[] encodeValue(Object dto){
		F fieldValue = getFieldValue(dto);
		return codec.encode(fieldValue);
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
		setFieldValue(object, fieldValue);
		return cursor - offset;
	}

	public int decodeFieldLength(byte[] bytes, final int offset){
		int cursor = offset;
		if(isNullable){
			boolean isNull = BinaryDtoNullFieldTool.decodeNullIndicator(bytes[cursor]);
			cursor += BinaryDtoNullFieldTool.NULL_INDICATOR_LENGTH;
			if(!isNull){
				cursor += codec.decodeLength(bytes, cursor);
			}
		}else{
			cursor += codec.decodeLength(bytes, cursor);
		}
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

	public void setFieldValue(Object dto, F fieldValue){
		BinaryDtoReflectionTool.setUnchecked(field, dto, fieldValue);
	}

}
