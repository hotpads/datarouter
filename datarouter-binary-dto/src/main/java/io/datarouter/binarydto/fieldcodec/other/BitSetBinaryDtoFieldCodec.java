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

import java.util.Arrays;
import java.util.BitSet;

import io.datarouter.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;

public class BitSetBinaryDtoFieldCodec extends BinaryDtoBaseFieldCodec<BitSet>{

	@Override
	public boolean supportsComparableCodec(){
		return false;
	}

	@Override
	public byte[] encode(BitSet value){
		return value.toByteArray();
	}

	@Override
	public BitSet decode(byte[] bytes, int offset, int length){
		if(offset == 0 && length == bytes.length){
			return BitSet.valueOf(bytes);
		}
		byte[] exactBytes = Arrays.copyOfRange(bytes, offset, offset + length);
		return BitSet.valueOf(exactBytes);
	}

}
