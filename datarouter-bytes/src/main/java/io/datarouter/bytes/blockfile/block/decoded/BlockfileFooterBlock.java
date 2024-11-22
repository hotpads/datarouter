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
package io.datarouter.bytes.blockfile.block.decoded;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.RecordByteArrayField;
import io.datarouter.bytes.blockfile.block.BlockfileFooterKey;
import io.datarouter.bytes.blockfile.io.storage.BlockfileLocation;
import io.datarouter.bytes.varint.VarIntTool;

public record BlockfileFooterBlock(
		RecordByteArrayField headerBytes,
		BinaryDictionary userDictionary,
		BlockfileLocation headerBlockLocation,
		BlockfileLocation rootIndexBlockLocation,
		long numValueBlocks,
		long numIndexBlocks){

	public static final Codec<BlockfileFooterBlock,byte[]> VALUE_CODEC = Codec.of(
			footer -> footer.toBinaryDictionary().encode(),
			bytes -> {
				var dictionary = BinaryDictionary.decode(bytes);
				return new BlockfileFooterBlock(
						parseHeaderBytes(dictionary),
						parseUserDictionary(dictionary),
						parseHeaderBlockLocation(dictionary),
						parseRootIndexBlockLocation(dictionary),
						parseNumValueBlocks(dictionary),
						parseNumIndexBlocks(dictionary));
			});

	/*----------- encode ------------*/

	private BinaryDictionary toBinaryDictionary(){
		return new BinaryDictionary()
				.put(
						BlockfileFooterKey.HEADER.bytes,
						headerBytes.bytes())
				.put(
						BlockfileFooterKey.USER_DICTIONARY.bytes,
						userDictionary.encode())
				.put(
						BlockfileFooterKey.HEADER_BLOCK_LOCATION.bytes,
						BlockfileLocation.CODEC.encode(headerBlockLocation))
				.put(
						BlockfileFooterKey.ROOT_INDEX_BLOCK_LOCATION.bytes,
						BlockfileLocation.CODEC.encode(rootIndexBlockLocation))
				.put(
						BlockfileFooterKey.NUM_VALUE_BLOCKS.bytes,
						VarIntTool.encode(numValueBlocks))
				.put(
						BlockfileFooterKey.NUM_INDEX_BLOCKS.bytes,
						VarIntTool.encode(numIndexBlocks));
	}

	/*----------- decode ------------*/

	private static RecordByteArrayField parseHeaderBytes(BinaryDictionary dictionary){
		byte[] bytes = dictionary.get(BlockfileFooterKey.HEADER.bytes);
		return new RecordByteArrayField(bytes);
	}

	private static BinaryDictionary parseUserDictionary(BinaryDictionary dictionary){
		byte[] bytes = dictionary.get(BlockfileFooterKey.USER_DICTIONARY.bytes);
		return BinaryDictionary.decode(bytes);
	}

	private static BlockfileLocation parseHeaderBlockLocation(BinaryDictionary dictionary){
		byte[] bytes = dictionary.get(BlockfileFooterKey.HEADER_BLOCK_LOCATION.bytes);
		return BlockfileLocation.CODEC.decode(bytes);
	}

	private static BlockfileLocation parseRootIndexBlockLocation(BinaryDictionary dictionary){
		byte[] bytes = dictionary.get(BlockfileFooterKey.ROOT_INDEX_BLOCK_LOCATION.bytes);
		return BlockfileLocation.CODEC.decode(bytes);
	}

	private static long parseNumValueBlocks(BinaryDictionary dictionary){
		byte[] bytes = dictionary.get(BlockfileFooterKey.NUM_VALUE_BLOCKS.bytes);
		return VarIntTool.decodeLong(bytes);
	}

	private static long parseNumIndexBlocks(BinaryDictionary dictionary){
		byte[] bytes = dictionary.get(BlockfileFooterKey.NUM_INDEX_BLOCKS.bytes);
		return VarIntTool.decodeLong(bytes);
	}

}
