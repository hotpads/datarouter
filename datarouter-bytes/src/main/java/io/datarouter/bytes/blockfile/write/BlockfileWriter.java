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
package io.datarouter.bytes.blockfile.write;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.blockfile.checksum.BlockfileChecksummer;
import io.datarouter.bytes.blockfile.compress.BlockfileCompressor;
import io.datarouter.bytes.blockfile.dto.BlockfileTokens;
import io.datarouter.bytes.blockfile.dto.tokens.BlockfileBlockTokens;
import io.datarouter.bytes.blockfile.dto.tokens.BlockfileFooterTokens;
import io.datarouter.bytes.blockfile.dto.tokens.BlockfileHeaderTokens;
import io.datarouter.bytes.blockfile.section.BlockfileFooter;
import io.datarouter.bytes.blockfile.section.BlockfileHeader;
import io.datarouter.bytes.blockfile.section.BlockfileHeader.BlockfileHeaderCodec;
import io.datarouter.bytes.blockfile.section.BlockfileTrailer;
import io.datarouter.bytes.blockfile.storage.BlockfileStorage;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;
import io.datarouter.bytes.io.MultiByteArrayInputStream;
import io.datarouter.scanner.ObjectScanner;
import io.datarouter.scanner.PagedList;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;

public class BlockfileWriter<T>{

	public record BlockfileWriterConfig<T>(
			BlockfileStorage storage,
			Function<T,byte[]> encoder,
			BlockfileHeaderCodec headerCodec,
			BlockfileCompressor compressor,
			BlockfileChecksummer checksummer,
			BinaryDictionary userDictionary,
			Supplier<BinaryDictionary> footerUserDictionarySupplier,
			List<BlockfileListener> listeners,
			int encodeBatchSize,
			Threads encodeThreads,
			boolean multipartWrite,
			Threads writeThreads){
	}

	// Don't change these - it will break decoding of existing blockfiles.
	public static final int NUM_VALUE_LENGTH_BYTES = RawIntCodec.INSTANCE.length();
	public static final int NUM_SECTION_BYTES = 1;
	public static final int NUM_HEADER_METADATA_BYTES = RawIntCodec.INSTANCE.length() + NUM_SECTION_BYTES;
	public static final int NUM_FOOTER_METADATA_BYTES = RawIntCodec.INSTANCE.length() + NUM_SECTION_BYTES;
	public static final int NUM_TRAILER_BYTES = RawIntCodec.INSTANCE.length() + RawIntCodec.INSTANCE.length();

	private final BlockfileWriterConfig<T> config;
	private final String name;
	private final AtomicLong dataBlockCounter = new AtomicLong();
	private final AtomicInteger footerBlockLength = new AtomicInteger();
	private final AtomicLong fileLengthBytesCounter = new AtomicLong();

	public BlockfileWriter(BlockfileWriterConfig<T> config, String name){
		this.config = config;
		this.name = name;
	}

	/*------------ write -------------*/

	public record BlockfileWriteResult(
			long numDataBlocks,
			ByteLength fileLength){
	}

	public BlockfileWriteResult write(Scanner<T> items){
		BlockfileTokens headerTokens = makeHeaderTokens();
		Scanner<BlockfileTokens> tokenGroups = ObjectScanner.of(headerTokens)
				.append(makeBlockScanner(items))
				// build the footer lazily, after the DataBlocks are scanned
				.append(makeFooterScanner())
				// build the trailer lazily, after the footerValueLength is set
				.append(makeTrailerScanner(headerTokens))
				.each(token -> fileLengthBytesCounter.addAndGet(token.totalLength()));
		for(BlockfileListener listener : config.listeners()){
			tokenGroups = tokenGroups.each(listener::accept);
		}
		Scanner<byte[]> tokens = tokenGroups
				.concatIter(BlockfileTokens::toList);
		if(config.multipartWrite()){
			config.storage().write(
					name,
					tokens.apply(MultiByteArrayInputStream::new),
					config.writeThreads());
		}else{
			List<byte[]> allTokens = tokens.collect(PagedList::new);
			config.storage().write(
					name,
					ByteTool.concat(allTokens));
		}
		config.listeners().forEach(BlockfileListener::complete);
		return new BlockfileWriteResult(
				dataBlockCounter.get(),
				ByteLength.ofBytes(fileLengthBytesCounter.get()));
	}

