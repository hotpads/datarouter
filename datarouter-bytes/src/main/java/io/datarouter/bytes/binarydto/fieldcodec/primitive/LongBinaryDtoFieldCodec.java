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
package io.datarouter.bytes.binarydto.fieldcodec.primitive;

import io.datarouter.bytes.LengthAndValue;
import io.datarouter.bytes.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;
import io.datarouter.bytes.codec.longcodec.ComparableLongCodec;

public class LongBinaryDtoFieldCodec extends BinaryDtoBaseFieldCodec<Long>{

	private static final ComparableLongCodec CODEC = ComparableLongCodec.INSTANCE;

	@Override
	public boolean isFixedLength(){
		return true;
	}

	@Override
	public int fixedLength(){
		return CODEC.length();
	}

	@Override
	public byte[] encode(Long value){
		return CODEC.encode(value);
	}

	@Override
	public Long decode(byte[] bytes, int offset){
		return CODEC.decode(bytes, offset);
	}

	@Override
	public LengthAndValue<Long> decodeWithLength(byte[] bytes, int offset){
		int length = fixedLength();
		long value = decode(bytes, offset);
		return new LengthAndValue<>(length, value);
	}

}
