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

import java.util.concurrent.ExecutorService;

import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.group.SnapshotGroup;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.path.SnapshotPathsRegistry;
import io.datarouter.filesystem.snapshot.storage.file.SnapshotFileDeleter;
import io.datarouter.filesystem.snapshot.storage.file.SnapshotFileStorage;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;

public class SnapshotGroupDeleteOps{

	private final SnapshotGroup group;

	private final SnapshotPathsRegistry pathsRegistry;
	private final Directory groupDirectory;
	private final Directory idDirectory;
	private final Directory fileDirectory;

	public SnapshotGroupDeleteOps(
			SnapshotGroup group,
			SnapshotPathsRegistry pathsRegistry,
			Directory groupSubpath,
			Directory idSubpath,
			Directory fileSubpath){
		this.group = group;
		this.pathsRegistry = pathsRegistry;
		this.groupDirectory = groupSubpath;
		this.idDirectory = idSubpath;
		this.fileDirectory = fileSubpath;
	}

	public void deleteSnapshot(
			SnapshotKey snapshotKey,
			ExecutorService exec,
			int numThreads){
		deleteIdFile(snapshotKey.snapshotId);
		RootBlock rootBlock = group.root(BlockKey.root(snapshotKey));
		//TODO delete from cache
		SnapshotFileStorage snapshotFileStorage = group.makeSnapshotFileStorage(snapshotKey.snapshotId);
		new SnapshotFileDeleter(rootBlock, pathsRegistry, snapshotKey, snapshotFileStorage, exec, numThreads).delete();
	}

	public void deleteGroup(ExecutorService exec, int numThreads){
		//delete ids first
		idDirectory.scanKeys(Subpath.empty())
				.parallel(new ParallelScannerContext(exec, numThreads, true))
				.forEach(idDirectory::delete);
		//then data
		fileDirectory.scanKeys(Subpath.empty())
				.parallel(new ParallelScannerContext(exec, numThreads, true))
				.forEach(fileDirectory::delete);
		//then leftovers (sub-directories)
		groupDirectory.deleteAll(Subpath.empty());
	}

	private void deleteIdFile(String snapshotId){
		idDirectory.delete(PathbeanKey.of(snapshotId));
	}

}
