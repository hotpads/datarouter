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

import io.datarouter.filesystem.snapshot.block.branch.BranchBlockV1Encoder;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlockV1Encoder;
import io.datarouter.filesystem.snapshot.block.root.RootBlockV1Encoder;
import io.datarouter.filesystem.snapshot.block.value.ValueBlockV1Encoder;
import io.datarouter.filesystem.snapshot.compress.BlockCompressor;
import io.datarouter.filesystem.snapshot.compress.PassthroughBlockCompressor;
import io.datarouter.filesystem.snapshot.encode.BranchBlockEncoder;
import io.datarouter.filesystem.snapshot.encode.LeafBlockEncoder;
import io.datarouter.filesystem.snapshot.encode.RootBlockEncoder;
import io.datarouter.filesystem.snapshot.encode.ValueBlockEncoder;
import io.datarouter.filesystem.snapshot.path.SnapshotPaths;
import io.datarouter.filesystem.snapshot.path.SnapshotPathsV1;

public class SnapshotWriterConfigBuilder{

	private static final int DEFAULT_BATCH_QUEUE_LENGTH = 100;
	private static final int DEFAULT_NUM_THREADS = Runtime.getRuntime().availableProcessors();
	private static final int DEFAULT_LOG_PERIOD_MS = 1_000;
	private static final boolean DEFAULT_COMPRESSOR_CONCAT_CHUNKS = false;
	private static final boolean DEFAULT_PERSIST = true;
	private static final boolean DEFAULT_UPDATE_CACHE = false;
	private static final int DEFAULT_LEAF_ENCODER_CHUNK_SIZE = 32 * 1024;
	private static final int DEFAULT_BYTES_PER_BLOCK = 32 * 1024;
	private static final int DEFAULT_BYTES_PER_FILE = 64 * 1024 * 1024;
	private static final int DEFAULT_BLOCKS_PER_FILE = 10_000;

	private boolean sorted;
	private final int numColumns;

	private int batchQueueLength;
	private int numThreads;
	private long logPeriodMs;
	private boolean compressorConcatChunks;
	private boolean persist;
	private boolean updateCache;

	private Supplier<SnapshotPaths> pathsSupplier;

	private int leafEncoderChunkSize;

	private int branchBlockSize;
	private int leafBlockSize;
	private int valueBlockSize;

	private int branchBytesPerFile;
	private int leafBytesPerFile;
	private int valueBytesPerFile;

	private int branchBlocksPerFile;
	private int leafBlocksPerFile;
	private int valueBlocksPerFile;

	private Supplier<RootBlockEncoder> rootBlockEncoderSupplier;
	private Function<Integer,BranchBlockEncoder> branchBlockEncoderFactory;
	private Supplier<LeafBlockEncoder> leafBlockEncoderSupplier;
	private Supplier<ValueBlockEncoder> valueBlockEncoderSupplier;


	private BlockCompressor branchBlockCompressor;
	private BlockCompressor leafBlockCompressor;
	private BlockCompressor valueBlockCompressor;

