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

import java.io.InputStream;
import java.util.List;

import io.datarouter.bytes.blockfile.block.BlockfileBlockType;
import io.datarouter.bytes.blockfile.io.storage.BlockfileLocation;
import io.datarouter.bytes.io.InputStreamTool;

public class BlockfileHeaderTokens
extends BlockfileBaseTokens{

	private final byte[] value;

	public BlockfileHeaderTokens(int length, byte[] value){
		super(length, BlockfileBlockType.HEADER);
		this.value = value;
	}

	@Override
	public int suffixLength(){
		return value.length;
	}

	@Override
	public List<byte[]> toList(){
		return List.of(prefixBytes(), value);
	}

	public static BlockfileLocation lengthLocation(){
		return new BlockfileLocation(0, BlockfileBaseTokens.NUM_LENGTH_BYTES);
	}

	public static long valueOffset(){
		return BlockfileBaseTokens.NUM_PREFIX_BYTES;
	}

	public static int valueLength(int blockLength){
		return blockLength - BlockfileBaseTokens.NUM_PREFIX_BYTES;
	}

	public static BlockfileLocation valueLocation(int blockLength){
		return new BlockfileLocation(valueOffset(), valueLength(blockLength));
	}

	public static int readBlockLength(InputStream inputStream){
		byte[] blockLengthBytes = InputStreamTool.readNBytes(inputStream, NUM_LENGTH_BYTES);
		return decodeLength(blockLengthBytes);
	}

	public static BlockfileBlockType readBlockType(InputStream inputStream){
		byte blockTypeByte = InputStreamTool.readRequiredByte(inputStream);
		return BlockfileBlockType.decode(blockTypeByte);
	}

	public static byte[] readValueBytes(InputStream inputStream, int blockLength){
		int valueLength = valueLength(blockLength);
		return InputStreamTool.readNBytes(inputStream, valueLength);
	}

}
