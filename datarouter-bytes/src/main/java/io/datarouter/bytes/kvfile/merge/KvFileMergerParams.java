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
package io.datarouter.bytes.kvfile.merge;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.Blockfile;
import io.datarouter.bytes.blockfile.compress.BlockfileCompressor;
import io.datarouter.bytes.kvfile.kv.KvFileEntry;
import io.datarouter.bytes.kvfile.merge.KvFileMergerParams.Nested.KvFileMergerReadParams;
import io.datarouter.bytes.kvfile.merge.KvFileMergerParams.Nested.KvFileMergerStorageParams;
import io.datarouter.bytes.kvfile.merge.KvFileMergerParams.Nested.KvFileMergerWriteParams;

public record KvFileMergerParams(
		KvFileMergerStorageParams storageParams,
		KvFileMergerReadParams readParams,
		KvFileMergerWriteParams writeParams,
		Duration heartbeatPeriod){

	public static class Nested{

		public record KvFileMergerStorageParams(
				Blockfile<List<KvFileEntry>> blockfile,
				Supplier<String> filenameSupplier){
		}

		public record KvFileMergerReadParams(
				Function<byte[],List<KvFileEntry>> decoder,
				int memoryFanIn,
				int streamingFanIn,
				ExecutorService prefetchExec,
				ByteLength readBufferSize,
				ByteLength readChunkSize,
				ExecutorService readExec,
				int decodeBatchSize,
				ExecutorService decodeExec){
		}

		public record KvFileMergerWriteParams(
				Function<List<KvFileEntry>,byte[]> encoder,
				BlockfileCompressor compressor,
				ByteLength minBlockSize,
				int encodeBatchSize,
				ExecutorService encodeExec,
				ByteLength multipartUploadThreshold,
				int writeThreads,
				ExecutorService writeExec){
		}

	}

}