	private BlockfileTokens makeHeaderTokens(){
		var header = new BlockfileHeader(
				config.userDictionary(),
				config.compressor(),
				config.checksummer().numBytes(),
				config.checksummer());
		byte[] headerValueBytes = config.headerCodec().encode(header);
		int headerBlockLength = NUM_HEADER_METADATA_BYTES + headerValueBytes.length;
		return new BlockfileHeaderTokens(
				RawIntCodec.INSTANCE.encode(headerBlockLength),
				headerValueBytes);
	}

	private Scanner<BlockfileTokens> makeBlockScanner(Scanner<T> items){
		return items
				.batch(config.encodeBatchSize())
				.parallelOrdered(config.encodeThreads())
				.map(this::encodeBlocks)
				.each(blockBatch -> dataBlockCounter.addAndGet(blockBatch.size()))
				.concat(Scanner::of)
				.map(BlockfileTokens.class::cast);
	}

	private Scanner<BlockfileTokens> makeFooterScanner(){
		return Scanner.of(config.footerUserDictionarySupplier())
				.map(Supplier::get)
				.map(footerUserDictionary -> {
					var footer = new BlockfileFooter(footerUserDictionary, dataBlockCounter.get());
					byte[] footerValueBytes = BlockfileFooter.VALUE_CODEC.encode(footer);
					BlockfileTokens footerTokens = encodeFooter(footerValueBytes);
					footerBlockLength.set(footerTokens.totalLength());
					return footerTokens;
				});
	}

	private Scanner<BlockfileTokens> makeTrailerScanner(BlockfileTokens headerTokens){
		Supplier<BlockfileTrailer> trailerSupplier = () -> new BlockfileTrailer(
				headerTokens.totalLength(),
				footerBlockLength.get());
		return Scanner.of(trailerSupplier)
				.map(Supplier::get)
				.map(BlockfileTrailer::encode);
	}

	/*
	 * Encode:
	 *
	 * For each block: do the encoding, compression, and checksumming together.
	 * This can help with performance as the data for each block is more likely to stay in L1/L2 caches.
	 */

	public int numBlockMetadataBytes(){
		return NUM_VALUE_LENGTH_BYTES
				+ config.checksummer().numBytes()
				+ NUM_SECTION_BYTES;
	}

	public List<BlockfileBlockTokens<T>> encodeBlocks(List<T> blocks){
		Codec<byte[],byte[]> compressorCodec = config.compressor().codecSupplier().get();
		return Scanner.of(blocks)
				.map(block -> encodeBlock(compressorCodec, block))
				.collect(() -> new ArrayList<>(blocks.size()));
	}

	public BlockfileBlockTokens<T> encodeBlock(Codec<byte[],byte[]> compressorCodec, T item){
		byte[] encodedBytes = config.encoder().apply(item);
		byte[] compressedBytes = compressorCodec.encode(encodedBytes);
		int blockLength = numBlockMetadataBytes() + compressedBytes.length;
		byte[] blockLengthBytes = RawIntCodec.INSTANCE.encode(blockLength);
		byte[] checksumBytes = config.checksummer().encoder().apply(compressedBytes);
		return new BlockfileBlockTokens<>(
				item,
				blockLengthBytes,
				checksumBytes,
				compressedBytes);
	}

	public static BlockfileTokens encodeFooter(byte[] footerValueBytes){
		int footerBlockLength = NUM_FOOTER_METADATA_BYTES + footerValueBytes.length;
		return new BlockfileFooterTokens(
				RawIntCodec.INSTANCE.encode(footerBlockLength),
				footerValueBytes);
	}

	public BlockfileWriterConfig<T> config(){
		return config;
	}

}
