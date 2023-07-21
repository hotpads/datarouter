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
package io.datarouter.bytes.kvfile.io.read;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.read.BlockfileReaderBuilder;
import io.datarouter.bytes.kvfile.codec.KvFileBlockCodec.KvFileBlockDecoder;
import io.datarouter.bytes.kvfile.io.KvFile;
import io.datarouter.bytes.kvfile.io.read.KvFileMetadataReader.KvFileMetadataReaderConfig;
import io.datarouter.bytes.kvfile.io.read.KvFileReader.KvFileReaderConfig;
import io.datarouter.bytes.kvfile.kv.KvFileEntry;
import io.datarouter.scanner.Threads;

public class KvFileReaderBuilder<T>{

	private final KvFile<T> kvFile;
	private final Function<byte[],List<T>> blockDecoder;
	private final String pathAndFile;
	private final BlockfileReaderBuilder<List<T>> blockfileReaderBuilder;

	public KvFileReaderBuilder(
			KvFile<T> kvFile,
			Function<KvFileEntry,T> decoder,
			String pathAndFile,
			Optional<Long> optKnownFileLength){//Shortcut to avoid a BuilderBuilder
		this.kvFile = kvFile;
		this.blockDecoder = new KvFileBlockDecoder<>(decoder)::decode;
		this.pathAndFile = pathAndFile;
		var blockfileMetadataReaderBuilder = kvFile.blockfile().newMetadataReaderBuilder(pathAndFile);
		optKnownFileLength.ifPresent(blockfileMetadataReaderBuilder::setKnownFileLength);
		var blockfileMetadataReader = blockfileMetadataReaderBuilder.build();
		blockfileReaderBuilder = kvFile.blockfile().newReaderBuilder(blockfileMetadataReader, blockDecoder);
	}

	/*----- BlockfileReaderBuilder pass-through methods -----*/

	public KvFileReaderBuilder<T> setReadThreads(Threads readThreads){
		blockfileReaderBuilder.setReadThreads(readThreads);
		return this;
	}

	public KvFileReaderBuilder<T> setReadChunkSize(ByteLength readChunkSize){
		blockfileReaderBuilder.setReadChunkSize(readChunkSize);
		return this;
	}

	public KvFileReaderBuilder<T> setDecodeBatchSize(int decodeBatchSize){
		blockfileReaderBuilder.setDecodeBatchSize(decodeBatchSize);
		return this;
	}

	public KvFileReaderBuilder<T> setDecodeThreads(Threads decodeThreads){
		blockfileReaderBuilder.setDecodeThreads(decodeThreads);
		return this;
	}

	public KvFileReaderBuilder<T> enableChecksumValidation(){
		blockfileReaderBuilder.enableChecksumValidation();
		return this;
	}

	/*----- build -----*/

	public KvFileReader<T> build(){
		var blockfileReader = blockfileReaderBuilder.build();
		var kvFileMetadataReaderConfig = new KvFileMetadataReaderConfig<>(
				blockfileReader.metadataReader(),
				kvFile.kvBlockFormats());
		var kvFileMetadataReader = new KvFileMetadataReader<>(kvFileMetadataReaderConfig);
		var kvFileReaderConfig = new KvFileReaderConfig<>(blockfileReader, kvFileMetadataReader);
		return new KvFileReader<>(kvFileReaderConfig);
	}


}
