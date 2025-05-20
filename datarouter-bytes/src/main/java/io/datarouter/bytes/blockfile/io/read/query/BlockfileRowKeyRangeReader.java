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

import java.io.InputStream;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.blockfile.block.BlockfileBlockType;
import io.datarouter.bytes.blockfile.block.decoded.BlockfileIndexBlock;
import io.datarouter.bytes.blockfile.block.parsed.BlockfileDecodedBlock;
import io.datarouter.bytes.blockfile.block.parsed.BlockfileDecodedBlockBatch;
import io.datarouter.bytes.blockfile.block.parsed.ParsedValueBlock;
import io.datarouter.bytes.blockfile.encoding.indexblock.BlockfileIndexBlockCodec;
import io.datarouter.bytes.blockfile.index.BlockfileIndexEntry;
import io.datarouter.bytes.blockfile.index.BlockfileIndexEntryRange;
import io.datarouter.bytes.blockfile.io.read.BlockfileReader;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileSequentialSingleUseReader.ParsedBlock;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.scanner.Scanner;

public class BlockfileRowKeyRangeReader<T>{
	private static final Logger logger = LoggerFactory.getLogger(BlockfileRowKeyRangeReader.class);

	private final BlockfileReader<T> reader;
	private final BlockfileIndexBlockCodec indexBlockCodec;

	public BlockfileRowKeyRangeReader(BlockfileReader<T> reader){
		this.reader = reader;
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

		public BlockfileKeyRange toInclusiveExclusive(){
			return new BlockfileKeyRange(
					from.map(fromBytes -> fromInclusive ? fromBytes : ByteTool.unsignedIncrement(fromBytes)),
					true,
					to.map(toBytes -> toInclusive ? ByteTool.unsignedIncrement(toBytes) : toBytes),
					false);
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
			int firstIndexEntryIndex = 0;
			if(range.from().isPresent()){
				firstIndexEntryIndex = indexBlockCodec.rangeStartIndex(
						firstIndexBlock,
						range.from().orElseThrow());
			}
			firstIndexEntry = indexBlockCodec.decodeChild(lastIndexBlock, firstIndexEntryIndex);
			int lastIndexEntryIndex = lastIndexBlock.numChildren() - 1;
			if(range.to().isPresent()){
				lastIndexEntryIndex = indexBlockCodec.rangeEndIndex(
						lastIndexBlock,
						range.to().orElseThrow());
			}
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
		BlockfileKeyRange inclusiveExclusiveRange = keyRange.toInclusiveExclusive();
		BlockfileIndexEntryRange indexEntryRange = indexEntryRange(inclusiveExclusiveRange);
		return scanParsedValueBlocks(indexEntryRange, inclusiveExclusiveRange)
				.batch(reader.config().decodeBatchSize())
				.parallelOrdered(reader.config().decodeThreads())
				.map(block -> reader.valueBlockDecoder().decompressAndDecodeValueBlocks(block, inclusiveExclusiveRange))
				.concatIter(BlockfileDecodedBlockBatch::blocks)
				.concatIter(BlockfileDecodedBlock::items);
	}

	private Scanner<ParsedValueBlock> scanParsedValueBlocks(
			BlockfileIndexEntryRange indexEntryRange,
			BlockfileKeyRange keyRange){
		long bytesFrom = indexEntryRange.first().byteRange().from();
		long bytesTo = indexEntryRange.last().byteRange().to();
		logger.debug(
				"scanning globalBlockIds(from={},to={}), bytes(from={},to={})",
				indexEntryRange.first().childGlobalBlockId(),
				indexEntryRange.last().childGlobalBlockId(),
				bytesFrom,
				bytesTo);
		InputStream inputStream = reader.makeInputStream(bytesFrom, bytesTo);
		return reader.sequentialSingleUse(inputStream).scanParsedBlocks()
				.each(block -> logger.warn("block type={}", block.blockType()))
				.limit(indexEntryRange.numBlocks())
				.include(parsedBlock -> parsedBlock.blockType() == BlockfileBlockType.VALUE)
				.map(ParsedBlock::parsedValueBlock);
	}

}
