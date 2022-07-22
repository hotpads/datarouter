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
package io.datarouter.filesystem.snapshot.reader;

import io.datarouter.bytes.Bytes;
import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.block.branch.BranchBlock;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlock;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlock.ValueLocation;
import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.block.value.ValueBlock;
import io.datarouter.filesystem.snapshot.cache.LatestBlockCache;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.reader.block.BlockLoader;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotLeafRecord;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotRecord;

/**
 * Not thread-safe because of LatestBlockCache
 */
public class SnapshotIdReader{

	private final SnapshotKey snapshotKey;
	private final BlockLoader blockLoader;
	private final RootBlock rootBlock;

	public SnapshotIdReader(SnapshotKey snapshotKey, BlockLoader blockLoader){
		this.snapshotKey = snapshotKey;
		// always add LatestBlockCache as it's essential for performance
		this.blockLoader = new LatestBlockCache(snapshotKey, blockLoader);
		this.rootBlock = blockLoader.root(BlockKey.root(snapshotKey));
	}

	public SnapshotLeafRecord leafRecord(long recordId){
		LeafBlock leafBlock = leafBlock(recordId);
		return leafBlock.snapshotLeafRecord(recordId);
	}

	public SnapshotRecord getRecord(long recordId){
		LeafBlock leafBlock = leafBlock(recordId);
		Bytes key = leafBlock.snapshotKey(recordId);
		Bytes value = leafBlock.snapshotValue(recordId);
		int numColumns = rootBlock.numColumns();
		byte[][] columnValues = new byte[numColumns][];
		for(int column = 0; column < numColumns; ++column){
			ValueLocation valueLocation = leafBlock.getValueBlock(column, recordId);
			BlockKey valueBlockKey = leafBlock.valueBlockKey(
					snapshotKey,
					column,
					valueLocation.valueBlockId);
			ValueBlock valueBlock = blockLoader.value(valueBlockKey);
			Bytes columnValue = valueBlock.value(valueLocation.valueIndex);
			columnValues[column] = columnValue.toArray();
		}
		return new SnapshotRecord(recordId, key.toArray(), value.toArray(), columnValues);
	}

	private LeafBlock leafBlock(long recordId){
		BlockKey rootBranchBlockKey = rootBlock.rootBranchBlockKey(snapshotKey);
		BranchBlock branchBlock = blockLoader.branch(rootBranchBlockKey);
		for(int level = rootBlock.maxBranchLevel() - 1; level >= 0; --level){
			int childBlockIndex = branchBlock.findChildBlockIndex(recordId);
			int childBlockId = branchBlock.childBlock(childBlockIndex);
			BlockKey branchBlockKey = branchBlock.childBranchBlockKey(
					snapshotKey,
					childBlockId);
			branchBlock = blockLoader.branch(branchBlockKey);
		}
		int leafBlockIndex = branchBlock.findChildBlockIndex(recordId);
		int leafBlockId = branchBlock.childBlock(leafBlockIndex);
		BlockKey leafBlockKey = branchBlock.leafBlockKey(snapshotKey, leafBlockId);
		return blockLoader.leaf(leafBlockKey);
	}

}
