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
package io.datarouter.binarydto.internal;

import java.lang.reflect.Field;
import java.util.Objects;

import io.datarouter.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;
import io.datarouter.bytes.LengthAndValue;
import io.datarouter.bytes.codec.array.bytearray.TerminatedByteArrayCodec;

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
		Objects.requireNonNull(codec, "Codec not found for " + field);
	}

	public String getName(){
		return field.getName();
	}

	public String getCodecName(){
		return codec.getClass().getSimpleName();
	}

	public boolean isNullable(){
		return isNullable;
	}

	public boolean isNull(Object dto){
		return getFieldValue(dto) == null;
	}

	public boolean isKeyCompatible(){
		return codec.supportsComparableCodec();
	}

	/*----------- key format ---------------*/

	public byte[] encodeComparable(Object dto){
		F fieldValue = getFieldValue(dto);
		byte[] value = codec.encode(fieldValue);
		return codec.isFixedLength()
				? value
				: TerminatedByteArrayCodec.INSTANCE.encode(value);
	}

	public int decodeComparableLength(byte[] bytes, final int offset){
		return codec.isFixedLength()
				? codec.fixedLength()
				: TerminatedByteArrayCodec.INSTANCE.lengthWithTerminalIndex(bytes, offset);
	}

	public int decodeComparable(Object object, byte[] bytes, final int offset){
		int cursor = offset;
		F fieldValue = null;
		if(codec.isFixedLength()){
			fieldValue = codec.decode(bytes, cursor, codec.fixedLength());
			cursor += codec.fixedLength();
		}else{
			LengthAndValue<byte[]> lengthAndEncodedValue = TerminatedByteArrayCodec.INSTANCE.decode(bytes, cursor);
			cursor += lengthAndEncodedValue.length;
			fieldValue = codec.decode(lengthAndEncodedValue.value);
		}
		setFieldValue(object, fieldValue);
		return cursor - offset;
	}

	/*----------- value format ---------------*/

	public byte[] encodeIndexed(Object dto){
		F fieldValue = getFieldValue(dto);
		return codec.encode(fieldValue);
	}

	public void decodeIndexed(Object object, byte[] bytes){
		F fieldValue = null;
		if(codec.isFixedLength()){
			if(bytes.length != codec.fixedLength()){
				String message = String.format("bytes.length=%s != codec.fixedLength=%s",
						bytes.length,
						codec.fixedLength());
				throw new RuntimeException(message);
			}
		}
		fieldValue = codec.decode(bytes);
		setFieldValue(object, fieldValue);
	}

	/*----------- compare ---------------*/

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

	/*----------- reflection ---------------*/

	@SuppressWarnings("unchecked")
	private F getFieldValue(Object dto){
		return (F)BinaryDtoReflectionTool.getUnchecked(field, dto);
	}

	private void setFieldValue(Object dto, F fieldValue){
		BinaryDtoReflectionTool.setUnchecked(field, dto, fieldValue);
	}

}
