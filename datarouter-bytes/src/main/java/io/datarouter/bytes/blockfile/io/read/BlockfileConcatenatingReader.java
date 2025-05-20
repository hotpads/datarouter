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

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.BlockfileGroup;
import io.datarouter.bytes.blockfile.io.storage.BlockfileStorage.FilenameAndInputStream;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;

public class BlockfileConcatenatingReader<T>{

	public record BlockfileConcatenatingReaderConfig<T>(
			BlockfileGroup<T> group,
			Function<BlockfileRow,T> rowDecoder,
			Threads readThreads,
			ByteLength readChunkSize,
			ByteLength prefetchBufferSize,
			ExecutorService prefetchExec,
			int decodeBatchSize,
			Threads decodeThreads,
			boolean validateChecksums){

		public BlockfileReaderBuilder<T> applyTo(BlockfileReaderBuilder<T> readerBuilder){
			return readerBuilder
					.setReadThreads(readThreads)
					.setReadChunkSize(readChunkSize)
					.setDecodeBatchSize(decodeBatchSize)
					.setDecodeThreads(decodeThreads)
					.setValidateChecksums(validateChecksums);
		}
	}

	private final BlockfileConcatenatingReaderConfig<T> config;

	public BlockfileConcatenatingReader(
			BlockfileConcatenatingReaderConfig<T> config){
		this.config = config;
	}

	/**
	 * Uses a BlobPrefetcher behind the scenes to speed up fetching across multiple files.
	 */
	public Scanner<T> scan(Scanner<String> pathAndFileScanner){
		Scanner<FilenameAndInputStream> inputStreamScanner = config.group().storage().readInputStreams(
				pathAndFileScanner,
				config.readThreads(),
				config.readChunkSize(),
				config.prefetchBufferSize(),
				config.prefetchExec());
		return inputStreamScanner
				.concat(filenameAndInputStream -> {
					BlockfileReaderBuilder<T> readerBuilder = config.group().newReaderBuilder(
							filenameAndInputStream.filename(),
							config.rowDecoder());
					config.applyTo(readerBuilder);
					BlockfileReader<T> reader = readerBuilder.build();
					InputStream inputStream = filenameAndInputStream.inputStream();
					return reader.sequentialSingleUse(inputStream).scan();
				});
	}

}
