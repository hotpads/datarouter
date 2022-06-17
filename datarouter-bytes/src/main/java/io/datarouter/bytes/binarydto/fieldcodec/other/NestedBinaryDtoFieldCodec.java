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

import io.datarouter.bytes.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.bytes.binarydto.dto.BaseBinaryDto;
import io.datarouter.bytes.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;

public class NestedBinaryDtoFieldCodec<T extends BaseBinaryDto<T>>
extends BinaryDtoBaseFieldCodec<T>{

	private final BinaryDtoIndexedCodec<T> codec;

	public NestedBinaryDtoFieldCodec(Class<T> dtoClass){
		this.codec = BinaryDtoIndexedCodec.of(dtoClass);
	}

	@Override
	public boolean supportsComparableCodec(){
		return false;
	}

	@Override
	public byte[] encode(T value){
		return codec.encode(value);
	}

	@Override
	public T decode(byte[] bytes, int offset, int length){
		return codec.decode(bytes, offset, length);
	}

}
