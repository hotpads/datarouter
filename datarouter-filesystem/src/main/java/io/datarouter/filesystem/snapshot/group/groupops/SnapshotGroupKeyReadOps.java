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
package io.datarouter.filesystem.snapshot.group.groupops;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.group.SnapshotGroup;
import io.datarouter.filesystem.snapshot.group.dto.SnapshotKeyAndRoot;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.key.SnapshotKeyDecoder;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.cached.CachingSupplier;
import io.datarouter.util.collection.ListTool;

public class SnapshotGroupKeyReadOps{

	private final SnapshotGroup group;
	private final SnapshotGroupFileReadOps groupFileReader;
	private final SnapshotKeyDecoder snapshotKeyDecoder;
	private final Supplier<List<SnapshotKey>> cachedKeys;
	private final boolean cacheOk;

	public SnapshotGroupKeyReadOps(
			SnapshotGroup group,
			SnapshotGroupFileReadOps groupFileReader,
			SnapshotKeyDecoder snapshotKeyDecoder,
			boolean cacheOk){
		this.group = group;
		this.groupFileReader = groupFileReader;
		this.snapshotKeyDecoder = snapshotKeyDecoder;
		cachedKeys = new CachingSupplier<>(
				() -> groupFileReader.scanSnapshotKeysFromStorage().list(),
				Duration.ofMinutes(1));
		this.cacheOk = cacheOk;
	}

	public Scanner<SnapshotKey> scanSnapshotKeys(){
		return cacheOk
				? Scanner.of(cachedKeys.get())
				: groupFileReader.scanSnapshotKeysFromStorage();
	}

	public Optional<SnapshotKey> findLastSnapshotKey(){
		return cacheOk
				? ListTool.findLast(cachedKeys.get())
				: groupFileReader.scanSnapshotKeysFromStorage().findLast();
	}

	public Scanner<SnapshotKey> scanSnapshotKeysOlderThan(Duration duration){
		Instant endTime = Instant.now().minus(duration);
		return scanSnapshotKeys()
				.advanceWhile(key -> snapshotKeyDecoder.toInstant(key).compareTo(endTime) < 0);
	}

	public Scanner<SnapshotKeyAndRoot> scanSnapshotKeysAndRootBlocks(ExecutorService exec, int numThreads){
		return scanSnapshotKeys()
				.parallel(new ParallelScannerContext(exec, numThreads, false))
				.map(key -> new SnapshotKeyAndRoot(key, group.root(BlockKey.root(key))));
	}

}
