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
package io.datarouter.filesystem.snapshot.cache;

import java.util.stream.IntStream;

import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.block.branch.BranchBlock;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlock;
import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.block.value.ValueBlock;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.reader.block.BlockLoader;
import io.datarouter.filesystem.snapshot.reader.block.LeafBlockRangeLoader.LeafBlockRange;
import io.datarouter.scanner.Scanner;

/**
 * Simple implementation holds the most recent block of each type accessed. For sequential access we may read the same
 * block many times in a row, in which case this cache allows skipping path generation, block loading, and checksumming.
 *
 * This is extremely optimized so the SnapshotReader doesn't need to think about caching. Numeric block indexes are
 * stored to avoid path-building String concatenation.
 */
public class LatestBlockCache implements BlockLoader{

	private final BlockLoader blockLoader;

	private final int[] branchIndexes;
	private final BranchBlock[] branchBlocks;
	private int leafIndex;
	private LeafBlock leafBlock;
	private int[] valueIndexes;
	private ValueBlock[] valueBlocks;

	public LatestBlockCache(SnapshotKey snapshotKey, BlockLoader blockLoader){
		this.blockLoader = blockLoader;
		RootBlock rootBlock = blockLoader.root(BlockKey.root(snapshotKey));

		int numBranchLevels = rootBlock.numBranchLevels();
		this.branchIndexes = IntStream.range(0, numBranchLevels)
				.map(_ -> -1)
				.toArray();
		this.branchBlocks = new BranchBlock[numBranchLevels];

		this.leafIndex = -1;

		int numColumns = rootBlock.numColumns();
		this.valueIndexes = IntStream.range(0, numColumns)
				.map(_ -> -1)
				.toArray();
		this.valueBlocks = new ValueBlock[numColumns];
	}

	@Override
	public RootBlock root(BlockKey key){
		return blockLoader.root(key);
	}

	@Override
	public BranchBlock branch(BlockKey key){
		if(branchIndexes[key.level()] != key.blockId()){
			branchBlocks[key.level()] = blockLoader.branch(key);
			branchIndexes[key.level()] = key.blockId();
		}
		return branchBlocks[key.level()];
	}

	@Override
	public LeafBlock leaf(BlockKey key){
		if(leafIndex != key.blockId()){
			leafBlock = blockLoader.leaf(key);
			leafIndex = key.blockId();
		}
		return leafBlock;
	}

	@Override
	public Scanner<LeafBlock> leafRange(LeafBlockRange range){
		return blockLoader.leafRange(range);
	}

	@Override
	public ValueBlock value(BlockKey key){
		if(valueIndexes[key.column()] != key.blockId()){
			valueBlocks[key.column()] = blockLoader.value(key);
			valueIndexes[key.column()] = key.blockId();
		}
		return valueBlocks[key.column()];
	}

}
