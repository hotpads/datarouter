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
package io.datarouter.filesystem.snapshot.writer;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.block.root.RootBlockV1;
import io.datarouter.filesystem.snapshot.compress.CompressedBlock;
import io.datarouter.filesystem.snapshot.encode.BranchBlockEncoder;
import io.datarouter.filesystem.snapshot.encode.EncodedBlock;
import io.datarouter.filesystem.snapshot.encode.LeafBlockEncoder;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.RootBlockEncoderBlockCounts;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.RootBlockEncoderBlockEndings;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.RootBlockEncoderBlocksPerFile;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.RootBlockEncoderByteCountsCompressed;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.RootBlockEncoderByteCountsEncoded;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.RootBlockEncoderBytesPerFile;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.RootBlockEncoderCompressors;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.RootBlockEncoderFormats;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.RootBlockEncoderTimings;
import io.datarouter.filesystem.snapshot.encode.ValueBlockEncoder;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.path.SnapshotPaths;
import io.datarouter.filesystem.snapshot.storage.block.CacheBlockKey;
import io.datarouter.filesystem.snapshot.storage.block.SnapshotBlockStorage;
import io.datarouter.filesystem.snapshot.storage.file.SnapshotFileStorage;
import io.datarouter.filesystem.snapshot.writer.BlockQueue.FileIdsAndEndings;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.Count.Counts;
import io.datarouter.util.Require;
import io.datarouter.util.concurrent.FutureTool;
import io.datarouter.util.concurrent.ThreadTool;

public class SnapshotBlockWriter{
	private static final Logger logger = LoggerFactory.getLogger(SnapshotBlockWriter.class);

	private final SnapshotKey snapshotKey;
	private final SnapshotWriterTracker tracker;
	private final SnapshotBlockStorage blockStorage;
	private final SnapshotFileStorage fileStorage;
	private final SnapshotWriterConfig config;
	public final SnapshotFileWriter fileWriter;
	private final ExecutorService exec;
	private final SnapshotPaths paths;

	private final long maxTasks;
	private final int stallMs;

	private final Queue<LeafBlockEncoder> pendingLeafEncoders;
	private final Map<Integer,Queue<BranchBlockEncoder>> pendingBranchEncodersByLevel;

	private final Map<Integer,Map<Integer,Future<?>>> branchFutureByBlockIdByLevel;
	private final Map<Integer,Future<?>> leafFutureByBlockId;
	private final Map<Integer,Map<Integer,Future<?>>> valueFutureByBlockIdByColumn;

	public SnapshotBlockWriter(
			SnapshotKey snapshotKey,
			SnapshotWriterTracker tracker,
			SnapshotBlockStorage blockStorage,
			SnapshotFileStorage fileStorage,
			SnapshotWriterConfig config,
			ExecutorService exec){
		this.snapshotKey = snapshotKey;
		this.tracker = tracker;
		this.blockStorage = blockStorage;
		this.fileStorage = fileStorage;
		this.config = config;
		this.fileWriter = new SnapshotFileWriter(
				tracker,
				fileStorage,
				config,
				this::onValueFileWrite,
				this::onLeafFileWrite,
				this::onBranchFileWrite);
		this.exec = exec;
		paths = config.pathsSupplier().get();

		maxTasks = config.numThreads() * 100;
		stallMs = 1;

		pendingLeafEncoders = new LinkedBlockingQueue<>();
		pendingBranchEncodersByLevel = new ConcurrentHashMap<>();

		branchFutureByBlockIdByLevel = new ConcurrentHashMap<>();
		leafFutureByBlockId = new ConcurrentHashMap<>();
		valueFutureByBlockIdByColumn = new ConcurrentHashMap<>();
	}

	/*------------------- root ---------------------*/

