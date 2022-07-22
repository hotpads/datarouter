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
package io.datarouter.binarydto.fieldcodec.array;

import java.util.Arrays;

import io.datarouter.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;

/**
 * Bytes are stored without transformation.
 *
 * When comparing:
 *  - shortest arrays are first due to the length prefix
 *  - positive values are before negative values due to unsigned comparison
 */
public class ByteArrayBinaryDtoFieldCodec extends BinaryDtoBaseFieldCodec<byte[]>{

	@Override
	public boolean supportsComparableCodec(){
		return true;
	}

	@Override
	public byte[] encode(byte[] value){
		return value;
	}

	@Override
	public byte[] decode(byte[] bytes, int offset, int length){
		return Arrays.copyOfRange(bytes, offset, offset + length);
	}

	@Override
	public int compareAsIfEncoded(byte[] left, byte[] right){
		return Arrays.compareUnsigned(left, right);
	}

}
