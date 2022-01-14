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
package io.datarouter.bytes.binarydto.codec.bytearray;

import io.datarouter.bytes.LengthAndValue;
import io.datarouter.bytes.VarIntTool;
import io.datarouter.bytes.binarydto.codec.BinaryDtoCodec;
import io.datarouter.bytes.binarydto.dto.BaseBinaryDto;
import io.datarouter.scanner.BaseScanner;
import io.datarouter.scanner.Scanner;

public class BinaryDtoByteArrayScanner<T extends BaseBinaryDto>
extends BaseScanner<T>{

	private final BinaryDtoCodec<T> codec;
	private final byte[] bytes;
	private int cursor;

	public BinaryDtoByteArrayScanner(Class<T> dtoClass, byte[] bytes){
		this.codec = new BinaryDtoCodec<>(dtoClass);
		this.bytes = bytes;
		cursor = 0;
	}

	public static <T extends BaseBinaryDto> Scanner<T> of(Class<T> dtoClass, byte[] bytes){
		return new BinaryDtoByteArrayScanner<>(dtoClass, bytes);
	}

	@Override
	public boolean advance(){
		if(cursor == bytes.length){
			return false;
		}
		int length = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(length);
		LengthAndValue<T> lengthAndValue = codec.decodeWithLength(bytes, cursor);
		current = lengthAndValue.value;
		if(length != lengthAndValue.length){
			String message = String.format("Disagreeing lengths: expected=%s, found=%s", length, lengthAndValue.length);
			throw new IllegalStateException(message);
		}
		cursor += length;
		return true;
	}

}
