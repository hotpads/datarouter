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
package io.datarouter.filesystem.snapshot.storage.file;

import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.path.SnapshotPaths;
import io.datarouter.filesystem.snapshot.path.SnapshotPathsRegistry;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.number.NumberFormatter;

public class SnapshotFileDeleter{
	private static final Logger logger = LoggerFactory.getLogger(SnapshotFileDeleter.class);

	private final SnapshotKey snapshotKey;
	private final SnapshotFileStorage snapshotFileStorage;
	private final int numThreads;

	private final RootBlock rootBlock;
	private final SnapshotPaths paths;
	private final ExecutorService exec;

	public SnapshotFileDeleter(
			RootBlock rootBlock,
			SnapshotPathsRegistry pathsRegistry,
			SnapshotKey snapshotKey,
			SnapshotFileStorage snapshotFileStorage,
			ExecutorService exec,
			int numThreads){
		this.rootBlock = rootBlock;
		paths = pathsRegistry.getPaths(rootBlock.pathFormat());
		this.snapshotKey = snapshotKey;
		this.snapshotFileStorage = snapshotFileStorage;
		this.exec = exec;
		this.numThreads = numThreads;
	}

	public void delete(){
		deleteBranchFiles();
		deleteLeafFiles();
		deleteValueFiles();
		snapshotFileStorage.deleteRootFile();
		try{
			snapshotFileStorage.deleteAll();// deletes directories and acts as a catch-all
		}catch(UnsupportedOperationException e){
			logger.warn("", e);
		}
		logger.info("deleted snapshot key={}, bytes={}",
				snapshotKey,
				NumberFormatter.addCommas(rootBlock.totalBytesCompressed()));
	}

	private void deleteBranchFiles(){
		IntStream.range(0, rootBlock.numBranchLevels()).forEach(level -> {
			int lastBlockId = rootBlock.numBranchBlocks(level) - 1;
			int numFiles = rootBlock.branchFileId(level, lastBlockId) + 1;
			Scanner.iterate(0, i -> i + 1)
					.limit(numFiles)
					.map(fileId -> FileKey.branch(level, fileId))
					.parallel(new ParallelScannerContext(exec, numThreads, true))
					.forEach(this::tryDeleteBranchFile);
		});
	}

	private void tryDeleteBranchFile(FileKey fileKey){
		try{
			snapshotFileStorage.deleteBranchFile(paths, fileKey);
		}catch(Exception e){
			logger.warn(e.getMessage());
		}
	}

	private void deleteLeafFiles(){
		int lastBlockId = rootBlock.numLeafBlocks() - 1;
		int numFiles = rootBlock.leafFileId(lastBlockId) + 1;
		Scanner.iterate(0, i -> i + 1)
				.limit(numFiles)
				.map(FileKey::leaf)
				.parallel(new ParallelScannerContext(exec, numThreads, true))
				.forEach(this::tryDeleteLeafFile);
	}

	private void tryDeleteLeafFile(FileKey fileKey){
		try{
			snapshotFileStorage.deleteLeafFile(paths, fileKey);
		}catch(Exception e){
			logger.warn(e.getMessage());
		}
	}

	private void deleteValueFiles(){
		IntStream.range(0, rootBlock.numColumns()).forEach(column -> {
			int lastBlockId = rootBlock.numValueBlocks(column) - 1;
			int numFiles = rootBlock.valueFileId(lastBlockId) + 1;
			Scanner.iterate(0, i -> i + 1)
					.limit(numFiles)
					.map(fileId -> FileKey.value(column, fileId))
					.parallel(new ParallelScannerContext(exec, numThreads, true))
					.forEach(this::tryDeleteValueFile);
		});
	}

	private void tryDeleteValueFile(FileKey fileKey){
		try{
			snapshotFileStorage.deleteValueFile(paths, fileKey);
		}catch(Exception e){
			logger.warn(e.getMessage());
		}
	}

}
