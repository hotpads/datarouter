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
package io.datarouter.bytes.blockfile.io.read;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.block.BlockfileBlockType;
import io.datarouter.bytes.blockfile.block.decoded.BlockfileHeaderBlock.BlockfileHeaderCodec;
import io.datarouter.bytes.blockfile.block.decoded.BlockfileIndexBlock;
import io.datarouter.bytes.blockfile.block.parsed.BlockfileDecodedBlock;
import io.datarouter.bytes.blockfile.block.parsed.BlockfileDecodedBlockBatch;
import io.datarouter.bytes.blockfile.block.parsed.ParsedValueBlock;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileBaseTokens;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockCodec.BlockfileEncodedValueBlock;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockDecoder;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockDecoder.BlockfileValueBlockDecoderConfig;
import io.datarouter.bytes.blockfile.index.BlockfileIndexEntry;
import io.datarouter.bytes.blockfile.io.read.metadata.BlockfileMetadataReader;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileBlockIdReader;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileIndexReader;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileRowIdReader;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileRowKeyRangeReader;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileRowKeyReader;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileSequentialReader;
import io.datarouter.bytes.blockfile.io.storage.BlockfileLocation;
import io.datarouter.bytes.blockfile.io.storage.BlockfileStorage;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.scanner.Threads;

public class BlockfileReader<T>{

	public record BlockfileReaderConfig<T>(
			BlockfileStorage storage,
			Function<BlockfileRow,T> rowDecoder,
			BlockfileHeaderCodec headerCodec,
			Threads readThreads,
			ByteLength readChunkSize,
			int decodeBatchSize,
			Threads decodeThreads,
			boolean validateChecksums,
			Optional<Long> knownFileLength){
	}

	private final BlockfileMetadataReader<T> metadata;
	private final BlockfileReaderConfig<T> config;
	private final BlockfileValueBlockDecoder<T> valueBlockDecoder;

	/*---------- construct ---------*/

	public BlockfileReader(
			BlockfileMetadataReader<T> metadata,
			BlockfileReaderConfig<T> config){
		this.metadata = metadata;
		this.config = config;
		var decodingManagerConfig = new BlockfileValueBlockDecoderConfig<>(
				config.rowDecoder(),
				metadata,
				config.validateChecksums());
		valueBlockDecoder = new BlockfileValueBlockDecoder<>(decodingManagerConfig);

	}

	public BlockfileMetadataReader<T> metadata(){
		return metadata;
	}

	public BlockfileReaderConfig<T> config(){
		return config;
	}

	public BlockfileValueBlockDecoder<T> valueBlockDecoder(){
		return valueBlockDecoder;
	}

	/*--------- query types --------*/

	public BlockfileSequentialReader<T> sequential(){
		return new BlockfileSequentialReader<>(this);
	}

	public BlockfileIndexReader<T> index(){
		return new BlockfileIndexReader<>(this);
	}

	public BlockfileBlockIdReader<T> blockId(){
		return new BlockfileBlockIdReader<>(this);
	}

	public BlockfileRowIdReader<T> rowId(){
		return new BlockfileRowIdReader<>(this);
	}

	public BlockfileRowKeyReader<T> rowKey(){
		return new BlockfileRowKeyReader<>(this);
	}

	public BlockfileRowKeyRangeReader<T> rowKeyRange(){
		return new BlockfileRowKeyRangeReader<>(this);
	}

	/*---------- load block -----------*/

	public BlockfileIndexBlock loadIndexBlock(BlockfileIndexEntry indexEntry){
		byte[] bytes = config().storage().readPartial(
				metadata().name(),
				indexEntry.byteRange().toLocation());
		//TODO validate length and block type?
		return metadata().header().indexBlockFormat().supplier().get().decode(bytes);
	}

	public BlockfileEncodedValueBlock loadEncodedValueBlock(BlockfileIndexEntry index){
		ParsedValueBlock parsedValueBlock = loadParsedValueBlock(index.byteRange().toLocation());
		return valueBlockDecoder.decompressValueBlock(parsedValueBlock);
	}

	public BlockfileDecodedBlock<T> loadValueBlock(BlockfileLocation location){
		var parsed = loadParsedValueBlock(location);
		BlockfileDecodedBlockBatch<T> blockBatch = valueBlockDecoder.decompressAndDecodeValueBlocks(List.of(parsed));
		return blockBatch.blocks().getFirst();
	}

	private ParsedValueBlock loadParsedValueBlock(BlockfileLocation location){
		byte[] bytes = config().storage().readPartial(metadata().name(), location);
		//TODO validate length and block type?
		int cursor = 0;
		byte[] lengthBytes = Arrays.copyOfRange(bytes, cursor, BlockfileBaseTokens.NUM_LENGTH_BYTES);
		cursor += BlockfileBaseTokens.NUM_LENGTH_BYTES;
		cursor += BlockfileBlockType.NUM_BYTES;
		byte[] checksumBytes = Arrays.copyOfRange(bytes, cursor, cursor + metadata().header().checksummer().numBytes());
		cursor += metadata().header().checksummer().numBytes();
		byte[] compressedValueBytes = Arrays.copyOfRange(bytes, cursor, bytes.length);
		return new ParsedValueBlock(lengthBytes, checksumBytes, compressedValueBytes);
	}

	/*------------ InputStream ------------*/

	public InputStream makeInputStream(){
		if(config().knownFileLength().isPresent()
				&& config().knownFileLength().orElseThrow() <= config().readChunkSize().toBytes()){
			// avoids an internal call to fetch the length before starting the parallel chunk scanner
			return new ByteArrayInputStream(config().storage().read(metadata().name()));
		}
		return config().storage().readInputStream(
				metadata().name(),
				config().readThreads(),
				config().readChunkSize());
	}

	public InputStream makeInputStream(long from, long to){
		long length = to - from;
		if(length <= config().readChunkSize().toBytes()){
			return new ByteArrayInputStream(config().storage().readPartial(
					metadata().name(),
					new BlockfileLocation(from, (int)length)));
		}
		return config().storage().readInputStream(
				metadata().name(),
				from,
				to,
				config().readThreads(),
				config().readChunkSize());
	}

}
