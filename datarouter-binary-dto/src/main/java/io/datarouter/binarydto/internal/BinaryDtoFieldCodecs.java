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
import java.time.Instant;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.datarouter.binarydto.dto.BaseBinaryDto;
import io.datarouter.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;
import io.datarouter.binarydto.fieldcodec.array.BooleanArrayBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.array.ByteArrayBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.array.CharArrayBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.array.DoubleArrayBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.array.FloatArrayBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.array.IntArrayBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.array.LongArrayBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.array.ShortArrayBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.dataroutertypes.ByteLengthToLongBinaryDtoFieldConverter;
import io.datarouter.binarydto.fieldcodec.dataroutertypes.MilliTimeToLongBinaryDtoFieldConverter;
import io.datarouter.binarydto.fieldcodec.other.BitSetBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.other.EnumBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.other.ListBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.other.NestedBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.other.ObjectArrayBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.primitive.BooleanBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.primitive.ByteBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.primitive.CharBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.primitive.DoubleBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.primitive.FloatBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.primitive.IntBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.primitive.LongBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.primitive.ShortBinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.string.Utf8BinaryDtoFieldCodec;
import io.datarouter.binarydto.fieldcodec.time.InstantBinaryDtoFieldCodec;
import io.datarouter.bytes.ByteLength;
import io.datarouter.types.MilliTime;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BinaryDtoFieldCodecs{

	private static final Map<Class,BinaryDtoBaseFieldCodec<?>> LEAF_CODEC_BY_CLASS = new HashMap<>();
	static{
		//primitives
		LEAF_CODEC_BY_CLASS.put(byte.class, new ByteBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(boolean.class, new BooleanBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(short.class, new ShortBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(char.class, new CharBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(int.class, new IntBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(float.class, new FloatBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(long.class, new LongBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(double.class, new DoubleBinaryDtoFieldCodec());
		//boxed primitives
		LEAF_CODEC_BY_CLASS.put(Byte.class, new ByteBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(Boolean.class, new BooleanBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(Short.class, new ShortBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(Character.class, new CharBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(Integer.class, new IntBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(Float.class, new FloatBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(Long.class, new LongBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(Double.class, new DoubleBinaryDtoFieldCodec());
		//primitive arrays
		LEAF_CODEC_BY_CLASS.put(byte[].class, new ByteArrayBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(boolean[].class, new BooleanArrayBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(short[].class, new ShortArrayBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(char[].class, new CharArrayBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(int[].class, new IntArrayBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(float[].class, new FloatArrayBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(long[].class, new LongArrayBinaryDtoFieldCodec());
		LEAF_CODEC_BY_CLASS.put(double[].class, new DoubleArrayBinaryDtoFieldCodec());
		//string
		LEAF_CODEC_BY_CLASS.put(String.class, new Utf8BinaryDtoFieldCodec());
		//time
		LEAF_CODEC_BY_CLASS.put(Instant.class, new InstantBinaryDtoFieldCodec());
		//other
		LEAF_CODEC_BY_CLASS.put(BitSet.class, new BitSetBinaryDtoFieldCodec());
		//datarouter-types
		LEAF_CODEC_BY_CLASS.put(ByteLength.class, new ByteLengthToLongBinaryDtoFieldConverter());
		LEAF_CODEC_BY_CLASS.put(MilliTime.class, new MilliTimeToLongBinaryDtoFieldConverter());
	}

	public static BinaryDtoBaseFieldCodec<?> getCodecForField(Field field){
		var fieldMetadataParser = new BinaryDtoFieldMetadataParser(field);
		Optional<BinaryDtoBaseFieldCodec<?>> optCustomCodec = fieldMetadataParser.optCodec();
		if(optCustomCodec.isPresent()){
			if(fieldMetadataParser.isObjectArray()){
				return new ObjectArrayBinaryDtoFieldCodec(
						field.getType().getComponentType(),
						optCustomCodec.get(),
						fieldMetadataParser.isNullableItems());
			}else if(fieldMetadataParser.isList()){
				return new ListBinaryDtoFieldCodec<>(
						optCustomCodec.get(),
						fieldMetadataParser.isNullableItems());
			}else{
				return optCustomCodec.get();
			}
		}else{
			if(fieldMetadataParser.isObjectArray()){
				Class itemClass = fieldMetadataParser.getObjectArrayItemClass();
				BinaryDtoBaseFieldCodec<Object> itemCodec = (BinaryDtoBaseFieldCodec)getLeafCodec(itemClass, field);
				return new ObjectArrayBinaryDtoFieldCodec(
						itemClass,
						itemCodec,
						fieldMetadataParser.isNullableItems());
			}else if(fieldMetadataParser.isList()){
				Class<Object> itemClass = fieldMetadataParser.getListItemClass();
				BinaryDtoBaseFieldCodec<Object> itemCodec = (BinaryDtoBaseFieldCodec)getLeafCodec(itemClass, field);
				return new ListBinaryDtoFieldCodec<>(
						itemCodec,
						fieldMetadataParser.isNullableItems());
			}else if(fieldMetadataParser.isEnum()){
				return new EnumBinaryDtoFieldCodec(field.getType());
			}else{
				return getLeafCodec(field.getType(), field);
			}
		}
	}

	private static BinaryDtoBaseFieldCodec<?> getLeafCodec(Class<?> clazz, Field field){
		if(BaseBinaryDto.class.isAssignableFrom(clazz)){
			return new NestedBinaryDtoFieldCodec(clazz);
		}
		var codec = LEAF_CODEC_BY_CLASS.get(clazz);
		if(codec != null){
			return codec;
		}
		var itemClass = field.getType().getSimpleName().equals(clazz.getSimpleName())
				? ""
				: " (" + clazz.getSimpleName() + ")";
		throw new NullPointerException("Codec not found for " + field + itemClass);
	}

}
