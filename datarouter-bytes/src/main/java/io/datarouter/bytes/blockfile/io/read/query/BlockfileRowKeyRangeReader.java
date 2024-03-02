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
package io.datarouter.bytes.blockfile.io.read.query;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.blockfile.block.decoded.BlockfileIndexBlock;
import io.datarouter.bytes.blockfile.block.parsed.BlockfileDecodedBlock;
import io.datarouter.bytes.blockfile.block.parsed.BlockfileDecodedBlockBatch;
import io.datarouter.bytes.blockfile.encoding.indexblock.BlockfileIndexBlockCodec;
import io.datarouter.bytes.blockfile.index.BlockfileIndexEntry;
import io.datarouter.bytes.blockfile.index.BlockfileIndexEntryRange;
import io.datarouter.bytes.blockfile.io.read.BlockfileReader;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.scanner.Scanner;

public class BlockfileRowKeyRangeReader<T>{
	private static final Logger logger = LoggerFactory.getLogger(BlockfileRowKeyRangeReader.class);

	private final BlockfileReader<T> reader;
	private final BlockfileSequentialReader<T> sequentialReader;
	private final BlockfileIndexBlockCodec indexBlockCodec;

	public BlockfileRowKeyRangeReader(BlockfileReader<T> reader){
		this.reader = reader;
		sequentialReader = new BlockfileSequentialReader<>(reader);
		indexBlockCodec = reader.metadata().header().indexBlockFormat().supplier().get();
	}

	public record BlockfileKeyRange(
			Optional<byte[]> from,
			boolean fromInclusive,
			Optional<byte[]> to,
			boolean toInclusive){

		private static final BlockfileKeyRange EVERYTHING = new BlockfileKeyRange(
				Optional.empty(),
				true,
				Optional.empty(),
				true);

		public static BlockfileKeyRange everything(){
			return EVERYTHING;
		}

		public boolean isEverything(){
			return from.isEmpty() && to.isEmpty();
		}

		public boolean contains(BlockfileRow row){
			boolean firstMatch = from.isEmpty();
			if(from.isPresent()){
				int diff = row.compareToKey(from.orElseThrow());
				firstMatch = fromInclusive ? diff >= 0 : diff > 0;
			}
			boolean lastMatch = to.isEmpty();
			if(to.isPresent()){
				int diff = row.compareToKey(to.orElseThrow());
				lastMatch = toInclusive ? diff <= 0 : diff < 0;
			}
			return firstMatch && lastMatch;
		}
	}

	public BlockfileIndexEntryRange indexEntryRange(BlockfileKeyRange range){
		BlockfileIndexBlock rootIndexBlock = reader.metadata().rootIndex();
		int level = rootIndexBlock.level();
		BlockfileIndexBlock firstIndexBlock = rootIndexBlock;
		BlockfileIndexBlock lastIndexBlock = rootIndexBlock;
		BlockfileIndexEntry firstIndexEntry = null;
		BlockfileIndexEntry lastIndexEntry = null;
		while(true){
			int firstIndexEntryIndex = range.from().isEmpty()
					? 0
					: indexBlockCodec.rangeStartIndex(firstIndexBlock, range.from().orElseThrow());
			firstIndexEntry = indexBlockCodec.decodeChild(lastIndexBlock, firstIndexEntryIndex);
			int lastIndexEntryIndex = range.to().isEmpty()
					? lastIndexBlock.numChildren() - 1
					: indexBlockCodec.rangeEndIndex(lastIndexBlock, range.to().orElseThrow());
			lastIndexEntry = indexBlockCodec.decodeChild(lastIndexBlock, lastIndexEntryIndex);
			if(level == 0){
				break;
			}
			firstIndexBlock = reader.loadIndexBlock(firstIndexEntry);
			// Avoid fetching the same child block twice.
			boolean sameChildBlock = firstIndexEntry.childGlobalBlockId() == lastIndexEntry.childGlobalBlockId();
			lastIndexBlock = sameChildBlock
					? firstIndexBlock
					: reader.loadIndexBlock(lastIndexEntry);
			--level;
		}
		return new BlockfileIndexEntryRange(firstIndexEntry, lastIndexEntry);
	}

	public Scanner<T> scanRange(BlockfileKeyRange keyRange){
		BlockfileIndexEntryRange indexEntryRange = indexEntryRange(keyRange);
		return sequentialReader.scanParsedValueBlocks(indexEntryRange, keyRange)
				.batch(reader.config().decodeBatchSize())
				.parallelOrdered(reader.config().decodeThreads())
				.map(block -> reader.valueBlockDecoder().decompressAndDecodeValueBlocks(block, keyRange))
				.concatIter(BlockfileDecodedBlockBatch::blocks)
				.concatIter(BlockfileDecodedBlock::items);
	}

}
