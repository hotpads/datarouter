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

import io.datarouter.bytes.blockfile.block.BlockfileBlockType;
import io.datarouter.bytes.blockfile.block.parsed.BlockfileDecodedBlock;
import io.datarouter.bytes.blockfile.block.parsed.BlockfileDecodedBlockBatch;
import io.datarouter.bytes.blockfile.block.parsed.ParsedValueBlock;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileBaseTokens;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileIndexTokens;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileValueTokens;
import io.datarouter.bytes.blockfile.io.read.BlockfileReader;
import io.datarouter.bytes.io.InputStreamTool;
import io.datarouter.scanner.Scanner;

public class BlockfileSequentialSingleUseReader<T>{

	private final BlockfileReader<T> reader;
	private final InputStream inputStream;

	public BlockfileSequentialSingleUseReader(
			BlockfileReader<T> reader,
			InputStream inputStream){
		this.reader = reader;
		this.inputStream = inputStream;
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

	private Scanner<ParsedValueBlock> scanParsedValueBlocks(){
		// Load and cache the header from the InputStream before accessing it.
		// Otherwise it will trigger separate header loading.
		reader.metadata().readAndCacheHeader(inputStream);
		return scanParsedBlocks()
				.advanceUntil(parsedBlock -> parsedBlock.blockType() == BlockfileBlockType.FOOTER)
				.include(parsedBlock -> parsedBlock.blockType() == BlockfileBlockType.VALUE)
				.map(ParsedBlock::parsedValueBlock);
	}

	public record ParsedBlock(
			BlockfileBlockType blockType,
			ParsedValueBlock parsedValueBlock){
	}

	public Scanner<ParsedBlock> scanParsedBlocks(){
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
