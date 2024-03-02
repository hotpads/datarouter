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

import java.util.Optional;
import java.util.function.Function;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.BlockfileGroup;
import io.datarouter.bytes.blockfile.block.decoded.BlockfileHeaderBlock.BlockfileHeaderCodec;
import io.datarouter.bytes.blockfile.io.read.BlockfileReader.BlockfileReaderConfig;
import io.datarouter.bytes.blockfile.io.read.metadata.BlockfileMetadataReader;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.scanner.Threads;

public class BlockfileReaderBuilder<T>{

	// required
	private final BlockfileGroup<T> blockfileGroup;
	private final BlockfileMetadataReader<T> metadataReader;
	private final Function<BlockfileRow,T> rowDecoder;
	// optional
	private Threads readThreads = Threads.none();
	private ByteLength readChunkSize = ByteLength.ofMiB(4);
	private int decodeBatchSize = 1;
	private Threads decodeThreads = Threads.none();
	private boolean validateChecksums = false;
	private Optional<Long> knownFileLength = Optional.empty();

	// construct
	public BlockfileReaderBuilder(
			BlockfileGroup<T> blockfile,
			BlockfileMetadataReader<T> metadataReader,
			Function<BlockfileRow,T> rowDecoder){
		this.blockfileGroup = blockfile;
		this.metadataReader = metadataReader;
		this.rowDecoder = rowDecoder;
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
				blockfileGroup.registeredValueBlockFormats(),
				blockfileGroup.registeredIndexBlockFormats(),
				blockfileGroup.registeredCompressors(),
				blockfileGroup.registeredChecksummers());
		var config = new BlockfileReaderConfig<>(
				blockfileGroup.storage(),
				rowDecoder,
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
