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
package io.datarouter.filesystem.snapshot.writer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.filesystem.snapshot.compress.CompressedBlock;
import io.datarouter.filesystem.snapshot.path.SnapshotPaths;
import io.datarouter.filesystem.snapshot.storage.file.FileKey;
import io.datarouter.filesystem.snapshot.storage.file.SnapshotFileStorage;
import io.datarouter.filesystem.snapshot.writer.BlockQueue.FileIdsAndEndings;
import io.datarouter.filesystem.snapshot.writer.BlockQueue.SnapshotFile;
import io.datarouter.util.number.NumberFormatter;

public class SnapshotFileWriter{
	private static final Logger logger = LoggerFactory.getLogger(SnapshotFileWriter.class);

	private static final boolean LOG_FLUSH_START = false;
	private static final boolean LOG_QUEUE_STATS = true;

	private final SnapshotWriterTracker tracker;
	private final SnapshotFileStorage fileStorage;
	private final SnapshotWriterConfig config;
	private final SnapshotPaths paths;

	private final Consumer<Void> onValueFileWriteCallback;
	private final Consumer<Void> onLeafFileWriteCallback;
	private final Consumer<Integer> onBranchFileWriteCallback;

	public final Map<Integer,BlockQueue> branchBlockQueueByLevel;
	public final BlockQueue leafBlockQueue;
	public final List<BlockQueue> valueBlockQueueByColumn;

	public SnapshotFileWriter(
			SnapshotWriterTracker tracker,
			SnapshotFileStorage snapshotFileStorage,
			SnapshotWriterConfig config,
			Consumer<Void> onValueFileWriteCallback,
			Consumer<Void> onLeafFileWriteCallback,
			Consumer<Integer> onBranchFileWriteCallback){
		this.tracker = tracker;
		this.fileStorage = snapshotFileStorage;
		this.config = config;
		paths = config.pathsSupplier.get();

		this.onValueFileWriteCallback = onValueFileWriteCallback;
		this.onLeafFileWriteCallback = onLeafFileWriteCallback;
		this.onBranchFileWriteCallback = onBranchFileWriteCallback;

		branchBlockQueueByLevel = new ConcurrentHashMap<>();
		leafBlockQueue = new BlockQueue("leaf", config.leafBytesPerFile, config.leafBlocksPerFile);
		valueBlockQueueByColumn = config.columnIds()
				.map(columnId -> new BlockQueue(
						"branch-" + columnId,
						config.valueBytesPerFile,
						config.valueBlocksPerFile))
				.list();
	}

	/*----------------- branch ----------------------*/

	public void addBranchBlock(int level, int blockId, CompressedBlock block){
		tracker.branchMemory(true, 1, block.totalLength);
		BlockQueue queue = branchBlockQueueByLevel.computeIfAbsent(level, $ -> new BlockQueue(
				"branch-" + level,
				config.branchBytesPerFile,
				config.branchBlocksPerFile));
		queue.submit(blockId, block).forEach(file -> writeBranchFile(level, file));
	}

	private void writeBranchFile(int level, SnapshotFile file){
		if(config.persist){
			logWriteStart(file);
			long startMs = System.currentTimeMillis();
			fileStorage.addBranchFile(paths, FileKey.branch(level, file.id), file.compressedBlocks);
			long ms = System.currentTimeMillis() - startMs;
			logWriteEnd(file, ms);
		}
		onBranchFileWriteCallback.accept(level);
		tracker.branchMemory(false, file.compressedBlocks.count, file.compressedBlocks.totalLength);
	}

	public void completeBranches(int level){
		BlockQueue queue = branchBlockQueueByLevel.get(level);
		queue.takeLastFiles().forEach(file -> writeBranchFile(level, file));
		queue.assertEmpty();
	}

	/*----------------- leaf ----------------------*/

	public void addLeafBlock(int blockId, CompressedBlock block){
		tracker.leafMemory(true, 1, block.totalLength);
		leafBlockQueue.submit(blockId, block).forEach(this::writeLeafFile);
	}

	private void writeLeafFile(SnapshotFile file){
		if(config.persist){
			logWriteStart(file);
			long startMs = System.currentTimeMillis();
			fileStorage.addLeafFile(paths, FileKey.leaf(file.id), file.compressedBlocks);
			long ms = System.currentTimeMillis() - startMs;
			logWriteEnd(file, ms);
		}
		onLeafFileWriteCallback.accept(null);
		tracker.leafMemory(false, file.compressedBlocks.count, file.compressedBlocks.totalLength);
	}

