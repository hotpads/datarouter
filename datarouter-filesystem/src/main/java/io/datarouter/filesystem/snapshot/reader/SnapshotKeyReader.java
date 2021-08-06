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

import java.util.Optional;

import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.block.branch.BranchBlock;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlock;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlock.ValueLocation;
import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.block.value.ValueBlock;
import io.datarouter.filesystem.snapshot.cache.LatestBlockCache;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.reader.block.BlockLoader;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotRecord;
import io.datarouter.model.util.Bytes;

/**
 * Not thread-safe because of LatestBlockCache
 */
public class SnapshotKeyReader{

	private final SnapshotKey snapshotKey;
	private final BlockLoader blockLoader;
	private final RootBlock rootBlock;

	public SnapshotKeyReader(SnapshotKey snapshotKey, BlockLoader blockLoader){
		this.snapshotKey = snapshotKey;
		// always add LatestBlockCache as it's essential for performance
		this.blockLoader = new LatestBlockCache(snapshotKey, blockLoader);
		this.rootBlock = blockLoader.root(BlockKey.root(snapshotKey));
		if(!rootBlock.sorted()){
			String message = String.format("%s only supported for sorted snapshots", getClass().getSimpleName());
			throw new IllegalStateException(message);
		}
	}

	public Optional<Long> findRecordId(byte[] searchKey){
		LeafBlock leafBlock = leafBlock(searchKey);
		return leafBlock.findRecordId(searchKey);
	}

	public Optional<SnapshotRecord> findRecord(byte[] searchKey){
		LeafBlock leafBlock = leafBlock(searchKey);
		Optional<Long> optRecordId = leafBlock.findRecordId(searchKey);
		if(optRecordId.isEmpty()){
			return Optional.empty();
		}
		long recordId = optRecordId.get();
		byte[] value = leafBlock.snapshotValue(recordId).toArray();
		int numColumns = rootBlock.numColumns();
		byte[][] columnValues = new byte[numColumns][];
		for(int column = 0; column < numColumns; ++column){
			//TODO lookup value blocks using already-found keyId
			Optional<ValueLocation> optValueLocation = leafBlock.findValueBlock(column, searchKey);
			if(optValueLocation.isEmpty()){
				return Optional.empty();
			}
			ValueLocation valueLocation = optValueLocation.get();
			BlockKey valueBlockKey = leafBlock.valueBlockKey(
					snapshotKey,
					column,
					valueLocation.valueBlockId);
			ValueBlock valueBlock = blockLoader.value(valueBlockKey);
			Bytes columnValue = valueBlock.value(valueLocation.valueIndex);
			columnValues[column] = columnValue.toArray();
		}
		return Optional.of(new SnapshotRecord(recordId, searchKey, value, columnValues));
	}

	public LeafBlock leafBlock(byte[] searchKey){
		BlockKey rootBranchBlockKey = rootBlock.rootBranchBlockKey(snapshotKey);
		BranchBlock branchBlock = blockLoader.branch(rootBranchBlockKey);
		for(int level = rootBlock.maxBranchLevel() - 1; level >= 0; --level){
			int childBlockIndex = branchBlock.findChildBlockIndex(searchKey);
			int childBlockId = branchBlock.childBlock(childBlockIndex);
			BlockKey branchBlockKey = branchBlock.childBranchBlockKey(
					snapshotKey,
					childBlockId);
			branchBlock = blockLoader.branch(branchBlockKey);
		}
		int leafBlockIndex = branchBlock.findChildBlockIndex(searchKey);
		int leafBlockId = branchBlock.childBlock(leafBlockIndex);
		BlockKey leafBlockKey = branchBlock.leafBlockKey(snapshotKey, leafBlockId);
		return blockLoader.leaf(leafBlockKey);
	}

}
