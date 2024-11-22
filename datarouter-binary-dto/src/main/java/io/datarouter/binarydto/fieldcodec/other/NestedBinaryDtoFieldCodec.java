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

import java.util.function.Supplier;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BaseBinaryDto;
import io.datarouter.binarydto.fieldcodec.BinaryDtoBaseFieldCodec;
import io.datarouter.binarydto.internal.BinaryDtoSingletonSupplier;

public class NestedBinaryDtoFieldCodec<T extends BaseBinaryDto<T>>
extends BinaryDtoBaseFieldCodec<T>{

	private final BinaryDtoSingletonSupplier<BinaryDtoIndexedCodec<T>> indexedCodecSupplier;

	public NestedBinaryDtoFieldCodec(Supplier<BinaryDtoIndexedCodec<T>> indexedCodecSupplier){
		this.indexedCodecSupplier = new BinaryDtoSingletonSupplier<>(indexedCodecSupplier);
	}

	@Override
	public boolean supportsComparableCodec(){
		return false;
	}

	@Override
	public byte[] encode(T value){
		return indexedCodecSupplier.get().encode(value);
	}

	@Override
	public T decode(byte[] bytes, int offset, int length){
		return indexedCodecSupplier.get().decode(bytes, offset, length);
	}

}
