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

import io.datarouter.bytes.blockfile.block.BlockfileBlockType;
import io.datarouter.bytes.blockfile.io.storage.BlockfileLocation;

public class BlockfileFooterTokens
extends BlockfileBaseTokens{

	private final byte[] value;

	public BlockfileFooterTokens(int length, byte[] value){
		super(length, BlockfileBlockType.FOOTER);
		this.value = value;
	}

	@Override
	public int suffixLength(){
		return value.length + NUM_LENGTH_BYTES;
	}

	@Override
	public List<byte[]> toList(){
		return List.of(
				prefixBytes(),
				value,
				encodeLength());
	}

	public static BlockfileLocation lengthLocation(long fileLength){
		return new BlockfileLocation(
				fileLength - NUM_LENGTH_BYTES,
				NUM_LENGTH_BYTES);
	}

	public static BlockfileLocation blockLocation(long fileLength, int footerBlockLength){
		return new BlockfileLocation(
				fileLength - footerBlockLength,
				footerBlockLength);
	}

	public static BlockfileLocation valueLocation(BlockfileLocation blockLocation){
		long from = blockLocation.from()
				+ BlockfileBaseTokens.NUM_PREFIX_BYTES;
		int length = blockLocation.length()
				- BlockfileBaseTokens.NUM_PREFIX_BYTES
				- BlockfileBaseTokens.NUM_LENGTH_BYTES;
		return new BlockfileLocation(from, length);
	}

}