	public SnapshotWriterConfigBuilder(boolean sorted, int numColumns){
		this.sorted = sorted;
		this.numColumns = numColumns;

		batchQueueLength = DEFAULT_BATCH_QUEUE_LENGTH;
		numThreads = DEFAULT_NUM_THREADS;
		logPeriodMs = DEFAULT_LOG_PERIOD_MS;
		compressorConcatChunks = DEFAULT_COMPRESSOR_CONCAT_CHUNKS;
		persist = DEFAULT_PERSIST;
		updateCache = DEFAULT_UPDATE_CACHE;

		pathsSupplier = SnapshotPathsV1::new;

		leafEncoderChunkSize = DEFAULT_LEAF_ENCODER_CHUNK_SIZE;

		branchBlockSize = DEFAULT_BYTES_PER_BLOCK;
		leafBlockSize = DEFAULT_BYTES_PER_BLOCK;
		valueBlockSize = DEFAULT_BYTES_PER_BLOCK;

		branchBytesPerFile = DEFAULT_BYTES_PER_FILE;
		leafBytesPerFile = DEFAULT_BYTES_PER_FILE;
		valueBytesPerFile = DEFAULT_BYTES_PER_FILE;

		branchBlocksPerFile = DEFAULT_BLOCKS_PER_FILE;
		leafBlocksPerFile = DEFAULT_BLOCKS_PER_FILE;
		valueBlocksPerFile = DEFAULT_BLOCKS_PER_FILE;

		rootBlockEncoderSupplier = RootBlockV1Encoder::new;
		branchBlockEncoderFactory = BranchBlockV1Encoder::new;
		leafBlockEncoderSupplier = () -> new LeafBlockV1Encoder(leafEncoderChunkSize);
		valueBlockEncoderSupplier = ValueBlockV1Encoder::new;

		branchBlockCompressor = new PassthroughBlockCompressor();
		leafBlockCompressor = new PassthroughBlockCompressor();
		valueBlockCompressor = new PassthroughBlockCompressor();
	}

	public SnapshotWriterConfigBuilder withBatchQueueLength(int batchQueueLength){
		this.batchQueueLength = batchQueueLength;
		return this;
	}

	public SnapshotWriterConfigBuilder withNumThreads(int numThreads){
		this.numThreads = numThreads;
		return this;
	}

	public SnapshotWriterConfigBuilder withLogPeriodThreads(int logPeriodMs){
		this.logPeriodMs = logPeriodMs;
		return this;
	}

	public SnapshotWriterConfigBuilder withCompressorConcatChunks(boolean compressorConcatChunks){
		this.compressorConcatChunks = compressorConcatChunks;
		return this;
	}

	public SnapshotWriterConfigBuilder withPersist(boolean persist){
		this.persist = persist;
		return this;
	}

	public SnapshotWriterConfigBuilder withUpdateCache(boolean updateCache){
		this.updateCache = updateCache;
		return this;
	}

	public SnapshotWriterConfigBuilder withPathsSupplier(Supplier<SnapshotPaths> pathsSupplier){
		this.pathsSupplier = pathsSupplier;
		return this;
	}

	/*-------------- encoder chunk size ------------*/

	public SnapshotWriterConfigBuilder withLeafEncoderChunkSize(int leafEncoderChunkSize){
		this.leafEncoderChunkSize = leafEncoderChunkSize;
		return this;
	}

	/*-------------- block size -----------------*/

	public SnapshotWriterConfigBuilder withBlockSize(int blockSize){
		this.branchBlockSize = blockSize;
		this.leafBlockSize = blockSize;
		this.valueBlockSize = blockSize;
		return this;
	}

	public SnapshotWriterConfigBuilder withBranchBlockSize(int branchBlockSize){
		this.branchBlockSize = branchBlockSize;
		return this;
	}

	public SnapshotWriterConfigBuilder withLeafBlockSize(int leafBlockSize){
		this.leafBlockSize = leafBlockSize;
		return this;
	}

	public SnapshotWriterConfigBuilder withValueBlockSize(int valueBlockSize){
		this.valueBlockSize = valueBlockSize;
		return this;
	}

	/*-------------- file size --------------------*/

	public SnapshotWriterConfigBuilder withBytesPerFile(int bytesPerFile){
		this.branchBytesPerFile = bytesPerFile;
		this.leafBytesPerFile = bytesPerFile;
		this.valueBytesPerFile = bytesPerFile;
		return this;
	}

	public SnapshotWriterConfigBuilder withBranchBytesPerFile(int branchBytesPerFile){
		this.branchBytesPerFile = branchBytesPerFile;
		return this;
	}

	public SnapshotWriterConfigBuilder withLeafBytesPerFile(int leafBytesPerFile){
		this.leafBytesPerFile = leafBytesPerFile;
		return this;
	}

