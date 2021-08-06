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
package io.datarouter.filesystem.snapshot.block.leaf;

import java.util.Optional;

import io.datarouter.filesystem.snapshot.block.Block;
import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotLeafRecord;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotLeafSearchResult;
import io.datarouter.model.util.Bytes;
import io.datarouter.scanner.Scanner;

public interface LeafBlock extends Block{

	long recordId(int index);

	/**
	 * @return  Offset from first key in snapshot
	 */
	default long firstRecordId(){
		return recordId(0);
	}

	/**
	 * @return  Number of keys in the block
	 */
	int numRecords();

	/*----------------search ------------------*/

	default Optional<Long> findRecordId(byte[] searchKey){
		return findRecordIndex(searchKey)
				.map(this::recordId);
	}

	default Optional<Integer> findRecordIndex(byte[] searchKey){
		int index = insertionIndex(searchKey);
		return index < 0 ? Optional.empty() : Optional.of(index);
	}

	default SnapshotLeafSearchResult search(byte[] searchKey){
		int index = insertionIndex(searchKey);
		boolean exactMatch = true;
		if(index < 0){
			index = -index - 1;
			exactMatch = false;
		}
		long recordId = recordId(index);
		return new SnapshotLeafSearchResult(recordId, exactMatch);
	}

	/**
	 * @return index of first match if present, otherwise -(insertionPoint + 1)
	 */
	default int insertionIndex(byte[] searchKey){
		var searchKeyRange = new Bytes(searchKey);
		int low = 0;
		int mid = 0;
		int high = numRecords() - 1;
		int diff = 0;
		int lastMatch = -1;
		while(low <= high){
			mid = (low + high) >>> 1;
			Bytes midVal = blockKey(mid);
			diff = midVal.compareTo(searchKeyRange);
			if(diff < 0){
				low = mid + 1;
			}else if(diff == 0){
				lastMatch = mid;
				high = mid - 1;
			}else{
				high = mid - 1;
			}
		}
		if(lastMatch >= 0){
			return lastMatch;
		}else if(diff < 0){
			int insertionPoint = mid + 1;
			return -(insertionPoint + 1);
		}else{
			int insertionPoint = mid;
			return -(insertionPoint + 1);
		}
	}

	/*--------------- keys --------------------*/

	/**
	 * @return  Nth key in the block
	 */
	Bytes blockKey(int index);

	/**
	 * @return  Nth key in the snapshot
	 */
	default Bytes snapshotKey(long recordId){
		long blockRecordIndex = recordId - firstRecordId();
		return blockKey((int)blockRecordIndex);
	}

	/**
	 * The Scanner may reuse the same ByteRange for all keys, meaning the caller should clone the ByteRanges if they
	 * need to be long-lived.
	 *
	 * @return  Scanner of key transient ByteRanges
	 */
	default Scanner<Bytes> keys(){
		return Scanner.iterate(0, i -> i + 1)
				.limit(numRecords())
				.map(this::blockKey);
	}

	default Scanner<byte[]> keyCopies(){
		return keys()
				.map(Bytes::toArray);
	}

	/*------------------- values --------------------*/

	/**
	 * @return  Nth value in the block
	 */
	Bytes blockValue(int index);

	/**
	 * @return  Nth key in the snapshot
	 */
	default Bytes snapshotValue(long recordId){
		int index = (int)(recordId - firstRecordId());
		return blockValue(index);
	}

	default Scanner<Bytes> values(){
		return Scanner.iterate(0, i -> i + 1)
				.limit(numRecords())
				.map(this::blockValue);
	}

	default Scanner<byte[]> valueCopies(){
		return values()
				.map(Bytes::toArray);
	}

	/*------------------- key + value --------------------*/

	default SnapshotLeafRecord snapshotLeafRecord(long recordId){
		byte[] key = snapshotKey(recordId).toArray();
		byte[] value = snapshotValue(recordId).toArray();
		return new SnapshotLeafRecord(recordId, key, value);
	}

	default Scanner<SnapshotLeafRecord> leafRecords(){
		return Scanner.iterate(firstRecordId(), i -> i + 1)
				.limit(numRecords())
				.map(this::snapshotLeafRecord);
	}

	/*------------------- column values --------------------*/

	public static class ValueLocation{

		public final int valueBlockId;
		public final int valueIndex;

		public ValueLocation(int valueBlockId, int valueIndex){
			this.valueBlockId = valueBlockId;
			this.valueIndex = valueIndex;
		}

	}

	default ValueLocation getValueBlock(int column, long recordId){
		int index = (int)(recordId - firstRecordId());
		int valueBlockOffset = valueBlockOffsetForKey(column, index);
		int valueBlockId = firstValueBlockId(column) + valueBlockOffset;
		int valueIndex = valueIndex(column, valueBlockOffset, index);
		return new ValueLocation(valueBlockId, valueIndex);
	}

	default Optional<ValueLocation> findValueBlock(int column, byte[] searchKey){
		return findRecordIndex(searchKey)
				.map(index -> {
					int valueBlockOffset = valueBlockOffsetForKey(column, index);
					int valueBlockId = firstValueBlockId(column) + valueBlockOffset;
					int valueIndex = valueIndex(column, valueBlockOffset, index);
					return new ValueLocation(valueBlockId, valueIndex);
				});
	}

	/**
	 * @return  First ValueBlock known to this LeafBlock
	 */
	int firstValueBlockId(int column);

	/**
	 * @return  Number of ValueBlocks referenced by this LeafBlock
	 */
	int numValueBlocks(int column);

	default Scanner<Integer> valueBlockIds(int column){
		return Scanner.iterate(firstValueBlockId(column), i -> i + 1)
				.limit(numValueBlocks(column));
	}

	int valueBlockEnding(int column, int valueBlockId);

	BlockKey valueBlockKey(SnapshotKey snapshotKey, int column, int valueBlockId);

	/**
	 * @return  First index into first ValueBlock known to this LeafBlock
	 */
	int firstValueIndex(int column);

	/**
	 * @param valueBlockOffsetIndex offset into the list of valueBlockOffsets.  Passing 0 should always return 0.
	 * @return recordIndex at the requested offset
	 */
	int valueBlockOffset(int column, int valueBlockOffsetIndex);

	/**
	 * @param index  Record index
	 * @return  Offset of ValueBlock from firstValueBlock
	 */
	default int valueBlockOffsetForKey(int column, int index){
		int valueBlockOffsetIndex = 0;
		for(int i = 1; i <= numValueBlocks(column) - 1; ++i){
			if(valueBlockOffset(column, i) <= index){
				++valueBlockOffsetIndex;
			}
		}
		return valueBlockOffsetIndex;
	}

	/**
	 * @param index  Key index
	 * @return  The index of the value in the value block (not the byte offset)
	 */
	default int valueIndex(int column, int valueBlockOffsetForKey, int index){
		int valueBlockOffset = valueBlockOffsetForKey;
		if(valueBlockOffset == 0){
			return firstValueIndex(column) + index;
		}
		int previousValueBlockOffset = valueBlockOffset(column, valueBlockOffset);
		return index - previousValueBlockOffset;
	}

}
