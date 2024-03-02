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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.blockfile.block.BlockfileBlockType;
import io.datarouter.bytes.blockfile.block.parsed.BlockfileDecodedBlock;
import io.datarouter.bytes.blockfile.block.parsed.BlockfileDecodedBlockBatch;
import io.datarouter.bytes.blockfile.block.parsed.ParsedValueBlock;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileBaseTokens;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileIndexTokens;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileValueTokens;
import io.datarouter.bytes.blockfile.index.BlockfileIndexEntryRange;
import io.datarouter.bytes.blockfile.io.read.BlockfileReader;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileRowKeyRangeReader.BlockfileKeyRange;
import io.datarouter.bytes.io.InputStreamTool;
import io.datarouter.scanner.Scanner;

public class BlockfileSequentialReader<T>{
	private static final Logger logger = LoggerFactory.getLogger(BlockfileSequentialReader.class);

	private final BlockfileReader<T> reader;

	public BlockfileSequentialReader(BlockfileReader<T> reader){
		this.reader = reader;
	}

	public Scanner<byte[]> scanDecompressedValues(){
		return scanParsedValueBlocks()
				.batch(reader.config().decodeBatchSize())
				.parallelOrdered(reader.config().decodeThreads())
				.map(reader.valueBlockDecoder()::decompressValueBlocks)
				.concat(Scanner::of);
	}

	public Scanner<T> scan(){
		return scanDecodedValues()
				.concat(Scanner::of);
	}

	public Scanner<List<T>> scanDecodedValues(){
		return scanDecodedBlocks()
				.map(BlockfileDecodedBlock::items);
	}

	public Scanner<BlockfileDecodedBlock<T>> scanDecodedBlocks(){
		return scanDecodedBlockBatches()
				.concatIter(BlockfileDecodedBlockBatch::blocks);
	}

	public Scanner<BlockfileDecodedBlockBatch<T>> scanDecodedBlockBatches(){
		return scanParsedValueBlocks()
				.batch(reader.config().decodeBatchSize())
				.parallelOrdered(reader.config().decodeThreads())
				.map(reader.valueBlockDecoder()::decompressAndDecodeValueBlocks);
	}

	/*--------- private -----------*/

	private record ParsedBlock(
			BlockfileBlockType blockType,
			ParsedValueBlock parsedValueBlock){
	}

	private Scanner<ParsedValueBlock> scanParsedValueBlocks(){
		InputStream inputStream = reader.makeInputStream();
		// Load and cache the header from the InputStream before accessing it.
		// Otherwise it will trigger separate header loading.
		reader.metadata().readAndCacheHeader(inputStream);
		return scanParsedBlocks(inputStream)
				.advanceUntil(parsedBlock -> parsedBlock.blockType() == BlockfileBlockType.FOOTER)
				.include(parsedBlock -> parsedBlock.blockType() == BlockfileBlockType.VALUE)
				.map(ParsedBlock::parsedValueBlock);
	}

	//TODO share code with above method
	public Scanner<ParsedValueBlock> scanParsedValueBlocks(
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
		return scanParsedBlocks(inputStream)
				.each(block -> logger.warn("block type={}", block.blockType()))
				.limit(indexEntryRange.numBlocks())
				.include(parsedBlock -> parsedBlock.blockType() == BlockfileBlockType.VALUE)
				.map(ParsedBlock::parsedValueBlock);
	}

	private Scanner<ParsedBlock> scanParsedBlocks(InputStream inputStream){
		int checksumLength = reader.metadata().header().checksummer().numBytes();
		int valueBlockMetadataLength = BlockfileValueTokens.lengthWithoutValue(
				reader.metadata().header().checksummer().numBytes());
		int indexBlockMetadataLength = BlockfileIndexTokens.lengthWithoutValue();
		return Scanner.generate(() -> {
			byte[] compressedValueLengthBytes = InputStreamTool.readNBytes(
					inputStream,
					BlockfileBaseTokens.NUM_LENGTH_BYTES);
			int blockLength = BlockfileBaseTokens.decodeLength(compressedValueLengthBytes);
			byte blockTypeCode = InputStreamTool.readRequiredByte(inputStream);
			BlockfileBlockType blockType = BlockfileBlockType.decode(blockTypeCode);
			if(blockType == BlockfileBlockType.INDEX){
				InputStreamTool.readNBytes(inputStream, blockLength - indexBlockMetadataLength);
				return new ParsedBlock(BlockfileBlockType.INDEX, null);
			}else if(blockType == BlockfileBlockType.FOOTER){
				return new ParsedBlock(BlockfileBlockType.FOOTER, null);
			}else{// must be VALUE block
				byte[] checksumValue = InputStreamTool.readNBytes(inputStream, checksumLength);
				int compressedValueLength = blockLength - valueBlockMetadataLength;
				byte[] compressedValue = InputStreamTool.readNBytes(inputStream, compressedValueLength);
				var parsedValueBlock = new ParsedValueBlock(
						compressedValueLengthBytes,
						checksumValue,
						compressedValue);
				return new ParsedBlock(BlockfileBlockType.VALUE, parsedValueBlock);
			}
		});
	}

}
