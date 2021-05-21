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
package io.datarouter.filesystem.snapshot.reader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlock;
import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.block.value.ValueBlock;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.reader.block.BlockLoader;
import io.datarouter.filesystem.snapshot.reader.block.LeafBlockWithValueBlocks;
import io.datarouter.filesystem.snapshot.reader.block.ScanningBlockReader;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotLeafRecord;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotLeafSearchResult;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotRecord;
import io.datarouter.scanner.Scanner;

/**
 * Thread-safe
 *
 * The parallel operations perform the potentially high-latency block fetches
 *
 * The blocks are passed to the parent scanner which parses them.  The parsing could potentially be done in the parallel
 * section, but it would result in more objects being allocated at the same time.  The parsing of the blocks is pretty
 * inexpensive, so this version leaves that for the parent reader thread.
 */
public class ScanningSnapshotReader{

	//TODO make this configurable per scan
	private static final int NUM_BLOCKS = 100;

	private final SnapshotKey snapshotKey;
	private final BlockLoader blockLoader;
	private final RootBlock rootBlock;
	private final ScanningBlockReader scanningBlockReader;

	public ScanningSnapshotReader(
			SnapshotKey snapshotKey,
			ExecutorService exec,
			int numThreads,
			BlockLoader blockLoader){
		this.snapshotKey = snapshotKey;
		this.blockLoader = blockLoader;
		this.rootBlock = blockLoader.root(BlockKey.root(snapshotKey));
		scanningBlockReader = new ScanningBlockReader(snapshotKey, exec, numThreads, NUM_BLOCKS, blockLoader);
	}

	public Scanner<SnapshotLeafRecord> scanLeafRecords(long fromRecordIdInclusive){
		return scanningBlockReader.scanLeafBlocks(fromRecordIdInclusive)
				.concat(LeafBlock::leafRecords)
				.include(leafRecord -> leafRecord.id >= fromRecordIdInclusive);
	}

	public Scanner<SnapshotLeafRecord> scanLeafRecords(byte[] startKey, boolean inclusive){
		var keyReader = new SnapshotKeyReader(snapshotKey, blockLoader);
		LeafBlock leafBlock = keyReader.leafBlock(startKey);
		SnapshotLeafSearchResult searchResult = leafBlock.search(startKey);
		long fromRecordIdInclusive = searchResult.recordId(inclusive);
		return scanLeafRecords(fromRecordIdInclusive);
	}

	public Scanner<byte[]> scanKeys(){
		return scanningBlockReader.scanLeafBlocks(0)
				.concat(LeafBlock::keyCopies);
	}

	public Scanner<byte[]> scanValues(){
		return scanningBlockReader.scanLeafBlocks(0)
				.concat(LeafBlock::valueCopies);
	}

	public Scanner<byte[]> scanColumnValues(int column){
		var lastValueBlockId = new AtomicInteger(-1);
		return scanningBlockReader.scanLeafBlocks(0)
				.concat(leafBlock -> {
					return leafBlock.valueBlockIds(column)
							.include(valueBlockId -> lastValueBlockId.compareAndSet(valueBlockId - 1, valueBlockId))
							.map(valueBlockId -> leafBlock.valueBlockKey(
									snapshotKey,
									column,
									valueBlockId))
							.map(blockLoader::value)
							.concat(ValueBlock::valueCopies);
				});
	}

	public Scanner<SnapshotRecord> scan(int fromRecordIdInclusive){
		return scanningBlockReader.scanLeafBlocks(fromRecordIdInclusive)
				.map(leafBlock -> {
					return Scanner.iterate(0, column -> column + 1)
							.limit(rootBlock.numColumns())
							.map(column -> leafBlock.valueBlockIds(column)
									.map(valueBlockId -> {
										BlockKey valueBlockKey = leafBlock.valueBlockKey(
												snapshotKey,
												column,
												valueBlockId);
										return blockLoader.value(valueBlockKey);
									})
									.list())
							.listTo(valueBlocks -> new LeafBlockWithValueBlocks(rootBlock, leafBlock, valueBlocks));
				})
				.concat(leafBlockWithValueBlocks -> leafBlockWithValueBlocks.scan(fromRecordIdInclusive));
	}

}
