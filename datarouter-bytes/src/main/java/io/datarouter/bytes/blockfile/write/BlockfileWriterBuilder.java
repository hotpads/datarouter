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
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.blockfile.Blockfile;
import io.datarouter.bytes.blockfile.checksum.BlockfileChecksummer;
import io.datarouter.bytes.blockfile.checksum.BlockfileStandardChecksummers;
import io.datarouter.bytes.blockfile.compress.BlockfileCompressor;
import io.datarouter.bytes.blockfile.compress.BlockfileStandardCompressors;
import io.datarouter.bytes.blockfile.section.BlockfileHeader.BlockfileHeaderCodec;
import io.datarouter.bytes.blockfile.write.BlockfileWriter.BlockfileWriterConfig;
import io.datarouter.scanner.Threads;

public class BlockfileWriterBuilder<T>{

	// required
	private final Blockfile<T> blockfile;
	private final Function<T,byte[]> encoder;
	private final String name;
	// optional
	private BinaryDictionary headerDictionary = new BinaryDictionary();
	private Supplier<BinaryDictionary> footerDictionarySupplier = BinaryDictionary::new;
	private List<BlockfileListener> listeners = new ArrayList<>();
	private int encodeBatchSize = 1;
	private Threads encodeThreads = Threads.none();
	private BlockfileCompressor compressor = BlockfileStandardCompressors.NONE;
	private BlockfileChecksummer checksummer = BlockfileStandardChecksummers.NONE;
	private boolean multipartWrite = true;
	private Threads writeThreads = Threads.none();

	// construct
	public BlockfileWriterBuilder(
			Blockfile<T> blockfile,
			Function<T,byte[]> encoder,
			String name){
		this.blockfile = blockfile;
		this.encoder = encoder;
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

	// build
	public BlockfileWriter<T> build(){
		var headerCodec = new BlockfileHeaderCodec(
				blockfile.registeredCompressors(),
				blockfile.registeredChecksummers());
		var config = new BlockfileWriterConfig<>(
				blockfile.storage(),
				encoder,
				headerCodec,
				compressor,
				checksummer,
				headerDictionary,
				footerDictionarySupplier,
				listeners,
				encodeBatchSize,
				encodeThreads,
				multipartWrite,
				writeThreads);
		return new BlockfileWriter<>(config, name);
	}

}
