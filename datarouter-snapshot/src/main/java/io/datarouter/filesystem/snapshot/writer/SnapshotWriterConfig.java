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
package io.datarouter.filesystem.snapshot.writer;

import java.util.function.Function;
import java.util.function.Supplier;

import io.datarouter.filesystem.snapshot.compress.BlockCompressor;
import io.datarouter.filesystem.snapshot.encode.BranchBlockEncoder;
import io.datarouter.filesystem.snapshot.encode.LeafBlockEncoder;
import io.datarouter.filesystem.snapshot.encode.RootBlockEncoder;
import io.datarouter.filesystem.snapshot.encode.ValueBlockEncoder;
import io.datarouter.filesystem.snapshot.path.SnapshotPaths;
import io.datarouter.scanner.Scanner;

public record SnapshotWriterConfig(
		boolean sorted,
		int numColumns,

		int batchQueueLength,
		int numThreads,
		long logPeriodMs,
		boolean compressorConcatChunks,
		boolean persist,
		boolean updateCache,

		Supplier<SnapshotPaths> pathsSupplier,

		int leafEncoderChunkSize,

		int branchBlockSize,
		int leafBlockSize,
		int valueBlockSize,

		int branchBytesPerFile,
		int leafBytesPerFile,
		int valueBytesPerFile,

		int branchBlocksPerFile,
		int leafBlocksPerFile,
		int valueBlocksPerFile,

		Supplier<RootBlockEncoder> rootBlockEncoderSupplier,
		Function<Integer,BranchBlockEncoder> branchBlockEncoderFactory,
		Supplier<LeafBlockEncoder> leafBlockEncoderSupplier,
		Supplier<ValueBlockEncoder> valueBlockEncoderSupplier,

		BlockCompressor branchBlockCompressor,
		BlockCompressor leafBlockCompressor,
		BlockCompressor valueBlockCompressor){

	public Scanner<Integer> columnIds(){
		return Scanner.iterate(0, i -> i + 1)
				.limit(numColumns);
	}

}
