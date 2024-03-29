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
import io.datarouter.bytes.codec.array.longarray.ComparableLongArrayCodec;

public class LongArrayBinaryDtoFieldCodec extends BinaryDtoBaseFieldCodec<long[]>{

	private static final ComparableLongArrayCodec CODEC = ComparableLongArrayCodec.INSTANCE;

	@Override
	public boolean supportsComparableCodec(){
		return true;
	}

	@Override
	public byte[] encode(long[] value){
		return CODEC.encode(value);
	}

	@Override
	public long[] decode(byte[] bytes, int offset, int length){
		return CODEC.decode(bytes, offset, length);
	}

	@Override
	public int compareAsIfEncoded(long[] left, long[] right){
		return Arrays.compare(left, right);
	}

}