	public void completeLeaves(){
		leafBlockQueue.takeLastFiles().forEach(this::writeLeafFile);
		leafBlockQueue.assertEmpty();
	}

	/*----------------- value ----------------------*/

	public void addValueBlock(int column, int blockId, CompressedBlock block){
		tracker.valueMemory(true, 1, block.totalLength);
		valueBlockQueueByColumn.get(column).submit(blockId, block).forEach(file -> writeValueFile(column, file));
	}

	private void writeValueFile(int column, SnapshotFile file){
		if(config.persist){
			logWriteStart(file);
			long startMs = System.currentTimeMillis();
			fileStorage.addValueFile(paths, FileKey.value(column, file.id), file.compressedBlocks);
			long ms = System.currentTimeMillis() - startMs;
			logWriteEnd(file, ms);
		}
		onValueFileWriteCallback.accept(null);
		tracker.valueMemory(false, file.compressedBlocks.count, file.compressedBlocks.totalLength);
	}

	public void completeValues(){
		config.columnIds().forEach(column -> {
			BlockQueue queue = valueBlockQueueByColumn.get(column);
			queue.takeLastFiles().forEach(file -> writeValueFile(column, file));
			queue.assertEmpty();
		});
	}

	/*----------------- complete ----------------------*/

	private void logWriteStart(SnapshotFile file){
		if(LOG_FLUSH_START){
			logger.warn("writing SnapshotFile {}", file.getFlushLog());
		}
	}

	private void logWriteEnd(SnapshotFile file, long ms){
		double megabytes = file.compressedBlocks.totalLength / 1024d / 1024d;
		double seconds = ms / 1000d;
		double megabytesPerSecond = megabytes / seconds;
		logger.info("wrote BlockFile {}, {} ms, {} MBps",
				file.getFlushLog(),
				ms,
				NumberFormatter.format(megabytesPerSecond, 3));
	}

	public void logQueueStats(){
		if(!LOG_QUEUE_STATS){
			return;
		}
		IntStream.range(0, valueBlockQueueByColumn.size()).forEach(column -> logger.info(
				"column={}, valueSingleEndingChecks={}, valueMultiEndingChecks={}",
				column,
				valueBlockQueueByColumn.get(column).numSingleEndingChecks,
				valueBlockQueueByColumn.get(column).numMultiEndingChecks));
		logger.info("leafSingleEndingChecks={}, leafMultiEndingChecks={}",
				leafBlockQueue.numSingleEndingChecks,
				leafBlockQueue.numMultiEndingChecks);
		IntStream.range(0, branchBlockQueueByLevel.size()).forEach(level -> logger.info(
				"level={}, branchSingleEndingChecks={}, branchMultiEndingChecks={}",
				level,
				branchBlockQueueByLevel.get(level).numSingleEndingChecks,
				branchBlockQueueByLevel.get(level).numMultiEndingChecks));
	}

	/*----------------- value fileIds and endings ----------------------*/

	public boolean valueFileInfoReady(int column, int firstBlockId, int numBlocks){
		return valueBlockQueueByColumn.get(column).isReady(firstBlockId, numBlocks);
	}

	public FileIdsAndEndings valueFileInfo(int column, int firstBlockId, int numBlocks){
		return valueBlockQueueByColumn.get(column).fileIdsAndEndings(firstBlockId, numBlocks);
	}

	/*----------------- leaf fileIds and endings ----------------------*/

	public boolean leafFileInfoReady(int firstBlockId, int numBlocks){
		return leafBlockQueue.isReady(firstBlockId, numBlocks);
	}

	public FileIdsAndEndings leafFileInfo(int firstBlockId, int numBlocks){
		return leafBlockQueue.fileIdsAndEndings(firstBlockId, numBlocks);
	}

	/*----------------- branch fileIds and endings ----------------------*/

	public boolean branchFileInfoReady(int level, int firstBlockId, int numBlocks){
		return branchBlockQueueByLevel.get(level).isReady(firstBlockId, numBlocks);
	}

	public FileIdsAndEndings branchFileInfo(int level, int firstBlockId, int numBlocks){
		return branchBlockQueueByLevel.get(level).fileIdsAndEndings(firstBlockId, numBlocks);
	}

	public int rootBranchEnding(){
		int maxLevel = branchBlockQueueByLevel.size() - 1;
		return branchBlockQueueByLevel.get(maxLevel).ending(0);
	}

}
