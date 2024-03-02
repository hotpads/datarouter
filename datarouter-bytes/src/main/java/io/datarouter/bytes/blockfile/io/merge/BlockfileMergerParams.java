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
package io.datarouter.bytes.blockfile.io.merge;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.BlockfileGroup;
import io.datarouter.bytes.blockfile.encoding.compression.BlockfileCompressor;
import io.datarouter.bytes.blockfile.encoding.indexblock.BlockfileIndexBlockFormat;
import io.datarouter.bytes.blockfile.encoding.valueblock.BlockfileValueBlockFormat;
import io.datarouter.bytes.blockfile.row.BlockfileRow;

public record BlockfileMergerParams(
		BlockfileMergerStorageParams storageParams,
		BlockfileMergerReadParams readParams,
		BlockfileMergerWriteParams writeParams,
		Duration heartbeatPeriod){

	public record BlockfileMergerStorageParams(
			BlockfileGroup<BlockfileRow> blockfileGroup,
			Supplier<String> filenameSupplier){
	}

	public record BlockfileMergerReadParams(
			int memoryFanIn,
			int streamingFanIn,
			ExecutorService prefetchExec,
			ByteLength readBufferSize,
			ByteLength readChunkSize,
			ExecutorService readExec,
			int decodeBatchSize,
			ExecutorService decodeExec){
	}

	public record BlockfileMergerWriteParams(
			BlockfileValueBlockFormat valueBlockFormat,
			BlockfileIndexBlockFormat indexBlockFormat,
			BlockfileCompressor compressor,
			ByteLength minBlockSize,
			int encodeBatchSize,
			ExecutorService encodeExec,
			ByteLength multipartUploadThreshold,
			int writeThreads,
			ExecutorService writeExec){
	}

}
