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
package io.datarouter.bytes;

import java.util.Arrays;

import io.datarouter.scanner.BaseScanner;
import io.datarouter.scanner.Scanner;

public class ExtractFromPrependedLengthByteArrayScanner extends BaseScanner<byte[]>{

	private final byte[] bytes;
	private int cursor;

	public ExtractFromPrependedLengthByteArrayScanner(byte[] bytes){
		this.bytes = bytes;
		cursor = 0;
	}

	public static Scanner<byte[]> of(byte[] bytes){
		return new ExtractFromPrependedLengthByteArrayScanner(bytes);
	}

	@Override
	public boolean advance(){
		if(cursor == bytes.length){
			return false;
		}
		int length = VarIntTool.decodeInt(bytes, cursor);
		cursor += VarIntTool.length(length);
		current = Arrays.copyOfRange(bytes, cursor, cursor + length);
		cursor += length;
		return true;
	}

}