	public RootBlock flushRootBlock(
			long writeStartTimeMs,
			List<Integer> numBranchBlocksByLevel,
			List<Integer> numValueBlocksByColumn,
			int numBranchLevels,
			long numKeys,
			int numLeafBlocks){
		int[] numBranchBlocksByLevelArray = numBranchBlocksByLevel.stream()
				.mapToInt(Integer::intValue)
				.toArray();
		int[] numValueBlocksByColumnArray = numValueBlocksByColumn.stream()
				.mapToInt(Integer::intValue)
				.toArray();
		long writeDurationMs = System.currentTimeMillis() - writeStartTimeMs;
		var encoder = config.rootBlockEncoderSupplier().get();
		var rootBlockFields = new RootBlockFields(
				config.sorted(),
				config.pathsSupplier().get(),
				new RootBlockEncoderFormats(
						config.branchBlockEncoderFactory().apply(0).format(),
						config.leafBlockEncoderSupplier().get().format(),
						config.valueBlockEncoderSupplier().get().format()),
				new RootBlockEncoderCompressors(
						config.branchBlockCompressor().name(),
						config.leafBlockCompressor().name(),
						config.valueBlockCompressor().name()),
				new RootBlockEncoderBytesPerFile(
						config.branchBytesPerFile(),
						config.leafBytesPerFile(),
						config.valueBytesPerFile()),
				new RootBlockEncoderBlocksPerFile(
						config.branchBlocksPerFile(),
						config.leafBlocksPerFile(),
						config.valueBlocksPerFile()),
				numKeys,
				numBranchLevels,
				new RootBlockEncoderBlockCounts(
						numBranchBlocksByLevelArray,
						numLeafBlocks,
						numValueBlocksByColumnArray),
				new RootBlockEncoderByteCountsEncoded(
						tracker.branchBytesEncoded.value(),
						tracker.leafBytesEncoded.value(),
						tracker.valueBytesEncoded.value()),
				new RootBlockEncoderByteCountsCompressed(
						tracker.branchBytesCompressed.value(),
						tracker.leafBytesCompressed.value(),
						tracker.valueBytesCompressed.value()),
				new RootBlockEncoderBlockEndings(
						fileWriter.rootBranchEnding()),
				new RootBlockEncoderTimings(
						writeStartTimeMs,
						writeDurationMs));
		encoder.set(rootBlockFields);

		EncodedBlock encodedRootBlock = encoder.encode();
		if(config.persist()){
			fileStorage.addRootFile(encodedRootBlock);
		}
		return new RootBlockV1(encodedRootBlock.concat());
	}

	/*------------------- branch ---------------------*/

	public void submitBranch(BranchBlockEncoder encoder){
		pendingBranchEncodersByLevel
				.computeIfAbsent(encoder.level(), _ -> new LinkedBlockingQueue<>())
				.add(encoder);
	}

	private boolean isBranchFlushable(BranchBlockEncoder encoder){
		boolean includePreviousChildBlockId = encoder.firstChildBlockId() > 0;
		int firstBlockId = includePreviousChildBlockId ? encoder.firstChildBlockId() - 1 : encoder.firstChildBlockId();
		int numBlockIds = includePreviousChildBlockId ? 1 + encoder.numRecords() : encoder.numRecords();
		if(encoder.level() == 0){
			return fileWriter.leafFileInfoReady(firstBlockId, numBlockIds);
		}else{
			int childLevel = encoder.level() - 1;
			return fileWriter.branchFileInfoReady(childLevel, firstBlockId, numBlockIds);
		}
	}