	public SnapshotWriterConfigBuilder withValueBytesPerFile(int valueBytesPerFile){
		this.valueBytesPerFile = valueBytesPerFile;
		return this;
	}

	/*--------------- blocks per file -------------*/

	public SnapshotWriterConfigBuilder withBlocksPerFile(int blocksPerFile){
		this.branchBlocksPerFile = blocksPerFile;
		this.leafBlocksPerFile = blocksPerFile;
		this.valueBlocksPerFile = blocksPerFile;
		return this;
	}

	public SnapshotWriterConfigBuilder withBranchBlocksPerFile(int branchBlocksPerFile){
		this.branchBlocksPerFile = branchBlocksPerFile;
		return this;
	}

	public SnapshotWriterConfigBuilder withLeafBlocksPerFile(int leafBlocksPerFile){
		this.leafBlocksPerFile = leafBlocksPerFile;
		return this;
	}

	public SnapshotWriterConfigBuilder withValueBlocksPerFile(int valueBlocksPerFile){
		this.valueBlocksPerFile = valueBlocksPerFile;
		return this;
	}

	/*---------------- encoder -----------------*/

	public SnapshotWriterConfigBuilder withRootBlockEncoderFactory(
			Supplier<RootBlockEncoder> rootBlockEncoderSupplier){
		this.rootBlockEncoderSupplier = rootBlockEncoderSupplier;
		return this;
	}

	public SnapshotWriterConfigBuilder withBranchBlockEncoderFactory(
			Function<Integer,BranchBlockEncoder> branchEncoderFactory){
		this.branchBlockEncoderFactory = branchEncoderFactory;
		return this;
	}

	public SnapshotWriterConfigBuilder withLeafBlockEncoderSupplier(
			Supplier<LeafBlockEncoder> leafBlockEncoderSupplier){
		this.leafBlockEncoderSupplier = leafBlockEncoderSupplier;
		return this;
	}

	public SnapshotWriterConfigBuilder withValueBlockEncoderSupplier(
			Supplier<ValueBlockEncoder> valueBlockEncoderSupplier){
		this.valueBlockEncoderSupplier = valueBlockEncoderSupplier;
		return this;
	}

	/*------------------- compressor -------------*/

	public SnapshotWriterConfigBuilder withCompressor(BlockCompressor compressor){
		this.branchBlockCompressor = compressor;
		this.leafBlockCompressor = compressor;
		this.valueBlockCompressor = compressor;
		return this;
	}

	public SnapshotWriterConfigBuilder withBranchBlockCompressor(BlockCompressor branchBlockCompressor){
		this.branchBlockCompressor = branchBlockCompressor;
		return this;
	}

	public SnapshotWriterConfigBuilder withLeafBlockCompressor(BlockCompressor leafBlockCompressor){
		this.leafBlockCompressor = leafBlockCompressor;
		return this;
	}

	public SnapshotWriterConfigBuilder withValueBlockCompressor(BlockCompressor valueBlockCompressor){
		this.valueBlockCompressor = valueBlockCompressor;
		return this;
	}

	/*-------------- build ---------------*/

	public SnapshotWriterConfig build(){
		return new SnapshotWriterConfig(
				sorted,
				numColumns,

				batchQueueLength,
				numThreads,
				logPeriodMs,
				compressorConcatChunks,
				persist,
				updateCache,

				pathsSupplier,

				leafEncoderChunkSize,

				branchBlockSize,
				leafBlockSize,
				valueBlockSize,

				branchBytesPerFile,
				leafBytesPerFile,
				valueBytesPerFile,

				branchBlocksPerFile,
				leafBlocksPerFile,
				valueBlocksPerFile,

				rootBlockEncoderSupplier,
				branchBlockEncoderFactory,
				leafBlockEncoderSupplier,
				valueBlockEncoderSupplier,

				branchBlockCompressor,
				leafBlockCompressor,
				valueBlockCompressor);
	}

}
