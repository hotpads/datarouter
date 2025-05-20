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

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.RecordByteArrayField;
import io.datarouter.bytes.blockfile.block.decoded.BlockfileFooterBlock;
import io.datarouter.bytes.blockfile.block.decoded.BlockfileHeaderBlock;
import io.datarouter.bytes.blockfile.block.decoded.BlockfileHeaderBlock.BlockfileHeaderCodec;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileBaseTokens;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileFooterTokens;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileHeaderTokens;
import io.datarouter.bytes.blockfile.encoding.checksum.BlockfileChecksummer;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileCompressor;
import io.datarouter.bytes.blockfile.encoding.indexblock.BlockfileIndexBlockCodec;
import io.datarouter.bytes.blockfile.encoding.indexblock.BlockfileIndexBlockFormat;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockCodec.BlockfileValueBlockRows;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockEncoder;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockEncoder.BlockfileValueBlockEncoderConfig;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockFormat;
import io.datarouter.bytes.blockfile.index.BlockfileByteRange;
import io.datarouter.bytes.blockfile.index.BlockfileRowIdRange;
import io.datarouter.bytes.blockfile.index.BlockfileRowRange;
import io.datarouter.bytes.blockfile.io.storage.BlockfileStorage;
import io.datarouter.bytes.blockfile.io.write.listener.BlockfileListener;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.bytes.io.MultiByteArrayInputStream;
import io.datarouter.scanner.ObjectScanner;
import io.datarouter.scanner.PagedList;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;

public class BlockfileWriter<T>{

	public record BlockfileWriterConfig(
			BlockfileStorage storage,
			BlockfileHeaderCodec headerCodec,
			BlockfileValueBlockFormat valueBlockFormat,
			BlockfileIndexBlockFormat indexBlockFormat,
			BlockfileCompressor compressor,
			BlockfileChecksummer checksummer,
			BinaryDictionary userDictionary,
			Supplier<BinaryDictionary> footerUserDictionarySupplier,
			List<BlockfileListener> listeners,
			int encodeBatchSize,
			Threads encodeThreads,
			boolean multipartWrite,
			Threads writeThreads,
			ByteLength minWritePartSize,
			int indexFanOut,
			Optional<ByteLength> optTargetIndexBlockSize){
	}

	private final BlockfileWriterConfig config;
	private final String name;
	private final BlockfileIndexBlockCodec indexBlockCodec;
	private final BlockfileWriterState state;
	private final BlockfileIndexer indexer;
	private final BlockfileValueBlockEncoder valueBlockEncoder;
	private final BlockfileHeaderBlock header;

	public BlockfileWriter(BlockfileWriterConfig config, String name){
		this.config = config;
		this.name = name;
		indexBlockCodec = config.indexBlockFormat().supplier().get();
		state = new BlockfileWriterState();
		indexer = new BlockfileIndexer(
				state,
				config.indexFanOut(),
				config.optTargetIndexBlockSize(),
				indexBlockCodec);
		var encodingManagerConfig = new BlockfileValueBlockEncoderConfig(
				config.valueBlockFormat(),
				config.compressor(),
				config.checksummer());
		valueBlockEncoder = new BlockfileValueBlockEncoder(encodingManagerConfig);
		header = new BlockfileHeaderBlock(
				config.userDictionary(),
				config.valueBlockFormat(),
				config.indexBlockFormat(),
				config.compressor(),
				config.checksummer());
	}

	public BlockfileWriterConfig config(){
		return config;
	}

	public BlockfileWriterState state(){
		return state;
	}

	/*------------ write -------------*/

	public record BlockfileWriteResult(
			long numValueBlocks,
			ByteLength fileLength){
	}

	public BlockfileWriteResult writeItems(
			Function<T,BlockfileRow> rowEncoder,
			ByteLength targetValueBlockSize,
			Scanner<T> rows){
		return rows
				.map(rowEncoder)
				.batchByMinSize(targetValueBlockSize.toBytes(), BlockfileRow::length)
				.apply(this::writeBlocks);
	}

	public BlockfileWriteResult writeRows(
			ByteLength targetValueBlockSize,
			Scanner<BlockfileRow> rows){
		return rows
				.batchByMinSize(targetValueBlockSize.toBytes(), BlockfileRow::length)
				.apply(this::writeBlocks);
	}

