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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.bytestringcodec.CsvIntByteStringCodec;
import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.encode.BranchBlockEncoder;
import io.datarouter.filesystem.snapshot.encode.LeafBlockEncoder;
import io.datarouter.filesystem.snapshot.encode.ValueBlockEncoder;
import io.datarouter.filesystem.snapshot.entry.SnapshotEntry;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.storage.block.SnapshotBlockStorage;
import io.datarouter.filesystem.snapshot.storage.file.SnapshotFileStorage;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.concurrent.CountDownLatchTool;
import io.datarouter.util.concurrent.LinkedBlockingDequeTool;

public class SnapshotWriter implements AutoCloseable{
	private static final Logger logger = LoggerFactory.getLogger(SnapshotWriter.class);

	private final SnapshotKey snapshotKey;
	private final SnapshotWriterTracker tracker;
	private final SnapshotWriterConfig config;
	private final SnapshotBlockWriter blockWriter;
	private long lastStatusLogMs;

	private final Thread writerThread;
	private final long startTimeMs;
	private final LinkedBlockingDeque<Message> messages;
	private final CountDownLatch writerThreadCompletionLatch;

	//branch encoders
	private final List<BranchBlockEncoder> branchBlockEncoders;
	private final List<Integer> numBranchBlocksByLevel;

	//leaf encoder
	private LeafBlockEncoder leafBlockEncoder;
	private long numKeys;
	private int numLeafBlocks;
	private SnapshotEntry lastEntry;

	//value encoder
	private final int numColumns;
	private final List<ValueBlockEncoder> valueBlockEncoders;
	private final List<Integer> numValueBlocksByColumn;
	private final List<Integer> numValuesInBlockByColumn;

	public SnapshotWriter(
			SnapshotKey snapshotKey,
			SnapshotFileStorage snapshotFileStorage,
			SnapshotBlockStorage snapshotBlockStorage,
			SnapshotWriterConfig config,
			ExecutorService exec){
		this.snapshotKey = snapshotKey;
		tracker = new SnapshotWriterTracker(snapshotKey);
		this.config = config;
		this.blockWriter = new SnapshotBlockWriter(
				snapshotKey,
				tracker,
				snapshotBlockStorage,
				snapshotFileStorage,
				config, exec);
		lastStatusLogMs = System.currentTimeMillis();

		messages = new LinkedBlockingDeque<>(config.batchQueueLength);
		writerThreadCompletionLatch = new CountDownLatch(1);

		//branch encoders
		this.branchBlockEncoders = new ArrayList<>();
		this.numBranchBlocksByLevel = new ArrayList<>();

		//leaf encoder
		this.leafBlockEncoder = config.leafBlockEncoderSupplier.get();
		this.numLeafBlocks = 0;

		//value encoder
		this.numColumns = config.numColumns;
		this.valueBlockEncoders = new ArrayList<>();
		this.numValueBlocksByColumn = new ArrayList<>();
		this.numValuesInBlockByColumn = new ArrayList<>();
		IntStream.range(0, numColumns).forEach($ -> {
			valueBlockEncoders.add(config.valueBlockEncoderSupplier.get());
			numValueBlocksByColumn.add(0);
			numValuesInBlockByColumn.add(0);
		});

		writerThread = startWriterThread();
		startTimeMs = System.currentTimeMillis();
	}

	private Thread startWriterThread(){
		Runnable writerRunnable = () -> {
			Message batch;
			do{
				long beforeNs = System.nanoTime();
				batch = LinkedBlockingDequeTool.pollForever(messages);
				long ns = System.nanoTime() - beforeNs;
				tracker.readStallNs.incrementBy(ns);
				batch.entries.forEach(this::add);
				tracker.entriesQueued.decrementBySize(batch.entries);
				tracker.entriesProcessed.incrementBySize(batch.entries);
			}while(!batch.isLast);
			writerThreadCompletionLatch.countDown();
		};
		String writerThreadName = String.join("-", getClass().getSimpleName(), snapshotKey.toString());
		Thread thread = new Thread(writerRunnable, writerThreadName);
		thread.start();
		return thread;
	}

	@Override
	public void close(){
		writerThread.interrupt();
	}

	public void addBatch(List<SnapshotEntry> entries){
		LinkedBlockingDequeTool.put(messages, Message.addBatch(entries));
		tracker.entriesQueued.incrementBySize(entries);
		logStatusOccasional();
	}

