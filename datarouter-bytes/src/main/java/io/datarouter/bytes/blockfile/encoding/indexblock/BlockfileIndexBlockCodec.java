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
package io.datarouter.bytes.blockfile.encoding.indexblock;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.blockfile.block.decoded.BlockfileIndexBlock;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileIndexTokens;
import io.datarouter.bytes.blockfile.index.BlockfileIndexBlockInput;
import io.datarouter.bytes.blockfile.index.BlockfileIndexEntry;
import io.datarouter.bytes.blockfile.index.BlockfilePointSearchTool;
import io.datarouter.bytes.blockfile.index.BlockfileRangeSearchTool;
import io.datarouter.scanner.Scanner;

public interface BlockfileIndexBlockCodec{
	static final Logger logger = LoggerFactory.getLogger(BlockfileIndexBlockCodec.class);

	BlockfileIndexTokens encode(BlockfileIndexBlockInput input);

	BlockfileIndexBlock decode(byte[] bytes);

	int estEncodedBytes(BlockfileIndexEntry indexEntry);

	BlockfileIndexEntry decodeChild(BlockfileIndexBlock block, int childIndex);

	/*--------- default -----------*/

	default BlockfileIndexEntry firstChild(BlockfileIndexBlock block){
		return decodeChild(block, 0);
	}

	default BlockfileIndexEntry lastChild(BlockfileIndexBlock block){
		return decodeChild(block, block.numChildren() - 1);
	}

	default Scanner<BlockfileIndexEntry> scanChildren(BlockfileIndexBlock block){
		return Scanner.iterate(0, i -> i + 1)
				.limit(block.numChildren())
				.map(i -> decodeChild(block, i));
	}

	default Scanner<BlockfileIndexEntry> scanChildrenDesc(BlockfileIndexBlock block){
		return Scanner.iterate(block.numChildren() - 1, i -> i - 1)
				.limit(block.numChildren())
				.map(i -> decodeChild(block, i));
	}

	default BlockfileIndexEntry childContainingValueBlockId(BlockfileIndexBlock block, long valueBlockId){
		return BlockfilePointSearchTool.findAny(
				block.numChildren(),
				childIndex -> decodeChild(block, childIndex),
				indexEntry -> indexEntry.valueBlockIdRange().compareTo(valueBlockId))
				.orElseThrow();
	}

	default BlockfileIndexEntry childContainingRowId(BlockfileIndexBlock block, long rowId){
		return BlockfilePointSearchTool.findAny(
				block.numChildren(),
				childIndex -> decodeChild(block, childIndex),
				indexEntry -> indexEntry.rowIdRange().compareTo(rowId))
				.orElseThrow();
	}

	default Optional<BlockfileIndexEntry> firstChildContainingKey(
			BlockfileIndexBlock block,
			byte[] key){
		return BlockfilePointSearchTool.findFirst(
				block.numChildren(),
				childIndex -> decodeChild(block, childIndex),
				indexEntry -> indexEntry.rowRange().compareToKey(key));
	}

	default Optional<BlockfileIndexEntry> lastChildContainingKey(
			BlockfileIndexBlock block,
			byte[] key){
		return BlockfilePointSearchTool.findLast(
				block.numChildren(),
				childIndex -> decodeChild(block, childIndex),
				indexEntry -> indexEntry.rowRange().compareToKey(key));
	}

	default int rangeStartIndex(
			BlockfileIndexBlock block,
			byte[] key){
		int index = BlockfileRangeSearchTool.startIndex(
				block.numChildren(),
				childIndex -> decodeChild(block, childIndex),
				indexEntry -> indexEntry.rowRange().compareToKey(key));
		return Math.max(0, index);
	}

	default int rangeEndIndex(
			BlockfileIndexBlock block,
			byte[] key){
		return BlockfileRangeSearchTool.endIndex(
				block.numChildren(),
				childIndex -> decodeChild(block, childIndex),
				indexEntry -> indexEntry.rowRange().compareToKey(key));
	}
}
