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
package io.datarouter.binarydto.codec;

import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.compress.gzip.GzipTool;

public class BinaryDtoIndexedGzipCodec<T extends BinaryDto<T>>
implements Codec<T,byte[]>{

	private final BinaryDtoIndexedCodec<T> indexedCodec;

	public BinaryDtoIndexedGzipCodec(Class<T> dtoClass){
		indexedCodec = BinaryDtoIndexedCodec.of(dtoClass);
	}

	@Override
	public byte[] encode(T value){
		byte[] indexedBytes = indexedCodec.encode(value);
		return GzipTool.encode(indexedBytes);
	}

	@Override
	public T decode(byte[] gzipBytes){
		byte[] indexedBytes = GzipTool.decode(gzipBytes);
		return indexedCodec.decode(indexedBytes);
	}

}