	public BlockfileWriteResult writeBlocks(
			Scanner<List<BlockfileRow>> items){
		BlockfileBaseTokens headerTokens = makeHeaderTokens();
		state.appendHeaderBlock(headerTokens);
		Scanner<BlockfileBaseTokens> tokenGroups = ObjectScanner.of(headerTokens)
				// value blocks and occasional index blocks
				.append(makeValueAndIndexScanner(items))
				// remaining partial index blocks
				.append(makeFinalIndexScanner())
				// build the footer lazily, after the value/index blocks are processed
				.append(makeFooterScanner());
		for(BlockfileListener listener : config.listeners()){
			tokenGroups = tokenGroups.each(listener::accept);
		}
		tokenGroups
				.concatIter(BlockfileBaseTokens::toList)
				.then(this::persist);
		config.listeners().forEach(BlockfileListener::complete);
		return new BlockfileWriteResult(
				state.numValueBlocks(),
				ByteLength.ofBytes(state.cursor()));
	}

	private void persist(Scanner<byte[]> tokens){
		if(config.multipartWrite()){
			InputStream inputStream = tokens.apply(MultiByteArrayInputStream::new);
			config.storage().write(name, inputStream, config.writeThreads(), config.minWritePartSize());
		}else{
			List<byte[]> allTokens = tokens.collect(PagedList::new);
			byte[] bytes = ByteTool.concat(allTokens);
			config.storage().write(name, bytes);
		}
	}

	/*--------- generate block tokens -----------*/

	private BlockfileHeaderTokens makeHeaderTokens(){
		byte[] headerValueBytes = config.headerCodec().encode(header);
		int headerBlockLength = BlockfileBaseTokens.NUM_PREFIX_BYTES + headerValueBytes.length;
		return new BlockfileHeaderTokens(headerBlockLength, headerValueBytes);
	}

	private Scanner<BlockfileBaseTokens> makeValueAndIndexScanner(Scanner<List<BlockfileRow>> blocksOfRows){
		return blocksOfRows
				.map(blockOfRows -> new BlockfileValueBlockRows(
						// We don't know the globalBlockId here.
						state.takeValueBlockId(),
						state.getNumItemsAndAdd(blockOfRows.size()),
						blockOfRows))
				.batch(config.encodeBatchSize())
				.parallelOrdered(config.encodeThreads())
				.map(valueBlockEncoder::encodeValueBlocks)
				.concat(Scanner::of)
				.each(valueBlock -> {
					BlockfileRowIdRange rowIdRange = valueBlock.toRowIdRange();
					BlockfileRowRange rowRange = valueBlock.toRowKeyRange();
					BlockfileByteRange byteRange = state.appendValueBlock(valueBlock);
					indexer.onValueBlockWrite(
							state.previousGlobalBlockId(),
							valueBlock.valueBlockId(),
							rowIdRange,
							rowRange,
							byteRange);
				})
				.map(BlockfileBaseTokens.class::cast)
				.concat(valueBlock -> ObjectScanner.of(valueBlock)
						.append(indexer.drainCompletedBlocks()
								.map(BlockfileBaseTokens.class::cast)));
	}

	private Scanner<BlockfileBaseTokens> makeFinalIndexScanner(){
		return indexer.drainAllBlocks()
				.map(BlockfileBaseTokens.class::cast);
	}

	private Scanner<BlockfileBaseTokens> makeFooterScanner(){
		return Scanner.of(config.footerUserDictionarySupplier())
				.map(Supplier::get)
				.map(footerUserDictionary -> {
					var footer = new BlockfileFooterBlock(
							new RecordByteArrayField(config.headerCodec().encode(header)),
							footerUserDictionary,
							state.headerBlockLocation(),
							state.latestIndexBlockLocation(),
							state.numValueBlocks(),
							state.numIndexBlocks());
					byte[] footerValueBytes = BlockfileFooterBlock.VALUE_CODEC.encode(footer);
					return encodeFooter(footerValueBytes);
				})
				.each(state::appendFooterBlock);
	}

	/*------------ helpers ------------*/

	public static BlockfileBaseTokens encodeFooter(byte[] footerValueBytes){
		int footerBlockLength = BlockfileBaseTokens.NUM_PREFIX_BYTES
				+ footerValueBytes.length
				+ BlockfileBaseTokens.NUM_LENGTH_BYTES;
		return new BlockfileFooterTokens(footerBlockLength, footerValueBytes);
	}

}
