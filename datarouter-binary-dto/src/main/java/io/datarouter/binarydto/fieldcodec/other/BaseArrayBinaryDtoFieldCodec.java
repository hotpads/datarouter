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
package io.datarouter.binarydto.fieldcodec.other;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;
import io.datarouter.binarydto.internal.BinaryDtoNullFieldTool;
import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.VarIntTool;

public abstract class BaseArrayBinaryDtoFieldCodec<T,A>
extends BinaryDtoBaseFieldCodec<A>{

	private final BinaryDtoBaseFieldCodec<T> itemCodec;
	private final boolean isNullableItems;

	public BaseArrayBinaryDtoFieldCodec(BinaryDtoBaseFieldCodec<T> itemCodec, boolean isNullableItems){
		this.itemCodec = itemCodec;
		this.isNullableItems = isNullableItems;
	}

	@Override
	public boolean supportsComparableCodec(){
		return false;
	}

	//TODO should it include the size?  It makes decoding more efficient but ruins comparability
	public byte[] encodeInternal(List<T> value){
		int size = value.size();
		List<byte[]> byteArrays = new ArrayList<>(1 + 2 * size);//null indicator and value for each item
		byte[] sizeBytes = VarIntTool.encode(size);
		byteArrays.add(sizeBytes);
		for(T item : value){
			if(isNullableItems){
				if(item == null){
					byteArrays.add(BinaryDtoNullFieldTool.NULL_INDICATOR_TRUE_ARRAY);
				}else{
					byteArrays.add(BinaryDtoNullFieldTool.NULL_INDICATOR_FALSE_ARRAY);
					byte[] itemBytes = itemCodec.encode(item);
					if(itemCodec.isVariableLength()){
						byteArrays.add(VarIntTool.encode(itemBytes.length));
					}
					byteArrays.add(itemBytes);
				}
			}else{
				if(item == null){
					throw new IllegalArgumentException("Cannot contain nulls");
				}else{
					byte[] itemBytes = itemCodec.encode(item);
					if(itemCodec.isVariableLength()){
						byteArrays.add(VarIntTool.encode(itemBytes.length));
					}
					byteArrays.add(itemBytes);
				}
			}
		}
		return ByteTool.concat(byteArrays);
	}

	public List<T> decodeInternal(byte[] bytes, final int offset, int length){
		int cursor = offset;
		int size = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(size);
		List<T> value = new ArrayList<>(size);
		for(int i = 0; i < size; ++i){
			T item = null;
			if(isNullableItems){
				boolean isItemNull = BinaryDtoNullFieldTool.decodeNullIndicator(bytes[cursor]);
				cursor += BinaryDtoNullFieldTool.NULL_INDICATOR_LENGTH;
				if(!isItemNull){
					if(itemCodec.isFixedLength()){
						item = itemCodec.decode(bytes, cursor, itemCodec.fixedLength());
						cursor += itemCodec.fixedLength();
					}else{
						int itemLength = VarIntTool.decodeInt(bytes, cursor);
						cursor += VarIntTool.length(itemLength);
						item = itemCodec.decode(bytes, cursor, itemLength);
						cursor += itemLength;
					}
				}
			}else{
				if(itemCodec.isFixedLength()){
					item = itemCodec.decode(bytes, cursor, itemCodec.fixedLength());
					cursor += itemCodec.fixedLength();
				}else{
					int itemLength = VarIntTool.decodeInt(bytes, cursor);
					cursor += VarIntTool.length(itemLength);
					item = itemCodec.decode(bytes, cursor, itemLength);
					cursor += itemLength;
				}
			}
			value.add(item);
		}
		int foundLength = cursor - offset;
		if(length != foundLength){
			String message = String.format("length mismatch, expectedLength=%s, foundLength=%s", length, foundLength);
			throw new IllegalStateException(message);
		}
		return value;
	}

}
