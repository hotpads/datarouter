/**
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

public class SnapshotWriterConfig{

	public final boolean sorted;
	public final int numColumns;

	public final int batchQueueLength;
	public final int numThreads;
	public final long logPeriodMs;
	public final boolean compressorConcatChunks;
	public final boolean persist;
	public final boolean updateCache;

	public final Supplier<SnapshotPaths> pathsSupplier;

	public final int leafEncoderChunkSize;

	public final int branchBlockSize;
	public final int leafBlockSize;
	public final int valueBlockSize;

	public final int branchBytesPerFile;
	public final int leafBytesPerFile;
	public final int valueBytesPerFile;

	public final int branchBlocksPerFile;
	public final int leafBlocksPerFile;
	public final int valueBlocksPerFile;

	public final Supplier<RootBlockEncoder> rootBlockEncoderSupplier;
	public final Function<Integer,BranchBlockEncoder> branchBlockEncoderFactory;
	public final Supplier<LeafBlockEncoder> leafBlockEncoderSupplier;
	public final Supplier<ValueBlockEncoder> valueBlockEncoderSupplier;

	public final BlockCompressor branchBlockCompressor;
	public final BlockCompressor leafBlockCompressor;
	public final BlockCompressor valueBlockCompressor;

	public SnapshotWriterConfig(
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
		this.sorted = sorted;
		this.numColumns = numColumns;

		this.batchQueueLength = batchQueueLength;
		this.numThreads = numThreads;
		this.logPeriodMs = logPeriodMs;
		this.compressorConcatChunks = compressorConcatChunks;
		this.persist = persist;
		this.updateCache = updateCache;

		this.pathsSupplier = pathsSupplier;

		this.leafEncoderChunkSize = leafEncoderChunkSize;

		this.branchBlockSize = branchBlockSize;
		this.leafBlockSize = leafBlockSize;
		this.valueBlockSize = valueBlockSize;

		this.branchBytesPerFile = branchBytesPerFile;
		this.leafBytesPerFile = leafBytesPerFile;
		this.valueBytesPerFile = valueBytesPerFile;

		this.branchBlocksPerFile = branchBlocksPerFile;
		this.leafBlocksPerFile = leafBlocksPerFile;
		this.valueBlocksPerFile = valueBlocksPerFile;

		this.rootBlockEncoderSupplier = rootBlockEncoderSupplier;
		this.branchBlockEncoderFactory = branchBlockEncoderFactory;
		this.leafBlockEncoderSupplier = leafBlockEncoderSupplier;
		this.valueBlockEncoderSupplier = valueBlockEncoderSupplier;

		this.branchBlockCompressor = branchBlockCompressor;
		this.leafBlockCompressor = leafBlockCompressor;
		this.valueBlockCompressor = valueBlockCompressor;
	}

	public Scanner<Integer> columnIds(){
		return Scanner.iterate(0, i -> i + 1)
				.limit(numColumns);
	}

}
