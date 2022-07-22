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
package io.datarouter.binarydto.fieldcodec.primitive;

import io.datarouter.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;
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
	public boolean supportsComparableCodec(){
		return true;
	}

	@Override
	public byte[] encode(Long value){
		return CODEC.encode(value);
	}

	@Override
	public Long decode(byte[] bytes, int offset, int length){
		return CODEC.decode(bytes, offset);
	}

	@Override
	public int compareAsIfEncoded(Long left, Long right){
		return Long.compare(left, right);
	}

}
