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

import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.BlockfileGroup;
import io.datarouter.bytes.blockfile.io.read.BlockfileConcatenatingReader.BlockfileConcatenatingReaderConfig;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.scanner.Threads;

public class BlockfileConcatenatingReaderBuilder<T>{

	public static final ByteLength DEFAULT_PREFETCH_BUFFER_SIZE = ByteLength.ofMiB(64);

	// required
	private final BlockfileGroup<T> blockfileGroup;
	private final Function<BlockfileRow,T> rowDecoder;
	private final ExecutorService prefetchExec;
	// optional
	private Threads readThreads = Threads.none();
	private ByteLength readChunkSize = BlockfileReaderBuilder.DEFAULT_READ_CHUNK_SIZE;
	private ByteLength prefetchBufferSize = DEFAULT_PREFETCH_BUFFER_SIZE;
	private int decodeBatchSize = BlockfileReaderBuilder.DEFAULT_DECODE_BATCH_SIZE;
	private Threads decodeThreads = Threads.none();
	private boolean validateChecksums = BlockfileReaderBuilder.DEFAULT_VALIDATE_CHECKSUMS;

	// construct
	public BlockfileConcatenatingReaderBuilder(
			BlockfileGroup<T> blockfile,
			Function<BlockfileRow,T> rowDecoder,
			ExecutorService prefetchExec){
		this.blockfileGroup = blockfile;
		this.rowDecoder = rowDecoder;
		this.prefetchExec = prefetchExec;
	}

	//options
	public BlockfileConcatenatingReaderBuilder<T> setReadThreads(Threads readThreads){
		this.readThreads = readThreads;
		return this;
	}

	public BlockfileConcatenatingReaderBuilder<T> setReadChunkSize(ByteLength readChunkSize){
		this.readChunkSize = readChunkSize;
		return this;
	}

	public BlockfileConcatenatingReaderBuilder<T> setPrefetchBufferSize(ByteLength prefetchBufferSize){
		this.prefetchBufferSize = prefetchBufferSize;
		return this;
	}

	public BlockfileConcatenatingReaderBuilder<T> setDecodeBatchSize(int decodeBatchSize){
		this.decodeBatchSize = decodeBatchSize;
		return this;
	}

	public BlockfileConcatenatingReaderBuilder<T> setDecodeThreads(Threads decodeThreads){
		this.decodeThreads = decodeThreads;
		return this;
	}

	public BlockfileConcatenatingReaderBuilder<T> enableChecksumValidation(){
		this.validateChecksums = true;
		return this;
	}

	// build
	public BlockfileConcatenatingReader<T> build(){
		var config = new BlockfileConcatenatingReaderConfig<>(
				blockfileGroup,
				rowDecoder,
				readThreads,
				readChunkSize,
				prefetchBufferSize,
				prefetchExec,
				decodeBatchSize,
				decodeThreads,
				validateChecksums);
		return new BlockfileConcatenatingReader<>(config);
	}

}
