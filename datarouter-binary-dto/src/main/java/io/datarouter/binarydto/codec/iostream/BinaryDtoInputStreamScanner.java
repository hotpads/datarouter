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
package io.datarouter.binarydto.codec.iostream;

import java.io.InputStream;
import java.util.Optional;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BaseBinaryDto;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.bytes.InputStreamTool;
import io.datarouter.bytes.VarIntTool;
import io.datarouter.scanner.BaseScanner;
import io.datarouter.scanner.Scanner;

public class BinaryDtoInputStreamScanner<T extends BaseBinaryDto<T>>
extends BaseScanner<T>{

	private final BinaryDtoIndexedCodec<T> codec;
	private final InputStream inputStream;

	public BinaryDtoInputStreamScanner(Class<T> dtoClass, InputStream inputStream){
		this.codec = BinaryDtoIndexedCodec.of(dtoClass);
		this.inputStream = inputStream;
	}

	public static <T extends BinaryDto<T>> Scanner<T> of(Class<T> dtoClass, InputStream inputStream){
		return new BinaryDtoInputStreamScanner<>(dtoClass, inputStream);
	}

	@Override
	public boolean advance(){
		Optional<Long> size = VarIntTool.fromInputStream(inputStream);
		if(size.isEmpty()){
			return false;
		}
		byte[] dtoBytes = InputStreamTool.readNBytes(inputStream, size.get().intValue());
		current = codec.decode(dtoBytes);
		return true;
	}

	@Override
	public void close(){
		InputStreamTool.close(inputStream);
	}

}
