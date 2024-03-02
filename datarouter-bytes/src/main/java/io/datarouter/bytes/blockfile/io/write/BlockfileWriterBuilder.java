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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.BlockfileGroup;
import io.datarouter.bytes.blockfile.block.decoded.BlockfileHeaderBlock.BlockfileHeaderCodec;
import io.datarouter.bytes.blockfile.encoding.checksum.BlockfileChecksummer;
import io.datarouter.bytes.blockfile.encoding.checksum.BlockfileStandardChecksummers;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileCompressor;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileStandardCompressors;
import io.datarouter.bytes.blockfile.encoding.indexblock.BlockfileIndexBlockFormat;
import io.datarouter.bytes.blockfile.encoding.indexblock.BlockfileStandardIndexBlockFormats;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileStandardValueBlockFormats;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockFormat;
import io.datarouter.bytes.blockfile.io.write.BlockfileWriter.BlockfileWriterConfig;
import io.datarouter.bytes.blockfile.io.write.listener.BlockfileListener;
import io.datarouter.scanner.Threads;

public class BlockfileWriterBuilder<T>{

	// required
	private final BlockfileGroup<T> blockfileGroup;
	private final String name;
	// optional
	private BinaryDictionary headerDictionary = new BinaryDictionary();
	private Supplier<BinaryDictionary> footerDictionarySupplier = BinaryDictionary::new;
	private List<BlockfileListener> listeners = new ArrayList<>();
	private int encodeBatchSize = 1;
	private Threads encodeThreads = Threads.none();
	private BlockfileValueBlockFormat valueBlockFormat = BlockfileStandardValueBlockFormats.SEQUENTIAL;
	private BlockfileIndexBlockFormat indexBlockFormat = BlockfileStandardIndexBlockFormats.V1;
	private BlockfileCompressor compressor = BlockfileStandardCompressors.NONE;
	private BlockfileChecksummer checksummer = BlockfileStandardChecksummers.NONE;
	private boolean multipartWrite = true;
	private Threads writeThreads = Threads.none();
	private Optional<ByteLength> optTargetIndexBlockSize = Optional.empty();
	private int indexFanOut = 100;

	// construct
	public BlockfileWriterBuilder(
			BlockfileGroup<T> blockfile,
			String name){
		this.blockfileGroup = blockfile;
		this.name = name;
	}

	// options
	public BlockfileWriterBuilder<T> setHeaderDictionary(BinaryDictionary headerDictionary){
		this.headerDictionary = headerDictionary;
		return this;
	}

	public BlockfileWriterBuilder<T> setFooterDictionarySupplier(Supplier<BinaryDictionary> footerDictionarySupplier){
		this.footerDictionarySupplier = footerDictionarySupplier;
		return this;
	}

	public BlockfileWriterBuilder<T> addListener(BlockfileListener listener){
		listeners.add(listener);
		return this;
	}

	public BlockfileWriterBuilder<T> addListeners(BlockfileListener... listeners){
		Arrays.asList(listeners).forEach(this::addListener);
		return this;
	}

	public BlockfileWriterBuilder<T> setEncodeBatchSize(int encodeBatchSize){
		this.encodeBatchSize = encodeBatchSize;
		return this;
	}

	public BlockfileWriterBuilder<T> setEncodeThreads(Threads encodeThreads){
		this.encodeThreads = encodeThreads;
		return this;
	}

	public BlockfileWriterBuilder<T> setValueBlockFormat(BlockfileValueBlockFormat format){
		this.valueBlockFormat = format;
		return this;
	}

	public BlockfileWriterBuilder<T> setIndexBlockFormat(BlockfileIndexBlockFormat format){
		this.indexBlockFormat = format;
		return this;
	}

	public BlockfileWriterBuilder<T> setCompressor(BlockfileCompressor compressor){
		this.compressor = compressor;
		return this;
	}

	public BlockfileWriterBuilder<T> setChecksummer(BlockfileChecksummer checksummer){
		this.checksummer = checksummer;
		return this;
	}

	public BlockfileWriterBuilder<T> setMultipartWrite(boolean multipartWrite){
		this.multipartWrite = multipartWrite;
		return this;
	}

	/**
	 * When you know the total file size will be small enough to buffer everything in memory.
	 * Removes the overhead of a multipart upload.
	 */
	public BlockfileWriterBuilder<T> disableMultipartWrite(){
		return setMultipartWrite(false);
	}

	public BlockfileWriterBuilder<T> setWriteThreads(Threads writeThreads){
		this.writeThreads = writeThreads;
		return this;
	}

	public BlockfileWriterBuilder<T> setIndexFanOut(int indexFanOut){
		this.indexFanOut = indexFanOut;
		return this;
	}

	/**
	 * For dynamic index block sizing, for example to get large index blocks without estimating.
	 * This will override the indexFanOut.
	 */
	public BlockfileWriterBuilder<T> setTargetIndexBlockSize(ByteLength optTargetIndexBlockSize){
		this.optTargetIndexBlockSize = Optional.of(optTargetIndexBlockSize);
		return this;
	}

	// build
	public BlockfileWriter<T> build(){
		var headerCodec = new BlockfileHeaderCodec(
				blockfileGroup.registeredValueBlockFormats(),
				blockfileGroup.registeredIndexBlockFormats(),
				blockfileGroup.registeredCompressors(),
				blockfileGroup.registeredChecksummers());
		var config = new BlockfileWriterConfig(
				blockfileGroup.storage(),
				headerCodec,
				valueBlockFormat,
				indexBlockFormat,
				compressor,
				checksummer,
				headerDictionary,
				footerDictionarySupplier,
				listeners,
				encodeBatchSize,
				encodeThreads,
				multipartWrite,
				writeThreads,
				indexFanOut,
				optTargetIndexBlockSize);
		return new BlockfileWriter<>(config, name);
	}

}
