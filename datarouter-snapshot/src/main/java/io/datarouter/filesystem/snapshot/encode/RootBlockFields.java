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
package io.datarouter.filesystem.snapshot.encode;

import io.datarouter.filesystem.snapshot.encode.RootBlockFields.NestedRecords.RootBlockEncoderBlockCounts;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.NestedRecords.RootBlockEncoderBlockEndings;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.NestedRecords.RootBlockEncoderBlocksPerFile;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.NestedRecords.RootBlockEncoderByteCountsCompressed;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.NestedRecords.RootBlockEncoderByteCountsEncoded;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.NestedRecords.RootBlockEncoderBytesPerFile;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.NestedRecords.RootBlockEncoderCompressors;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.NestedRecords.RootBlockEncoderFormats;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.NestedRecords.RootBlockEncoderTimings;
import io.datarouter.filesystem.snapshot.path.SnapshotPaths;
import io.datarouter.util.todo.NestedRecordImportWorkaround;

public record RootBlockFields(
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

	@NestedRecordImportWorkaround
	public static class NestedRecords{

		public record RootBlockEncoderFormats(
				String branchBlockFormat,
				String leafBlockFormat,
				String valueBlockFormat){
		}

		public record RootBlockEncoderCompressors(
				String branchBlockCompressor,
				String leafBlockCompressor,
				String valueBlockCompressor){
		}

		public record RootBlockEncoderBytesPerFile(
				int branchBytesPerFile,
				int leafBytesPerFile,
				int valueBytesPerFile){
		}

		public record RootBlockEncoderBlocksPerFile(
				int branchBlocksPerFile,
				int leafBlocksPerFile,
				int valueBlocksPerFile){
		}

		public record RootBlockEncoderBlockCounts(
				int[] numBranchBlocksByLevel,
				int numLeafBlocks,
				int[] numValueBlocksByColumn){
		}

		public record RootBlockEncoderByteCountsEncoded(
				long numBranchBytesEncoded,
				long numLeafBytesEncoded,
				long numValueBytesEncoded){
		}

		public record RootBlockEncoderByteCountsCompressed(
				long numBranchBytesCompressed,
				long numLeafBytesCompressed,
				long numValueBytesCompressed){
		}

		public record RootBlockEncoderBlockEndings(
				int rootBranchBlockLength){
		}

		public record RootBlockEncoderTimings(
				long writeStartTimeMs,
				long writeDurationMs){
		}

	}

}
