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
package io.datarouter.bytes.blockfile.block.tokens;

import java.util.List;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.blockfile.block.BlockfileBlockType;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;
import io.datarouter.scanner.Scanner;

public abstract class BlockfileBaseTokens{

	private static final RawIntCodec LENGTH_CODEC = RawIntCodec.INSTANCE;

	public static final int NUM_LENGTH_BYTES = LENGTH_CODEC.length();
	public static final int NUM_PREFIX_BYTES = NUM_LENGTH_BYTES + BlockfileBlockType.NUM_BYTES;

	private final int length;
	private final BlockfileBlockType blockType;

	public BlockfileBaseTokens(int length, BlockfileBlockType blockType){
		this.length = length;
		this.blockType = blockType;
	}

	public int length(){
		return length;
	}

	public BlockfileBlockType blockType(){
		return blockType;
	}

	public byte[] encodeLength(){
		return LENGTH_CODEC.encode(length);
	}

	public static int decodeLength(byte[] lengthBytes){
		return LENGTH_CODEC.decode(lengthBytes);
	}

	public byte[] encodeBlockType(){
		return blockType.codeBytes;
	}

	public byte[] prefixBytes(){
		return ByteTool.concat(
				encodeLength(),
				encodeBlockType());
	}

	public abstract int suffixLength();

	public int totalLength(){
		return NUM_PREFIX_BYTES + suffixLength();
	}

	public abstract List<byte[]> toList();

	public Scanner<byte[]> scan(){
		return Scanner.of(toList());
	}

	public byte[] concat(){
		return ByteTool.concat(toList());
	}

}
