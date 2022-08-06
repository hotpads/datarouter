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
package io.datarouter.binarydto.codec.bytearray;

import java.util.List;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BaseBinaryDto;
import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.VarIntTool;
import io.datarouter.scanner.Scanner;

public class MultiBinaryDtoEncoder<T extends BaseBinaryDto<T>>{

	private final BinaryDtoIndexedCodec<T> codec;

	public MultiBinaryDtoEncoder(Class<T> dtoClass){
		this.codec = BinaryDtoIndexedCodec.of(dtoClass);
	}

	public MultiBinaryDtoEncoder(BinaryDtoIndexedCodec<T> codec){
		this.codec = codec;
	}

	public Scanner<byte[]> encode(List<T> dtos){
		return encode(Scanner.of(dtos));
	}

	public Scanner<byte[]> encode(Scanner<T> dtos){
		return dtos
				.concat(dto -> {
					byte[] dataBytes = codec.encode(dto);
					byte[] lengthBytes = VarIntTool.encode(dataBytes.length);
					return Scanner.of(lengthBytes, dataBytes);
				});
	}

	public Scanner<byte[]> encodeWithConcatenatedLength(List<T> dtos){
		return encodeWithConcatenatedLength(Scanner.of(dtos));
	}

	public Scanner<byte[]> encodeWithConcatenatedLength(Scanner<T> dtos){
		return dtos
				.map(dto -> {
					byte[] dataBytes = codec.encode(dto);
					byte[] lengthBytes = VarIntTool.encode(dataBytes.length);
					return ByteTool.concat(lengthBytes, dataBytes);
				});
	}

}
