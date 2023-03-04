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
package io.datarouter.filesystem.snapshot.reader.block;

import java.util.function.Function;

import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.block.branch.BranchBlock;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlock;
import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;

/**
 * Thread-safe
 *
 * The parallel operations perform the potentially high-latency block fetches
 *
 * The blocks are passed to the parent scanner which parses them.  The parsing could potentially be done in the parallel
 * section, but it would result in more objects being allocated at the same time.  The parsing of the blocks is pretty
 * inexpensive, so this version leaves that for the parent reader thread.
 */
public class ScanningBlockReader{

	private final SnapshotKey snapshotKey;
	private final Threads threads;
	private final int numBlocks;
	private final BlockLoader blockLoader;
	private final RootBlock rootBlock;

	public ScanningBlockReader(
			SnapshotKey snapshotKey,
			Threads threads,
			int numBlocks,
			BlockLoader blockLoader){
		this.snapshotKey = snapshotKey;
		this.threads = threads;
		this.numBlocks = numBlocks;
		this.blockLoader = blockLoader;
		this.rootBlock = blockLoader.root(BlockKey.root(snapshotKey));
	}

	public Scanner<LeafBlock> scanLeafBlocks(long fromRecordIdInclusive){
		return scanLeafBlockKeys(fromRecordIdInclusive)
				.apply(leafBlockKeys -> LeafBlockRangeLoader.splitByFileAndBatch(leafBlockKeys, numBlocks))
				.parallelOrdered(threads)
				.map(blockLoader::leafRange)
				.concat(Function.identity());
	}

	private Scanner<BlockKey> scanLeafBlockKeys(long fromRecordIdInclusive){
		BlockKey topBranchBlockKey = rootBlock.rootBranchBlockKey(snapshotKey);
		BranchBlock topBranchBlock = blockLoader.branch(topBranchBlockKey);
		return scanDescendantBranchBlocks(topBranchBlock, fromRecordIdInclusive)
				.include(branchBlock -> branchBlock.level() == 0)
				.concat(branchBlock -> Scanner.iterate(0, i -> i + 1)
						.limit(branchBlock.numRecords())
						.include(index -> branchBlock.recordId(index) >= fromRecordIdInclusive)
						.map(branchBlock::childBlock)
						.map(leafBlockId -> branchBlock.leafBlockKey(
								snapshotKey,
								leafBlockId)));
	}

	private Scanner<BranchBlock> scanDescendantBranchBlocks(BranchBlock branchBlock, long fromRecordIdInclusive){
		if(branchBlock.level() == 0){
			return Scanner.of(branchBlock);
		}
		return branchBlock.childBlockIds()
				.map(childBlockId -> branchBlock.childBranchBlockKey(
						snapshotKey,
						childBlockId))
				.parallelOrdered(threads)
				.map(blockLoader::branch)
				.include(childBranchBlock -> childBranchBlock.lastRecordId() >= fromRecordIdInclusive)
				.concat(childBranchBlock -> scanDescendantBranchBlocks(childBranchBlock, fromRecordIdInclusive));
	}

}
