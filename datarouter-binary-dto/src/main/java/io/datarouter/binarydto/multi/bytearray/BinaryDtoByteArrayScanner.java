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
package io.datarouter.binarydto.multi.bytearray;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BaseBinaryDto;
import io.datarouter.bytes.varint.VarIntTool;
import io.datarouter.scanner.BaseScanner;
import io.datarouter.scanner.Scanner;

public class BinaryDtoByteArrayScanner<T extends BaseBinaryDto<T>>
extends BaseScanner<T>{

	private final BinaryDtoIndexedCodec<T> codec;
	private final byte[] bytes;
	private int cursor;

	public BinaryDtoByteArrayScanner(Class<T> dtoClass, byte[] bytes){
		this.codec = BinaryDtoIndexedCodec.of(dtoClass);
		this.bytes = bytes;
		cursor = 0;
	}

	public static <T extends BaseBinaryDto<T>> Scanner<T> of(Class<T> dtoClass, byte[] bytes){
		return new BinaryDtoByteArrayScanner<>(dtoClass, bytes);
	}

	@Override
	public boolean advance(){
		if(cursor == bytes.length){
			return false;
		}
		int length = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(length);
		current = codec.decode(bytes, cursor, length);
		cursor += length;
		return true;
	}

}
