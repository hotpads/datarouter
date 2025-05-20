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
package io.datarouter.filesystem.snapshot.group.groupops;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.filesystem.snapshot.entry.SnapshotEntry;
import io.datarouter.filesystem.snapshot.group.SnapshotGroup;
import io.datarouter.filesystem.snapshot.group.dto.SnapshotWriteResult;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.reader.block.DecodingBlockLoader;
import io.datarouter.filesystem.snapshot.reader.block.DecodingBlockLoaderFactory;
import io.datarouter.filesystem.snapshot.storage.block.SnapshotBlockStorage;
import io.datarouter.filesystem.snapshot.storage.file.SnapshotFileStorage;
import io.datarouter.filesystem.snapshot.writer.SnapshotWriter;
import io.datarouter.filesystem.snapshot.writer.SnapshotWriterConfig;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.types.Ulid;

public class SnapshotGroupWriteOps{
	private static final Logger logger = LoggerFactory.getLogger(SnapshotGroupWriteOps.class);

	private final SnapshotGroup group;
	private final String groupId;
	private final DecodingBlockLoaderFactory decodingBlockLoaderFactory;
	private final Directory cacheStorage;
	private final Directory idDirectory;
	//TODO make this a cache with eviction
	private final Map<SnapshotKey,DecodingBlockLoader> decodingBlockLoaderBySnapshotKey;

	public SnapshotGroupWriteOps(
			SnapshotGroup group,
			String groupId,
			DecodingBlockLoaderFactory decodingBlockLoaderFactory,
			Directory cacheDirectory,
			Directory idDirectory,
			Map<SnapshotKey,DecodingBlockLoader> decodingBlockLoaderBySnapshotKey){
		this.group = group;
		this.groupId = groupId;
		this.decodingBlockLoaderFactory = decodingBlockLoaderFactory;
		this.cacheStorage = cacheDirectory;
		this.idDirectory = idDirectory;
		this.decodingBlockLoaderBySnapshotKey = decodingBlockLoaderBySnapshotKey;
	}

	public SnapshotWriteResult write(
			SnapshotWriterConfig config,
			Scanner<List<SnapshotEntry>> entries,
			ExecutorService exec,
			Supplier<Boolean> shouldStop){
		String snapshotId = new Ulid().value();
		return writeWithId(config, entries, snapshotId, exec, shouldStop);
	}

	//could be public if something like back-dating snapshotIds is necessary
	private SnapshotWriteResult writeWithId(
			SnapshotWriterConfig config,
			Scanner<List<SnapshotEntry>> entries,
			String snapshotId,
			ExecutorService exec,
			Supplier<Boolean> shouldStop){
		var snapshotKey = new SnapshotKey(groupId, snapshotId);
		SnapshotFileStorage snapshotFileStorage = group.makeSnapshotFileStorage(snapshotKey.snapshotId());
		SnapshotBlockStorage snapshotBlockStorage = cacheStorage == null
				? null
				: group.makeCacheStorage(snapshotKey.snapshotId());
		try(var writer = new SnapshotWriter(snapshotKey, snapshotFileStorage, snapshotBlockStorage, config, exec)){
			entries.advanceUntil(_ -> shouldStop.get())
					.forEach(writer::addBatch);
			if(shouldStop.get()){
				return SnapshotWriteResult.failure(snapshotKey);
			}
			return writer.complete()
					.map(rootBlock -> {
						writeIdFile(snapshotKey.snapshotId());
						decodingBlockLoaderBySnapshotKey.put(
								snapshotKey,
								decodingBlockLoaderFactory.create(
										rootBlock,
										group.makeStorageReader(snapshotKey.snapshotId())));
						return SnapshotWriteResult.success(snapshotKey, rootBlock);
					})
					.orElseGet(() -> {
						logger.warn("snapshot {} had no entries and was not written", snapshotKey);
						return SnapshotWriteResult.empty(snapshotKey);
					});
		}
	}

	private void writeIdFile(String snapshotId){
		idDirectory.write(PathbeanKey.of(snapshotId), EmptyArray.BYTE);
	}

}
