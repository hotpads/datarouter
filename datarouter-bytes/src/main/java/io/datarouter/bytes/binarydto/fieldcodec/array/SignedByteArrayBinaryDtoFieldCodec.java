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

import io.datarouter.bytes.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;
import io.datarouter.bytes.codec.array.bytearray.ComparableByteArrayCodec;

/**
 * Bytes are flipped to compare like numbers.  This is not the default FieldCodec for byte arrays.
 */
public class SignedByteArrayBinaryDtoFieldCodec extends BinaryDtoBaseFieldCodec<byte[]>{

	private static final ComparableByteArrayCodec CODEC = ComparableByteArrayCodec.INSTANCE;

	@Override
	public boolean supportsComparableCodec(){
		return true;
	}

	@Override
	public byte[] encode(byte[] value){
		return CODEC.encode(value);
	}

	@Override
	public byte[] decode(byte[] bytes, int offset, int length){
		return CODEC.decode(bytes, offset, length);
	}

	@Override
	public int compareAsIfEncoded(byte[] left, byte[] right){
		return Arrays.compare(left, right);
	}

}
