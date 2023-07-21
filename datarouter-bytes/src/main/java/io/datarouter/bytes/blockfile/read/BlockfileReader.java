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
package io.datarouter.bytes.blockfile.read;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.blockfile.enums.BlockfileSection;
import io.datarouter.bytes.blockfile.read.BlockfileMetadataReader.DecodedHeader;
import io.datarouter.bytes.blockfile.section.BlockfileFooter;
import io.datarouter.bytes.blockfile.section.BlockfileHeader;
import io.datarouter.bytes.blockfile.section.BlockfileHeader.BlockfileHeaderCodec;
import io.datarouter.bytes.blockfile.section.BlockfileTrailer;
import io.datarouter.bytes.blockfile.storage.BlockfileStorage;
import io.datarouter.bytes.blockfile.write.BlockfileWriter;
import io.datarouter.bytes.codec.bytestringcodec.HexByteStringCodec;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;
import io.datarouter.bytes.io.InputStreamTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;

public class BlockfileReader<T>{

	public record BlockfileReaderConfig<T>(
			BlockfileStorage storage,
			Function<byte[],T> decoder,
			BlockfileHeaderCodec headerCodec,
			Threads readThreads,
			ByteLength readChunkSize,
			int decodeBatchSize,
			Threads decodeThreads,
			boolean validateChecksums,
			Optional<Long> knownFileLength){
	}

	private final BlockfileMetadataReader<T> metadataReader;
	private final BlockfileReaderConfig<T> config;

	/*---------- construct ---------*/

	public BlockfileReader(
			BlockfileMetadataReader<T> metadataReader,
			BlockfileReaderConfig<T> config){
		this.metadataReader = metadataReader;
		this.config = config;
	}

	/*---------- metadata ---------*/

	public BlockfileMetadataReader<T> metadataReader(){
		return metadataReader;
	}

	public int headerBlockLength(){
		return metadataReader.headerBlockLength();
	}

	public BlockfileHeader header(){
		return metadataReader.header();
	}

	public BlockfileFooter footer(){
		return metadataReader.footer();
	}

	public BlockfileTrailer trailer(){
		return metadataReader.trailer();
	}

	/*------------ scan ------------*/

	private InputStream makeInputStream(){
		if(config.knownFileLength().isPresent()
				&& config.knownFileLength().orElseThrow() <= config.readChunkSize().toBytes()){
			// avoids an internal call to fetch the length before starting the parallel chunk scanner
			return new ByteArrayInputStream(config.storage().read(metadataReader.name()));
		}
		return config.storage().readInputStream(
				metadataReader.name(),
				config.readThreads(),
				config.readChunkSize());
	}

	record ParsedBlock(
			byte[] length,
			byte[] checksum,
			byte[] compressedValue){
	}

	private void readAndCacheHeader(InputStream inputStream){
		byte[] blockLengthBytes = InputStreamTool.readNBytes(
				inputStream,
				BlockfileWriter.NUM_VALUE_LENGTH_BYTES);
		int blockLength = RawIntCodec.INSTANCE.decode(blockLengthBytes);
		int valueLength = blockLength - BlockfileWriter.NUM_HEADER_METADATA_BYTES;
		InputStreamTool.readRequiredByte(inputStream);// section byte
		byte[] valueBytes = InputStreamTool.readNBytes(inputStream, valueLength);
		var decodedHeader = new DecodedHeader(
				config.headerCodec().decode(valueBytes),
				blockLength);
		metadataReader.setHeader(decodedHeader);
	}

	private Scanner<ParsedBlock> scanParsedBlocks(){
		InputStream inputStream = makeInputStream();
		// Load and cache the header from the InputStream before accessing it.
		// Otherwise it will trigger separate header loading.
		readAndCacheHeader(inputStream);
		var cursor = new AtomicLong(metadataReader.headerBlockLength());
		int checksumLength = metadataReader.header().checksumLength();
		int blockMetadataLength = metadataReader.numBlockMetadataBytes();
		return Scanner.generate(() -> {
					byte[] compressedValueLengthBytes = InputStreamTool.readNBytes(
							inputStream,
							BlockfileWriter.NUM_VALUE_LENGTH_BYTES);
					cursor.addAndGet(BlockfileWriter.NUM_VALUE_LENGTH_BYTES);
					int blockLength = RawIntCodec.INSTANCE.decode(compressedValueLengthBytes);
					byte[] checksumValue = InputStreamTool.readNBytes(inputStream, checksumLength);
					cursor.addAndGet(checksumLength);
					byte sectionCode = InputStreamTool.readRequiredByte(inputStream);
					if(sectionCode != BlockfileSection.BLOCK.codeByte){
						return Optional.<ParsedBlock>empty();
					}
					cursor.incrementAndGet();
					int compressedValueLength = blockLength - blockMetadataLength;
					byte[] compressedValue = InputStreamTool.readNBytes(inputStream, compressedValueLength);
					cursor.addAndGet(compressedValueLength);
					var parsedBlock = new ParsedBlock(compressedValueLengthBytes, checksumValue, compressedValue);
					return Optional.of(parsedBlock);
				})
				.advanceWhile(Optional::isPresent)
				.map(Optional::get);
	}

