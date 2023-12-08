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

import java.util.Optional;
import java.util.function.Function;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.Blockfile;
import io.datarouter.bytes.blockfile.read.BlockfileReader.BlockfileReaderConfig;
import io.datarouter.bytes.blockfile.section.BlockfileHeader.BlockfileHeaderCodec;
import io.datarouter.scanner.Threads;

public class BlockfileReaderBuilder<T>{

	// required
	private final Blockfile<T> blockfile;
	private final BlockfileMetadataReader<T> metadataReader;
	private final Function<BlockfileReader<T>,Function<byte[],T>> decoderExtractor;
	// optional
	private Threads readThreads = Threads.none();
	private ByteLength readChunkSize = ByteLength.ofMiB(4);
	private int decodeBatchSize = 1;
	private Threads decodeThreads = Threads.none();
	private boolean validateChecksums = false;
	private Optional<Long> knownFileLength = Optional.empty();

	// construct
	public BlockfileReaderBuilder(
			Blockfile<T> blockfile,
			BlockfileMetadataReader<T> metadataReader,
			Function<BlockfileReader<T>,Function<byte[],T>> decoderExtractor){
		this.blockfile = blockfile;
		this.metadataReader = metadataReader;
		this.decoderExtractor = decoderExtractor;
	}

	//options
	public BlockfileReaderBuilder<T> setReadThreads(Threads readThreads){
		this.readThreads = readThreads;
		return this;
	}

	public BlockfileReaderBuilder<T> setReadChunkSize(ByteLength readChunkSize){
		this.readChunkSize = readChunkSize;
		return this;
	}

	public BlockfileReaderBuilder<T> setDecodeBatchSize(int decodeBatchSize){
		this.decodeBatchSize = decodeBatchSize;
		return this;
	}

	public BlockfileReaderBuilder<T> setDecodeThreads(Threads decodeThreads){
		this.decodeThreads = decodeThreads;
		return this;
	}

	public BlockfileReaderBuilder<T> enableChecksumValidation(){
		this.validateChecksums = true;
		return this;
	}

	// build
	public BlockfileReader<T> build(){
		var headerCodec = new BlockfileHeaderCodec(
				blockfile.registeredCompressors(),
				blockfile.registeredChecksummers());
		var config = new BlockfileReaderConfig<>(
				blockfile.storage(),
				decoderExtractor,
				headerCodec,
				readThreads,
				readChunkSize,
				decodeBatchSize,
				decodeThreads,
				validateChecksums,
				knownFileLength);
		return new BlockfileReader<>(metadataReader, config);
	}

}