	private void add(SnapshotEntry entry){
		if(numColumns != entry.columnValues.length){
			String message = String.format("Expected %s values but found %s", numColumns, entry.columnValues.length);
			throw new IllegalArgumentException(message);
		}
		if(config.sorted
				&& lastEntry != null
				&& leafBlockEncoder.numRecords() == 0){// check sorting within block during encoding
			int diff = Arrays.compareUnsigned(
					entry.keySlab(),
					entry.keyFrom(),
					entry.keyTo(),
					lastEntry.keySlab(),
					lastEntry.keyFrom(),
					lastEntry.keyTo());
			if(diff <= 0){
				String message = String.format("key=[%s] must sort after lastKey=[%s]",
						CsvIntByteStringCodec.INSTANCE.encode(entry.key()),
						CsvIntByteStringCodec.INSTANCE.encode(lastEntry.key()));
				throw new IllegalArgumentException(message);
			}
		}

		//keys
		long keyId = numKeys;
		int[] valueBlockIds;
		int[] valueIndexes;
		if(numColumns == 0){
			valueBlockIds = EmptyArray.INT;
			valueIndexes = EmptyArray.INT;
		}else{
			valueBlockIds = new int[numColumns];
			valueIndexes = new int[numColumns];
			for(int column = 0; column < numColumns; ++column){
				valueBlockIds[column] = numValueBlocksByColumn.get(column);
				valueIndexes[column] = numValuesInBlockByColumn.get(column);
			}
		}
		leafBlockEncoder.add(numLeafBlocks, keyId, entry, valueBlockIds, valueIndexes);
		if(leafBlockEncoder.numBytes() >= config.leafBlockSize){
			addBranchEntry(0, keyId, entry, numLeafBlocks);
			blockWriter.submitLeaf(leafBlockEncoder);
			leafBlockEncoder = config.leafBlockEncoderSupplier.get();
			++numLeafBlocks;
		}

		//values
		for(int column = 0; column < numColumns; ++column){
			ValueBlockEncoder valueBlockEncoder = valueBlockEncoders.get(column);
			valueBlockEncoder.add(entry, column);
			numValuesInBlockByColumn.set(column, numValuesInBlockByColumn.get(column) + 1);
			if(valueBlockEncoder.numBytes() >= config.valueBlockSize){
				blockWriter.submitValueBlock(column, numValueBlocksByColumn.get(column), valueBlockEncoder);
				valueBlockEncoders.set(column, config.valueBlockEncoderSupplier.get());
				numValueBlocksByColumn.set(column, numValueBlocksByColumn.get(column) + 1);
				numValuesInBlockByColumn.set(column, 0);
			}
		}

		++numKeys;
		lastEntry = entry;
	}

	private void addBranchEntry(int level, long keyId, SnapshotEntry entry, int childBlockId){
		if(level > branchBlockEncoders.size() - 1){
			branchBlockEncoders.add(config.branchBlockEncoderFactory.apply(level));
			numBranchBlocksByLevel.add(0);
		}
		BranchBlockEncoder encoder = branchBlockEncoders.get(level);
		int blockId = numBranchBlocksByLevel.get(level);
		encoder.add(blockId, keyId, entry, childBlockId);
		if(encoder.numBytes() >= config.branchBlockSize){
			addBranchEntry(level + 1, keyId, entry, numBranchBlocksByLevel.get(level));
			blockWriter.submitBranch(encoder);
			branchBlockEncoders.set(level, config.branchBlockEncoderFactory.apply(level));
			numBranchBlocksByLevel.set(level, numBranchBlocksByLevel.get(level) + 1);
		}
	}

	public Optional<RootBlock> complete(){
		LinkedBlockingDequeTool.put(messages, Message.last());
		CountDownLatchTool.await(writerThreadCompletionLatch);

		// finish value blocks
		IntStream.range(0, valueBlockEncoders.size()).forEach(column -> {
			ValueBlockEncoder valueBlockEncoder = valueBlockEncoders.get(column);
			if(valueBlockEncoder.numRecords() > 0){
				blockWriter.submitValueBlock(column, numValueBlocksByColumn.get(column), valueBlockEncoder);
				numValueBlocksByColumn.set(column, numValueBlocksByColumn.get(column) + 1);
			}
		});

		//finish leaf blocks
		if(leafBlockEncoder.numRecords() > 0){
			addBranchEntry(0, numKeys, lastEntry, numLeafBlocks);
			blockWriter.submitLeaf(leafBlockEncoder);
			++numLeafBlocks;
		}

		//finish branch blocks
		IntStream.range(0, branchBlockEncoders.size()).forEach(level -> {
			BranchBlockEncoder branchEncoder = branchBlockEncoders.get(level);
			if(branchEncoder.numRecords() > 0){
				if(level != branchBlockEncoders.size() - 1){// avoid creating a root block with only one entry
					addBranchEntry(level + 1, numKeys, lastEntry, numBranchBlocksByLevel.get(level));
				}
				blockWriter.submitBranch(branchEncoder);
				branchBlockEncoders.set(level, config.branchBlockEncoderFactory.apply(level));
				numBranchBlocksByLevel.set(level, numBranchBlocksByLevel.get(level) + 1);
			}
		});

		//complete file uploads (could parallelize this?)
		blockWriter.complete();

		//write root block
		if(numKeys == 0){
			return Optional.empty();
		}
		//TODO write to cache if config.updateCache
		RootBlock root = blockWriter.flushRootBlock(
				startTimeMs,
				numBranchBlocksByLevel,
				numValueBlocksByColumn,
				branchBlockEncoders.size(),
				numKeys,
				numLeafBlocks);

		// log completion
		logStatus();
		String logTokens = Scanner.of(root.toKeyValueStrings().entrySet())
				.map(kv -> kv.getKey() + "=" + kv.getValue())
				.collect(Collectors.joining(", "));
		logger.warn("Completed group={}, id={}, {}", snapshotKey.groupId, snapshotKey.snapshotId, logTokens);
		return Optional.of(root);
	}

	private void logStatusOccasional(){
		long now = System.currentTimeMillis();
		long elapsedMs = now - lastStatusLogMs;
		if(elapsedMs > config.logPeriodMs){
			logStatus();
		}
	}

	private void logStatus(){
		long elapsedMs = System.currentTimeMillis() - startTimeMs;
		logger.warn("{}", tracker.toLog(elapsedMs));
		lastStatusLogMs = System.currentTimeMillis();
	}

	private static class Message{

		final List<SnapshotEntry> entries;
		final boolean isLast;

		Message(List<SnapshotEntry> entries, boolean isLast){
			this.entries = entries;
			this.isLast = isLast;
		}

		static Message addBatch(List<SnapshotEntry> entries){
			return new Message(entries, false);
		}

		static Message last(){
			return new Message(List.of(), true);
		}

	}

}
