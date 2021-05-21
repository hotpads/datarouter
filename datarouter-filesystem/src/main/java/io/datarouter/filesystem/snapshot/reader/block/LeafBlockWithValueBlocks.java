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
package io.datarouter.filesystem.snapshot.reader.block;

import java.util.List;
import java.util.stream.IntStream;

import io.datarouter.filesystem.snapshot.block.leaf.LeafBlock;
import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.block.value.ValueBlock;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotRecord;
import io.datarouter.scanner.Scanner;

public class LeafBlockWithValueBlocks{

	private final RootBlock rootBlock;
	private final LeafBlock leafBlock;
	private final int[] firstValueBlockIds;
	private final List<List<ValueBlock>> valueBlocks;

	public LeafBlockWithValueBlocks(RootBlock rootBlock, LeafBlock leafBlock, List<List<ValueBlock>> valueBlocks){
		this.rootBlock = rootBlock;
		this.leafBlock = leafBlock;
		this.firstValueBlockIds = IntStream.range(0, rootBlock.numColumns())
				.map(leafBlock::firstValueBlockId)
				.toArray();
		this.valueBlocks = valueBlocks;
	}

	public ValueBlock getValueBlock(int column, int index){
		return valueBlocks.get(column).get(index - firstValueBlockIds[column]);
	}

	public Scanner<SnapshotRecord> scan(int fromRecordIdInclusive){
		return Scanner.iterate(0, recordIndex -> recordIndex + 1)
				.limit(leafBlock.numRecords())
				//TODO find the start index before this scanner to avoid checking this every time
				.include(recordIndex -> leafBlock.recordId(recordIndex) >= fromRecordIdInclusive)
				.map(recordIndex -> {
					long recordId = leafBlock.firstRecordId() + recordIndex;
					byte[] key = leafBlock.blockKey(recordIndex).toArray();
					byte[] value = leafBlock.blockValue(recordIndex).toArray();
					byte[][] columnValues = new byte[rootBlock.numColumns()][];
					for(int column = 0; column < rootBlock.numColumns(); ++column){
						//TODO improve LeafBlock methods to skip looking up valueBlockOffset
						int valueBlockOffset = leafBlock.valueBlockOffsetForKey(column, recordIndex);
						int valueBlockId = leafBlock.firstValueBlockId(column) + valueBlockOffset;
						ValueBlock valueBlock = getValueBlock(column, valueBlockId);
						int valueIndex = leafBlock.valueIndex(column, valueBlockOffset, recordIndex);
						columnValues[column] = valueBlock.value(valueIndex).toArray();
					}
					return new SnapshotRecord(recordId, key, value, columnValues);
				});
	}

}