	public Scanner<byte[]> scanDecompressedValues(){
		return scanParsedBlocks()
				.batch(config.decodeBatchSize())
				.parallelOrdered(config.decodeThreads())
				.map(this::decompressBlocks)
				.concat(Scanner::of);
	}

	public Scanner<BlockfileDecodedBlockBatch<T>> scanDecodedBlockBatches(){
		return scanParsedBlocks()
				.batch(config.decodeBatchSize())
				.parallelOrdered(config.decodeThreads())
				.map(this::decompressAndDecodeBlocks);
	}

	public Scanner<BlockfileDecodedBlock<T>> scanDecodedBlocks(){
		return scanDecodedBlockBatches()
				.concatIter(BlockfileDecodedBlockBatch::blocks);
	}

	public Scanner<T> scanDecodedValues(){
		return scanDecodedBlocks()
				.map(BlockfileDecodedBlock::value);
	}

	/*
	 * Decoding:
	 *
	 * For each block: do the checksumming, decompression, and decoding together.
	 * This can help with performance as the data for each block is more likely to stay in L1/L2 caches.
	 */

	private List<byte[]> decompressBlocks(List<ParsedBlock> parsedBlocks){
		Codec<byte[],byte[]> compressorCodec = metadataReader.header().compressor().codecSupplier().get();
		List<byte[]> decompressedByteArrays = new ArrayList<>(parsedBlocks.size());
		for(ParsedBlock parsedBlock : parsedBlocks){
			if(config.validateChecksums()){
				validateChecksum(parsedBlock);
			}
			byte[] decompressedBytes = compressorCodec.decode(parsedBlock.compressedValue);
			decompressedByteArrays.add(decompressedBytes);
		}
		return decompressedByteArrays;
	}

	public record BlockfileDecodedBlock<T>(
			int compressedSize,
			int decompressedSize,
			T value){
	}

	public record BlockfileDecodedBlockBatch<T>(
			long totalCompressedSize,
			long totalDecompressedSize,
			List<BlockfileDecodedBlock<T>> blocks){

		public List<T> values(){
			return Scanner.of(blocks)
					.map(BlockfileDecodedBlock::value)
					.collect(() -> new ArrayList<>(blocks.size()));
		}
	}

	private BlockfileDecodedBlockBatch<T> decompressAndDecodeBlocks(List<ParsedBlock> parsedBlocks){
		int numMetadataBytes = metadataReader.numBlockMetadataBytes();
		Codec<byte[],byte[]> compressorCodec = metadataReader.header().compressor().codecSupplier().get();
		List<BlockfileDecodedBlock<T>> decodedBlocks = new ArrayList<>(parsedBlocks.size());
		long totalCompressedSize = 0;
		long totalDecompressedSize = 0;
		for(ParsedBlock parsedBlock : parsedBlocks){
			if(config.validateChecksums()){
				validateChecksum(parsedBlock);
			}
			byte[] decompressedBytes = compressorCodec.decode(parsedBlock.compressedValue);
			T value = config.decoder().apply(decompressedBytes);
			var decodedBlock = new BlockfileDecodedBlock<>(
					numMetadataBytes + parsedBlock.compressedValue.length,
					numMetadataBytes + decompressedBytes.length,
					value);
			decodedBlocks.add(decodedBlock);
			totalCompressedSize += parsedBlock.compressedValue.length;
			totalDecompressedSize += decompressedBytes.length;
		}
		return new BlockfileDecodedBlockBatch<>(totalCompressedSize, totalDecompressedSize, decodedBlocks);
	}

	private void validateChecksum(ParsedBlock parsedBlock){
		byte[] expected = parsedBlock.checksum();
		byte[] actual = metadataReader.header().checksummer().encoder().apply(parsedBlock.compressedValue);
		if(!Arrays.equals(expected, actual)){
			String message = String.format(
					"invalid checksum: expected=%s, actual=%s",
					HexByteStringCodec.INSTANCE.encode(expected),
					HexByteStringCodec.INSTANCE.encode(actual));
			throw new RuntimeException(message);
		}
	}

	public BlockfileReaderConfig<T> config(){
		return config;
	}

}
