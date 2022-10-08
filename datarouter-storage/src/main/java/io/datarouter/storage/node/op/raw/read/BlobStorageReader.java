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
package io.datarouter.storage.node.op.raw.read;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import io.datarouter.bytes.split.ChunkScannerTool;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.storage.util.Subpath;

/**
 * Methods for reading from an blob store such as the filesystem or S3.
 */
public interface BlobStorageReader
extends NodeOps<PathbeanKey,Pathbean>{

	String getBucket();
	Subpath getRootPath();

	boolean exists(PathbeanKey key, Config config);

	default boolean exists(PathbeanKey key){
		return exists(key, new Config());
	}

	Optional<Long> length(PathbeanKey key, Config config);

	default Optional<Long> length(PathbeanKey key){
		return length(key, new Config());
	}

	byte[] read(PathbeanKey key, Config config);

	default byte[] read(PathbeanKey key){
		return read(key, new Config());
	}

	byte[] read(PathbeanKey key, long offset, int length, Config config);

	default byte[] read(PathbeanKey key, long offset, int length){
		return read(key, offset, length, new Config());
	}

	Map<PathbeanKey,byte[]> read(List<PathbeanKey> keys, Config config);

	default Map<PathbeanKey,byte[]> read(List<PathbeanKey> keys){
		return read(keys, new Config());
	}

	Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath, Config config);

	default Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath){
		return scanKeysPaged(subpath, new Config());
	}

	Scanner<List<Pathbean>> scanPaged(Subpath subpath, Config config);

	default Scanner<List<Pathbean>> scanPaged(Subpath subpath){
		return scanPaged(subpath, new Config());
	}

	default Scanner<PathbeanKey> scanKeys(Subpath subpath, Config config){
		return scanKeysPaged(subpath, config)
				.concat(Scanner::of);
	}

	default Scanner<PathbeanKey> scanKeys(Subpath subpath){
		return scanKeys(subpath, new Config());
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
