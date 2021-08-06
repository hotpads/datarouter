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

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.filesystem.snapshot.group.SnapshotGroup;
import io.datarouter.filesystem.snapshot.group.vacuum.SnapshotVacuumConfig;
import io.datarouter.filesystem.snapshot.group.vacuum.SnapshotVacuumPlanner;
import io.datarouter.filesystem.snapshot.group.vacuum.SnapshotVacuumPlanner.SnapshotVacuumPlan;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.key.SnapshotKeyDecoder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.file.PathbeanKey;

public class SnapshotGroupVacuumOps{
	private static final Logger logger = LoggerFactory.getLogger(SnapshotGroupVacuumOps.class);

	private final SnapshotGroup group;
	private final String groupId;
	private final Directory fileDirectory;
	private final SnapshotKeyDecoder snapshotKeyDecoder;
	private final SnapshotVacuumConfig vacuumConfig;

	private final SnapshotGroupFileReadOps groupFileReader;
	private final SnapshotGroupKeyReadOps groupReader;

	public SnapshotGroupVacuumOps(
			SnapshotGroup group,
			String groupId,
			Directory fileDirectory,
			SnapshotKeyDecoder snapshotKeyDecoder,
			SnapshotVacuumConfig vacuumConfig,
			SnapshotGroupFileReadOps groupFileReader,
			SnapshotGroupKeyReadOps groupReader){
		this.group = group;
		this.groupId = groupId;
		this.fileDirectory = fileDirectory;
		this.snapshotKeyDecoder = snapshotKeyDecoder;
		this.vacuumConfig = vacuumConfig;
		this.groupFileReader = groupFileReader;
		this.groupReader = groupReader;
	}

	public void vacuum(ExecutorService exec, int numThreads){
		List<SnapshotKey> keys = groupReader.scanSnapshotKeys().list();
		SnapshotVacuumPlan plan = new SnapshotVacuumPlanner(vacuumConfig, snapshotKeyDecoder, keys).plan();
		logger.warn("Starting vacuum id={}, group={}, {}/{} snapshots",
				plan.id,
				groupId,
				plan.items.size(),
				keys.size());
		Scanner.of(plan.items)
				.each(result -> logger.warn("vacuum id={}, group={} deleting {} because {}",
						plan.id,
						groupId,
						result.snapshotKey.snapshotId,
						result.reason))
				.forEach(result -> group.deleteOps().deleteSnapshot(result.snapshotKey, exec, numThreads));
		logger.warn("Finished vacuum id={}, group={}, {}/{} snapshots",
				plan.id,
				groupId,
				plan.items.size(),
				keys.size());
	}

	public void vacuumOrphanedFilesOlderThan(Duration duration){
		Set<String> cachedIds = groupReader.scanSnapshotKeys()
				.map(key -> key.snapshotId)
				.collect(HashSet::new);
		groupFileReader.scanSnapshotFilesFromStorage()
				.advanceWhile(pathbeanKey -> {
					SnapshotKey snapshotKey = getSnapshotKeyForFile(pathbeanKey);
					return snapshotKeyDecoder.isOlderThan(snapshotKey, duration);
				})
				.exclude(pathbeanKey -> cachedIds.contains(getSnapshotKeyForFile(pathbeanKey).snapshotId))
				.forEach(fileDirectory::delete);
	}

	private SnapshotKey getSnapshotKeyForFile(PathbeanKey pathbeanKey){
		return new SnapshotKey(groupId, pathbeanKey.getPathSegments().get(0));
	}

}
