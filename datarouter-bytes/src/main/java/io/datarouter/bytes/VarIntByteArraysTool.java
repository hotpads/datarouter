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
import java.util.List;

import io.datarouter.scanner.BaseScanner;
import io.datarouter.scanner.Scanner;

/**
 * Concatenate multiple byte arrays into a bigger array.
 * The inner arrays are each prefixed with the output of VarIntTool.
 * Inner arrays must be parsed sequentially.
 * Simple, but no random access.
 */
public class VarIntByteArraysTool{

	public static int encodedLength(List<byte[]> arrays){
		int encodedLength = 0;
		for(byte[] array : arrays){
			encodedLength += VarIntTool.length(array.length);
			encodedLength += array.length;
		}
		return encodedLength;
	}

	public static byte[] encodeOne(byte[] input){
		int dataLength = input.length;
		int headerLength = VarIntTool.length(dataLength);
		int outputLength = headerLength + dataLength;
		byte[] output = new byte[outputLength];
		VarIntTool.encode(output, 0, dataLength);
		System.arraycopy(input, 0, output, headerLength, dataLength);
		return output;
	}

	public static byte[] encodeMulti(Scanner<byte[]> arrays){
		//TODO skip generating intermediate arrays
		return arrays
				.map(VarIntByteArraysTool::encodeOne)
				.listTo(ByteTool::concat);
	}

	public static byte[] decodeOne(byte[] encodedBytes){
		return decodeOne(encodedBytes, 0);
	}

	public static byte[] decodeOne(byte[] encodedBytes, int offset){
		int cursor = offset;
		int length = VarIntTool.decodeInt(encodedBytes, cursor);
		cursor += VarIntTool.length(length);
		return Arrays.copyOfRange(encodedBytes, cursor, cursor + length);
	}

	public static Scanner<byte[]> decodeMulti(byte[] block){
		return new VarIntByteArraysScanner(block);
	}

	/*---------- private -------------*/

	private static class VarIntByteArraysScanner
	extends BaseScanner<byte[]>{

		private final byte[] block;
		private int cursor;

		public VarIntByteArraysScanner(byte[] block){
			this.block = block;
			cursor = 0;
		}

		@Override
		public boolean advance(){
			if(cursor == block.length){
				return false;
			}
			int length = VarIntTool.decodeInt(block, cursor);
			cursor += VarIntTool.length(length);
			current = Arrays.copyOfRange(block, cursor, cursor + length);
			cursor += length;
			return true;
		}

	}

}
