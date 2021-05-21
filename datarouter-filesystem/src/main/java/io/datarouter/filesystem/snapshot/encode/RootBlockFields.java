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
package io.datarouter.filesystem.snapshot.encode;

import io.datarouter.filesystem.snapshot.path.SnapshotPaths;

public class RootBlockFields{

	public final boolean sorted;
	public final SnapshotPaths paths;
	public final RootBlockEncoderFormats formats;
	public final RootBlockEncoderCompressors compressors;
	public final RootBlockEncoderBytesPerFile bytesPerFile;
	public final RootBlockEncoderBlocksPerFile blocksPerFile;
	public final long numEntries;
	public final int numBranchLevels;
	public final RootBlockEncoderBlockCounts blockCounts;
	public final RootBlockEncoderByteCountsEncoded byteCountsEncoded;
	public final RootBlockEncoderByteCountsCompressed byteCountsCompressed;
	public final RootBlockEncoderBlockEndings blockEndings;
	public final RootBlockEncoderTimings timings;

	public RootBlockFields(
			boolean sorted,
			SnapshotPaths snapshotPaths,
			RootBlockEncoderFormats formats,
			RootBlockEncoderCompressors compressors,
			RootBlockEncoderBytesPerFile bytesPerFile,
			RootBlockEncoderBlocksPerFile blocksPerFile,
			long numEntries,
			int numBranchLevels,
			RootBlockEncoderBlockCounts blockCounts,
			RootBlockEncoderByteCountsEncoded byteCountsEncoded,
			RootBlockEncoderByteCountsCompressed byteCountsCompressed,
			RootBlockEncoderBlockEndings blockEndings,
			RootBlockEncoderTimings timings){
		this.sorted = sorted;
		this.paths = snapshotPaths;
		this.formats = formats;
		this.compressors = compressors;
		this.bytesPerFile = bytesPerFile;
		this.blocksPerFile = blocksPerFile;
		this.numEntries = numEntries;
		this.numBranchLevels = numBranchLevels;
		this.blockCounts = blockCounts;
		this.byteCountsEncoded = byteCountsEncoded;
		this.byteCountsCompressed = byteCountsCompressed;
		this.blockEndings = blockEndings;
		this.timings = timings;
	}

	public static class RootBlockEncoderFormats{

		public final String branchBlockFormat;
		public final String leafBlockFormat;
		public final String valueBlockFormat;

		public RootBlockEncoderFormats(
				String branchBlockFormat,
				String leafBlockFormat,
				String valueBlockFormat){
			this.branchBlockFormat = branchBlockFormat;
			this.leafBlockFormat = leafBlockFormat;
			this.valueBlockFormat = valueBlockFormat;
		}

	}

	public static class RootBlockEncoderCompressors{

		public final String branchBlockCompressor;
		public final String leafBlockCompressor;
		public final String valueBlockCompressor;

		public RootBlockEncoderCompressors(
				String branchBlockCompressor,
				String leafBlockCompressor,
				String valueBlockCompressor){
			this.branchBlockCompressor = branchBlockCompressor;
			this.leafBlockCompressor = leafBlockCompressor;
			this.valueBlockCompressor = valueBlockCompressor;
		}

	}

	public static class RootBlockEncoderBytesPerFile{

		public final int branchBytesPerFile;
		public final int leafBytesPerFile;
		public final int valueBytesPerFile;

		public RootBlockEncoderBytesPerFile(
				int branchBytesPerFile,
				int leafBytesPerFile,
				int valueBytesPerFile){
			this.branchBytesPerFile = branchBytesPerFile;
			this.leafBytesPerFile = leafBytesPerFile;
			this.valueBytesPerFile = valueBytesPerFile;
		}

	}

	public static class RootBlockEncoderBlocksPerFile{

		public final int branchBlocksPerFile;
		public final int leafBlocksPerFile;
		public final int valueBlocksPerFile;

		public RootBlockEncoderBlocksPerFile(
				int branchBlocksPerFile,
				int leafBlocksPerFile,
				int valueBlocksPerFile){
			this.branchBlocksPerFile = branchBlocksPerFile;
			this.leafBlocksPerFile = leafBlocksPerFile;
			this.valueBlocksPerFile = valueBlocksPerFile;
		}

	}

	public static class RootBlockEncoderBlockCounts{

		public final int[] numBranchBlocksByLevel;
		public final int numLeafBlocks;
		public final int[] numValueBlocksByColumn;

		public RootBlockEncoderBlockCounts(
				int[] numBranchBlocksByLevel,
				int numLeafBlocks,
				int[] numValueBlocksByColumn){
			this.numBranchBlocksByLevel = numBranchBlocksByLevel;
			this.numLeafBlocks = numLeafBlocks;
			this.numValueBlocksByColumn = numValueBlocksByColumn;
		}

	}

	public static class RootBlockEncoderByteCountsEncoded{

		public final long numBranchBytesEncoded;
		public final long numLeafBytesEncoded;
		public final long numValueBytesEncoded;

		public RootBlockEncoderByteCountsEncoded(
				long numBranchBytesEncoded,
				long numLeafBytesEncoded,
				long numValueBytesEncoded){
			this.numBranchBytesEncoded = numBranchBytesEncoded;
			this.numLeafBytesEncoded = numLeafBytesEncoded;
			this.numValueBytesEncoded = numValueBytesEncoded;
		}

	}

	public static class RootBlockEncoderByteCountsCompressed{

		public final long numBranchBytesCompressed;
		public final long numLeafBytesCompressed;
		public final long numValueBytesCompressed;

		public RootBlockEncoderByteCountsCompressed(
				long numBranchBytesCompressed,
				long numLeafBytesCompressed,
				long numValueBytesCompressed){
			this.numBranchBytesCompressed = numBranchBytesCompressed;
			this.numLeafBytesCompressed = numLeafBytesCompressed;
			this.numValueBytesCompressed = numValueBytesCompressed;
		}

	}

	public static class RootBlockEncoderBlockEndings{

		public final int rootBranchBlockLength;

		public RootBlockEncoderBlockEndings(
				int rootBranchBlockLength){
			this.rootBranchBlockLength = rootBranchBlockLength;
		}

	}

	public static class RootBlockEncoderTimings{

		public final long writeStartTimeMs;
		public final long writeDurationMs;

		public RootBlockEncoderTimings(long writeStartTimeMs, long writeDurationMs){
			this.writeStartTimeMs = writeStartTimeMs;
			this.writeDurationMs = Math.max(writeDurationMs, 1);
		}

	}

}
