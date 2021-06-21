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
package io.datarouter.storage.node.op.raw.read;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.split.ChunkScannerTool;

/**
 * Methods for reading from an blob store such as the filesystem or S3.
 */
public interface BlobStorageReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends NodeOps<PK,D>{

	String getBucket();
	Subpath getRootPath();

	boolean exists(PathbeanKey key);
	Optional<Long> length(PathbeanKey key);

	byte[] read(PathbeanKey key);
	byte[] read(PathbeanKey key, long offset, int length);

	Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath);
	Scanner<List<Pathbean>> scanPaged(Subpath subpath);


	default Scanner<PathbeanKey> scanKeys(Subpath subpath){
		return scanKeysPaged(subpath)
				.concat(Scanner::of);
	}

	default Scanner<Pathbean> scan(Subpath subpath){
		return scanPaged(subpath)
				.concat(Scanner::of);
	}

	default Scanner<byte[]> scanChunks(
			PathbeanKey key,
			ExecutorService exec,
			int numThreads,
			int chunkSize){
		long totalLength = length(key).orElseThrow();
		return ChunkScannerTool.scanChunks(totalLength, chunkSize)
				.parallel(new ParallelScannerContext(exec, numThreads, false))
				.map(range -> read(key, range.start, range.length));
	}

}