	private void flushBranch(BranchBlockEncoder encoder){
		tracker.branchTasks.increment();
		Map<Integer,Future<?>> futureByBlockId = branchFutureByBlockIdByLevel.computeIfAbsent(
				encoder.level(),
				ConcurrentHashMap::new);
		Future<?> future = exec.submit(() -> {
			int firstChildBlockId = encoder.firstChildBlockId();
			int numRecords = encoder.numRecords();
			int numEndings = numRecords + 1;//includes final endings from previous block
			FileIdsAndEndings fileIdsAndEndings;
			if(encoder.level() == 0){
				fileIdsAndEndings = fileWriter.leafFileInfo(
						firstChildBlockId - 1,// -1 to get the previous block's info
						numEndings);
			}else{
				int childLevel = encoder.level() - 1;
				fileIdsAndEndings = fileWriter.branchFileInfo(
						childLevel,
						firstChildBlockId - 1,// -1 to get the previous block's info
						numEndings);
			}
			EncodedBlock encodedPages = encoder.encode(fileIdsAndEndings);
			CompressedBlock compressedBlock = config.branchBlockCompressor().compress(
					encodedPages,
					config.compressorConcatChunks());
			tracker.branchBlock(encodedPages, compressedBlock);
			fileWriter.addBranchBlock(encoder.level(), encoder.blockId(), compressedBlock);
			if(blockStorage != null && config.updateCache()){
				CacheBlockKey cacheBlockKey = CacheBlockKey.branch(snapshotKey, encoder.level(), encoder.blockId());
				blockStorage.addBranchBlock(paths, cacheBlockKey, compressedBlock);
			}
			futureByBlockId.remove(encoder.blockId());
			tracker.branchTasks.decrement();
		});
		futureByBlockId.put(encoder.blockId(), future);
	}

	/*------------------- leaf ---------------------*/

	public void submitLeaf(LeafBlockEncoder encoder){
		if(config.numColumns() == 0){
			flushLeaf(encoder);
		}else{
			pendingLeafEncoders.add(encoder);
		}
	}

	private boolean isLeafFlushable(LeafBlockEncoder encoder){
		for(int column = 0; column < config.numColumns(); ++column){
			if(!fileWriter.valueFileInfoReady(
					column,
					encoder.firstValueBlockId(column),
					encoder.numValueBlocks(column))){
				return false;
			}
		}
		return true;
	}

	private void flushLeaf(LeafBlockEncoder encoder){
		leafBackpressure();
		tracker.leafTasks.increment();
		Future<?> future = exec.submit(() -> {
			if(config.sorted()){
				encoder.assertKeysSorted();// primary writer thread only validates sorting between key blocks
			}
			var fileIdsAndEndings = new FileIdsAndEndings[config.numColumns()];
			for(int column = 0; column < config.numColumns(); ++column){
				int firstValueBlockId = encoder.firstValueBlockId(column);
				int numValueBlocks = encoder.numValueBlocks(column);
				int numEndings = numValueBlocks + 1;// +1 for previous block final endings
				fileIdsAndEndings[column] = fileWriter.valueFileInfo(
						column,
						firstValueBlockId - 1,// -1 to get the previous block's info
						numEndings);
			}
			EncodedBlock encodedPages = encoder.encode(fileIdsAndEndings);
			CompressedBlock compressedBytes = config.leafBlockCompressor().compress(
					encodedPages,
					config.compressorConcatChunks());
			tracker.leafBlock(encodedPages, compressedBytes);
			fileWriter.addLeafBlock(encoder.blockId(), compressedBytes);
			if(blockStorage != null && config.updateCache()){
				CacheBlockKey cacheBlockKey = CacheBlockKey.leaf(snapshotKey, encoder.blockId());
				blockStorage.addLeafBlock(paths, cacheBlockKey, compressedBytes);
			}
			leafFutureByBlockId.remove(encoder.blockId());
			tracker.leafTasks.decrement();
		});
		leafFutureByBlockId.put(encoder.blockId(), future);
	}

	private void leafBackpressure(){
		if(config.numColumns() > 0){
			// don't block value files from flushing their pending leaves.  the value backpressure should limit leaves
			return;
		}
		long beforeNs = System.nanoTime();
		while(tracker.leafTasks.value() >= maxTasks){
			ThreadTool.trySleep(stallMs);
		}
		long ns = System.nanoTime() - beforeNs;
		tracker.leafStallNs.incrementBy(ns);
	}

	/*------------------- value ---------------------*/

	public void submitValueBlock(int column, int blockId, ValueBlockEncoder encoder){
		flushValueBlock(column, blockId, encoder);
	}

