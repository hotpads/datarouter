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
package io.datarouter.bytes.binarydto.fieldcodec.other;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.LengthAndValue;
import io.datarouter.bytes.VarIntTool;
import io.datarouter.bytes.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;
import io.datarouter.bytes.binarydto.internal.BinaryDtoNullFieldTool;

public class ListBinaryDtoFieldCodec<T>
extends BinaryDtoBaseFieldCodec<List<T>>{

	private final BinaryDtoBaseFieldCodec<T> itemCodec;
	private final boolean isNullableItems;

	public ListBinaryDtoFieldCodec(BinaryDtoBaseFieldCodec<T> itemCodec, boolean isNullableItems){
		this.itemCodec = itemCodec;
		this.isNullableItems = isNullableItems;
	}

	@Override
	public byte[] encode(List<T> value){
		int size = value.size();
		List<byte[]> byteArrays = new ArrayList<>(2 * size + 1);//null indicator and value for each item
		byte[] sizeBytes = VarIntTool.encode(size);
		byteArrays.add(sizeBytes);
		for(T item : value){
			if(item == null){
				if(isNullableItems){
					byteArrays.add(BinaryDtoNullFieldTool.NULL_INDICATOR_TRUE_ARRAY);
				}else{
					throw new IllegalArgumentException("Cannot contain nulls");
				}
			}else{
				byteArrays.add(BinaryDtoNullFieldTool.NULL_INDICATOR_FALSE_ARRAY);
				byte[] itemBytes = itemCodec.encode(item);
				byteArrays.add(itemBytes);
			}
		}
		return ByteTool.concatenate(byteArrays);
	}

	@Override
	public List<T> decode(byte[] bytes, int offset){
		return decodeWithLength(bytes, offset).value;
	}

	@Override
	public LengthAndValue<List<T>> decodeWithLength(byte[] bytes, final int offset){
		int cursor = offset;
		int size = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(size);
		List<T> value = new ArrayList<>(size);
		for(int i = 0; i < size; ++i){
			boolean isItemNull = false;
			if(isNullableItems){
				isItemNull = BinaryDtoNullFieldTool.decodeNullIndicator(bytes[cursor]);
				cursor += BinaryDtoNullFieldTool.NULL_INDICATOR_LENGTH;
			}
			if(isItemNull){
				value.add(null);
			}else{
				LengthAndValue<T> lengthAndValue = itemCodec.decodeWithLength(bytes, cursor);
				cursor += lengthAndValue.length;
				value.add(lengthAndValue.value);
			}
		}
		int length = cursor - offset;
		return new LengthAndValue<>(length, value);
	}

}
