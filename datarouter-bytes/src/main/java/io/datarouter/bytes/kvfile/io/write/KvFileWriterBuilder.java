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
package io.datarouter.bytes.kvfile.io.write;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.blockfile.checksum.BlockfileChecksummer;
import io.datarouter.bytes.blockfile.compress.BlockfileCompressor;
import io.datarouter.bytes.blockfile.write.BlockfileWriterBuilder;
import io.datarouter.bytes.kvfile.codec.KvFileBlockCodec.KvFileBlockEncoder;
import io.datarouter.bytes.kvfile.io.KvFile;
import io.datarouter.bytes.kvfile.io.write.KvFileWriter.KvFileWriterConfig;
import io.datarouter.bytes.kvfile.kv.KvFileEntry;
import io.datarouter.scanner.Threads;

public class KvFileWriterBuilder<T>{

	private final BlockfileWriterBuilder<List<T>> blockfileWriterBuilder;
	private BinaryDictionary headerUserDictionary = new BinaryDictionary();
	private Supplier<BinaryDictionary> footerUserDictionarySupplier = BinaryDictionary::new;

	public KvFileWriterBuilder(
			KvFile<T> kvFile,
			Function<T,KvFileEntry> encoder,
			String pathAndFile){
		blockfileWriterBuilder = new BlockfileWriterBuilder<>(
				kvFile.blockfile(),
				new KvFileBlockEncoder<>(encoder)::encode,
				pathAndFile);
	}

	/*-------BlockfileWriterBuilder pass-through options --------*/

	public KvFileWriterBuilder<T> setEncodeBatchSize(int encodeBatchSize){
		blockfileWriterBuilder.setEncodeBatchSize(encodeBatchSize);
		return this;
	}

	public KvFileWriterBuilder<T> setEncodeThreads(Threads encodeThreads){
		blockfileWriterBuilder.setEncodeThreads(encodeThreads);
		return this;
	}

	public KvFileWriterBuilder<T> setCompressor(BlockfileCompressor compressor){
		blockfileWriterBuilder.setCompressor(compressor);
		return this;
	}

	public KvFileWriterBuilder<T> setChecksummer(BlockfileChecksummer checksummer){
		blockfileWriterBuilder.setChecksummer(checksummer);
		return this;
	}

	public KvFileWriterBuilder<T> setMultipartWrite(boolean multipartWrite){
		blockfileWriterBuilder.setMultipartWrite(multipartWrite);
		return this;
	}

	/**
	 * When you know the total file size will be small enough to buffer everything in memory.
	 * Removes the overhead of a multipart upload.
	 */
	public KvFileWriterBuilder<T> disableMultipartWrite(){
		return setMultipartWrite(false);
	}

	public KvFileWriterBuilder<T> setWriteThreads(Threads writeThreads){
		blockfileWriterBuilder.setWriteThreads(writeThreads);
		return this;
	}

	/*------ KvFileWriter options -------*/

	public KvFileWriterBuilder<T> setHeaderDictionary(BinaryDictionary headerDictionary){
		this.headerUserDictionary = headerDictionary;
		return this;
	}

	public KvFileWriterBuilder<T> setFooterDictionarySupplier(Supplier<BinaryDictionary> footerDictionarySupplier){
		this.footerUserDictionarySupplier = footerDictionarySupplier;
		return this;
	}

	/*------ build ------*/

	public KvFileWriter<T> build(){
		var kvFileWriterConfig = new KvFileWriterConfig<>(
				blockfileWriterBuilder,
				headerUserDictionary,
				footerUserDictionarySupplier);
		return new KvFileWriter<>(kvFileWriterConfig);
	}

}