	private void flushValueBlock(int column, int blockId, ValueBlockEncoder encoder){
		valueBackpressure();
		tracker.valueTasks.increment();
		Map<Integer,Future<?>> futureByBlockId = valueFutureByBlockIdByColumn.computeIfAbsent(
				column,
				ConcurrentHashMap::new);
		Future<?> future = exec.submit(() -> {
			EncodedBlock encodedPages = encoder.encode();
			CompressedBlock compressedBytes = config.valueBlockCompressor().compress(
					encodedPages,
					config.compressorConcatChunks());
			tracker.valueBlock(encodedPages, compressedBytes);
			fileWriter.addValueBlock(column, blockId, compressedBytes);
			if(blockStorage != null && config.updateCache()){
				CacheBlockKey cacheBlockKey = CacheBlockKey.value(snapshotKey, column, blockId);
				blockStorage.addValueBlock(paths, cacheBlockKey, compressedBytes);
			}
			futureByBlockId.remove(blockId);
			tracker.valueTasks.decrement();
		});
		futureByBlockId.put(blockId, future);
	}

	private void valueBackpressure(){
		long beforeNs = System.nanoTime();
		while(tracker.valueTasks.value() >= maxTasks){
			ThreadTool.trySleep(stallMs);
		}
		long ns = System.nanoTime() - beforeNs;
		tracker.valueStallNs.incrementBy(ns);
	}

	/*----------------- callbacks -------------------*/

	public synchronized void onValueFileWrite(@SuppressWarnings("unused") Void unused){
		Scanner.of(pendingLeafEncoders)
				.include(this::isLeafFlushable)
				.each(_ -> pendingLeafEncoders.remove())
				.forEach(this::flushLeaf);
	}

	public void onLeafFileWrite(@SuppressWarnings("unused") Void unused){
		tryFlushBranches(0);
	}

	public void onBranchFileWrite(int levelWritten){
		tryFlushBranches(levelWritten + 1);
	}

	private synchronized void tryFlushBranches(int level){
		Queue<BranchBlockEncoder> pendingBranchEncodersForLevel = pendingBranchEncodersByLevel.get(level);
		if(pendingBranchEncodersForLevel == null){
			return;
		}
		Scanner.of(pendingBranchEncodersForLevel)
				.include(this::isBranchFlushable)
				.each(_ -> pendingBranchEncodersForLevel.remove())
				.forEach(this::flushBranch);
	}

	/*------------------- complete ---------------------*/

	public void complete(){
		//values
		Scanner.of(valueFutureByBlockIdByColumn.keySet())
				.sort()
				.forEach(column ->
						drainFutures(valueFutureByBlockIdByColumn.get(column).values(), "value column " + column));
		fileWriter.completeValues();

		//leaves
		Scanner.of(pendingLeafEncoders)
				.each(leafEncoder -> Require.isTrue(isLeafFlushable(leafEncoder)))
				.sort(LeafBlockEncoder.BLOCK_ID_COMPARATOR)
				.each(pendingLeafEncoders::remove)
				.forEach(this::flushLeaf);
		drainFutures(leafFutureByBlockId.values(), "leaf");
		fileWriter.completeLeaves();

		//branches
		Scanner.of(pendingBranchEncodersByLevel.keySet()).sort().forEach(level -> {
			Queue<BranchBlockEncoder> encodersForLevel = pendingBranchEncodersByLevel.get(level);
			Scanner.of(encodersForLevel)
					.exclude(BranchBlockEncoder::isEmpty)
					.each(encoder -> Require.isTrue(isBranchFlushable(encoder)))
					.forEach(this::flushBranch);
			drainFutures(branchFutureByBlockIdByLevel.get(level).values(), "branch level " + level);
			fileWriter.completeBranches(level);
		});

		fileWriter.logQueueStats();
	}

	private void drainFutures(Iterable<Future<?>> futures, String stageName){
		var counts = new Counts();
		var pending = counts.add("pending");
		var done = counts.add("done");
		var canceled = counts.add("canceled");
		var waited = counts.add("waited");
		Scanner.of(futures)
				.each(future -> {
					pending.increment();
					if(future.isDone()){
						done.increment();
					}
					if(future.isCancelled()){
						canceled.increment();
					}
					if(!future.isDone() && !future.isCancelled()){
						waited.increment();
					}
				})
				.each(FutureTool::get)
				.count();
		logger.info("drained {} {}", stageName, counts);
	}

}
