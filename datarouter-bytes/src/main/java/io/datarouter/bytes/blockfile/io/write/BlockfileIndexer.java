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
package io.datarouter.bytes.blockfile.io.write;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileIndexTokens;
import io.datarouter.bytes.blockfile.encoding.indexblock.BlockfileIndexBlockCodec;
import io.datarouter.bytes.blockfile.index.BlockfileByteRange;
import io.datarouter.bytes.blockfile.index.BlockfileIndexBlockInput;
import io.datarouter.bytes.blockfile.index.BlockfileIndexEntry;
import io.datarouter.bytes.blockfile.index.BlockfileRowIdRange;
import io.datarouter.bytes.blockfile.index.BlockfileRowRange;
import io.datarouter.bytes.blockfile.index.BlockfileValueBlockIdRange;
import io.datarouter.scanner.Scanner;

public class BlockfileIndexer{

	private final BlockfileWriterState writerState;
	private final int maxRecordsPerLevel;
	private final Optional<ByteLength> optTargetBlockSize;
	private final BlockfileIndexBlockCodec indexBlockCodec;
	private final List<BlockfileIndexBlockBuilder> builderByLevel = new ArrayList<>();

	public BlockfileIndexer(
			BlockfileWriterState writerState,
			int maxRecordsPerLevel,
			Optional<ByteLength> optTargetBlockSize,
			BlockfileIndexBlockCodec indexBlockCodec){
		this.writerState = writerState;
		this.maxRecordsPerLevel = maxRecordsPerLevel;
		this.optTargetBlockSize = optTargetBlockSize;
		this.indexBlockCodec = indexBlockCodec;
		getOrInitBuilderForLevel(0);
	}

	public void onValueBlockWrite(
			long globalBlockId,
			long valueBlockId,
			BlockfileRowIdRange rowIdRange,
			BlockfileRowRange rowRange,
			BlockfileByteRange byteRange){
		int indexLevel = 0;
		var child = new BlockfileIndexEntry(
				indexLevel,
				globalBlockId,
				valueBlockId,
				BlockfileValueBlockIdRange.singleBlock(valueBlockId),
				rowIdRange,
				rowRange,
				byteRange);
		int estEncodedBytes = indexBlockCodec.estEncodedBytes(child);
		getOrInitBuilderForLevel(indexLevel).addChild(child, estEncodedBytes);
	}

	public Scanner<BlockfileIndexTokens> drainCompletedBlocks(){
		// Optimization for the common case.
		if(!builderByLevel.getFirst().isFull()){
			return Scanner.empty();
		}
		return drain(false);
	}

	public Scanner<BlockfileIndexTokens> drainAllBlocks(){
		return drain(true);
	}

	private Scanner<BlockfileIndexTokens> drain(boolean drainAll){
		return Scanner.iterate(0, level -> level + 1)// access the levels list lazily
				.advanceWhile(level -> level < builderByLevel.size())
				.map(builderByLevel::get)
				.advanceWhile(builder -> drainAll || builder.isFull())
				.map(builder -> {
					BlockfileIndexBlockInput indexBlockInput = builder.build(
							writerState.nextGlobalBlockId(),
							writerState.takeIndexBlockId());
					BlockfileIndexTokens tokens = indexBlockCodec.encode(indexBlockInput);
					BlockfileByteRange byteRange = writerState.appendIndexBlock(tokens);
					if(shouldPromote(indexBlockInput, drainAll)){
						promoteToNextLevel(indexBlockInput, byteRange);
					}
					builderByLevel.set(
							builder.level(),
							new BlockfileIndexBlockBuilder(maxRecordsPerLevel, optTargetBlockSize, builder.level()));
					return tokens;
				});
	}

	private boolean shouldPromote(
			BlockfileIndexBlockInput indexBlockInput,
			boolean drainAll){
		if(!drainAll){
			return true;
		}
		if(indexBlockInput.children().isEmpty()){
			return false;
		}
		return indexBlockInput.level() < builderByLevel.size() - 1;
	}

	private void promoteToNextLevel(
			BlockfileIndexBlockInput indexBlockInput,
			BlockfileByteRange byteRange){
		int parentLevel = indexBlockInput.level() + 1;
		var child = new BlockfileIndexEntry(
				parentLevel,
				indexBlockInput.globalBlockId(),
				indexBlockInput.indexBlockId(),
				indexBlockInput.toParentValueBlockIdRange(),
				indexBlockInput.toParentRowIdRange(),
				indexBlockInput.toParentRowRange(),
				byteRange);
		int estEncodedBytes = indexBlockCodec.estEncodedBytes(child);
		getOrInitBuilderForLevel(parentLevel).addChild(child, estEncodedBytes);
	}

	// Assumes caller doesn't skip levels.
	private BlockfileIndexBlockBuilder getOrInitBuilderForLevel(int level){
		if(level > builderByLevel.size() - 1){
			var builder = new BlockfileIndexBlockBuilder(maxRecordsPerLevel, optTargetBlockSize, level);
			builderByLevel.add(builder);
		}
		return builderByLevel.get(level);
	}

}
