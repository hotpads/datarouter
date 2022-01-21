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
package io.datarouter.bytes.binarydto.fieldcodec.array;

import java.util.Arrays;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.LengthAndValue;
import io.datarouter.bytes.VarIntTool;
import io.datarouter.bytes.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;
import io.datarouter.bytes.codec.array.intarray.ComparableIntArrayCodec;

public class IntArrayBinaryDtoFieldCodec extends BinaryDtoBaseFieldCodec<int[]>{

	private static final ComparableIntArrayCodec CODEC = ComparableIntArrayCodec.INSTANCE;

	@Override
	public byte[] encode(int[] value){
		byte[] sizeBytes = VarIntTool.encode(value.length);
		byte[] valueBytes = CODEC.encode(value);
		return ByteTool.concat(sizeBytes, valueBytes);
	}

	@Override
	public LengthAndValue<int[]> decodeWithLength(byte[] bytes, int offset){
		int cursor = offset;
		int size = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(size);
		int bytesLength = size * CODEC.itemLength();
		int[] value = CODEC.decode(bytes, cursor, bytesLength);
		cursor += bytesLength;
		int length = cursor - offset;
		return new LengthAndValue<>(length, value);
	}

	@Override
	public int compareAsIfEncoded(int[] left, int[] right){
		int sizeDiff = Integer.compare(left.length, right.length);
		if(sizeDiff != 0){
			return sizeDiff;
		}
		return Arrays.compare(left, right);
	}

}
