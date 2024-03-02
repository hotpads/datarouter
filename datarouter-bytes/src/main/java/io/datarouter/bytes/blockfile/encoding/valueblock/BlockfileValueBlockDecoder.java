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
package io.datarouter.bytes.blockfile.encoding.valueblock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import io.datarouter.bytes.blockfile.block.parsed.BlockfileDecodedBlock;
import io.datarouter.bytes.blockfile.block.parsed.BlockfileDecodedBlockBatch;
import io.datarouter.bytes.blockfile.block.parsed.ParsedValueBlock;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileValueTokens;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockCodec.BlockfileEncodedValueBlock;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockCodec.BlockfileValueBlockRows;
import io.datarouter.bytes.blockfile.io.read.metadata.BlockfileMetadataReader;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileRowKeyRangeReader.BlockfileKeyRange;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.bytes.codec.bytestringcodec.HexByteStringCodec;

public class BlockfileValueBlockDecoder<T>{

	public record BlockfileValueBlockDecoderConfig<T>(
			Function<BlockfileRow,T> rowDecoder,
			BlockfileMetadataReader<T> metadata,
			boolean validateChecksums){
	}

	private final BlockfileValueBlockDecoderConfig<T> config;
	private final BlockfileMetadataReader<T> metadata;

	// The metadata is loaded lazily, so don't dereference things until needed.
	public BlockfileValueBlockDecoder(BlockfileValueBlockDecoderConfig<T> config){
		this.config = config;
		metadata = config.metadata();
	}

	/*---------- decompress ----------*/

	public List<byte[]> decompressValueBlocks(List<ParsedValueBlock> parsedValueBlocks){
		Function<byte[],byte[]> decompressor = metadata.header().compressor().newDecoder();
		List<byte[]> decompressedByteArrays = new ArrayList<>(parsedValueBlocks.size());
		for(ParsedValueBlock parsedValueBlock : parsedValueBlocks){
			if(config.validateChecksums()){
				validateChecksum(parsedValueBlock);
			}
			byte[] decompressedBytes = decompressor.apply(parsedValueBlock.compressedValue());
			decompressedByteArrays.add(decompressedBytes);
		}
		return decompressedByteArrays;
	}

	public BlockfileEncodedValueBlock decompressValueBlock(ParsedValueBlock parsedValueBlock){
		if(config.validateChecksums()){
			validateChecksum(parsedValueBlock);
		}
		Function<byte[],byte[]> decompressor = metadata.header().compressor().newDecoder();
		byte[] decompressedBytes = decompressor.apply(parsedValueBlock.compressedValue());
		return new BlockfileEncodedValueBlock(decompressedBytes);
	}

	/*---------- decode ----------*/

	public BlockfileDecodedBlockBatch<T> decompressAndDecodeValueBlocks(List<ParsedValueBlock> parsedValueBlocks){
		return decompressAndDecodeValueBlocks(parsedValueBlocks, BlockfileKeyRange.everything());
	}

	//TODO share code with above method
	public BlockfileDecodedBlockBatch<T> decompressAndDecodeValueBlocks(
			List<ParsedValueBlock> parsedValueBlocks,
			BlockfileKeyRange keyRange){
		Function<byte[],byte[]> decompressor = metadata.header().compressor().newDecoder();
		BlockfileValueBlockCodec valueBlockCodec = metadata.header().valueBlockFormat().newCodec();
		int numMetadataBytes = BlockfileValueTokens.lengthWithoutValue(metadata.header().checksummer().numBytes());
		List<BlockfileDecodedBlock<T>> decodedBlocks = new ArrayList<>(parsedValueBlocks.size());
		long totalCompressedSize = 0;
		long totalDecompressedSize = 0;
		for(ParsedValueBlock parsedValueBlock : parsedValueBlocks){
			if(config.validateChecksums()){
				validateChecksum(parsedValueBlock);
			}
			byte[] decompressedBytes = decompressor.apply(parsedValueBlock.compressedValue());
			var encodedValue = new BlockfileEncodedValueBlock(decompressedBytes);
			BlockfileValueBlockRows rows = valueBlockCodec.decode(encodedValue, keyRange);
			List<T> items = decodeRows(rows.rows());
			var decodedBlock = new BlockfileDecodedBlock<>(
					numMetadataBytes + parsedValueBlock.compressedValue().length,
					numMetadataBytes + decompressedBytes.length,
					rows.firstRowIdInBlock(),
					items);
			decodedBlocks.add(decodedBlock);
			totalCompressedSize += parsedValueBlock.compressedValue().length;
			totalDecompressedSize += decompressedBytes.length;
		}
		return new BlockfileDecodedBlockBatch<>(totalCompressedSize, totalDecompressedSize, decodedBlocks);
	}

	@SuppressWarnings("unchecked")
	private List<T> decodeRows(List<BlockfileRow> rows){
		Function<BlockfileRow,T> rowDecoder = config.rowDecoder();
		if(Function.identity().equals(rowDecoder)){
			return (List<T>)rows;
		}
		List<T> items = new ArrayList<>(rows.size());
		for(BlockfileRow row : rows){
			items.add(rowDecoder.apply(row));
		}
		return items;
	}

	/*------------- checksum ------------*/

	private void validateChecksum(ParsedValueBlock parsedValueBlock){
		Function<byte[],byte[]> encoder = metadata.header().checksummer().newEncoder();
		byte[] expected = parsedValueBlock.checksum();
		byte[] actual = encoder.apply(parsedValueBlock.compressedValue());
		if(!Arrays.equals(expected, actual)){
			String message = String.format(
					"invalid checksum: expected=%s, actual=%s",
					HexByteStringCodec.INSTANCE.encode(expected),
					HexByteStringCodec.INSTANCE.encode(actual));
			throw new RuntimeException(message);
		}
	}


}
