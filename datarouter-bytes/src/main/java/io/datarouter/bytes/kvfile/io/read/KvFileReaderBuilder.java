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
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.blockfile.read.BlockfileReader;
import io.datarouter.bytes.blockfile.read.BlockfileReaderBuilder;
import io.datarouter.bytes.kvfile.block.KvFileBlockCodec;
import io.datarouter.bytes.kvfile.blockformat.KvFileBlockFormat;
import io.datarouter.bytes.kvfile.io.KvFile;
import io.datarouter.bytes.kvfile.io.read.KvFileMetadataReader.KvFileMetadataReaderConfig;
import io.datarouter.bytes.kvfile.io.read.KvFileReader.KvFileReaderConfig;
import io.datarouter.bytes.kvfile.kv.KvFileEntry;
import io.datarouter.scanner.Threads;

public class KvFileReaderBuilder<T>{

	private final KvFile<T> kvFile;
	private final BlockfileReaderBuilder<List<T>> blockfileReaderBuilder;

	public KvFileReaderBuilder(
			KvFile<T> kvFile,
			Codec<T,KvFileEntry> kvCodec,
			String pathAndFile,
			Optional<Long> optKnownFileLength){//Shortcut to avoid a BuilderBuilder
		this.kvFile = kvFile;
		var blockfileMetadataReaderBuilder = kvFile.blockfile().newMetadataReaderBuilder(pathAndFile);
		optKnownFileLength.ifPresent(blockfileMetadataReaderBuilder::setKnownFileLength);
		var blockfileMetadataReader = blockfileMetadataReaderBuilder.build();
		blockfileReaderBuilder = kvFile.blockfile().newReaderBuilder(
				blockfileMetadataReader,
				blockfileReader -> makeDecoderExtractorFn(kvCodec, blockfileReader));
	}

	private Function<byte[],List<T>> makeDecoderExtractorFn(
			Codec<T,KvFileEntry> kvCodec,
			BlockfileReader<List<T>> blockfileReader){
		KvFileBlockFormat blockFormat = makeKvFileMetadataReader(blockfileReader).header().blockFormat();
		KvFileBlockCodec<T> blockCodec = blockFormat.newBlockCodec(kvCodec);
		return blockCodec::decodeAll;
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
		var kvFileMetadataReader = makeKvFileMetadataReader(blockfileReader);
		var kvFileReaderConfig = new KvFileReaderConfig<>(
				blockfileReader,
				kvFileMetadataReader);
		return new KvFileReader<>(kvFileReaderConfig);
	}

	private KvFileMetadataReader<T> makeKvFileMetadataReader(
			BlockfileReader<List<T>> blockfileReader){
		var kvFileMetadataReaderConfig = new KvFileMetadataReaderConfig<>(
				blockfileReader.metadataReader(),
				kvFile.kvBlockFormats());
		return new KvFileMetadataReader<>(kvFileMetadataReaderConfig);
	}

}
