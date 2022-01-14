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
package io.datarouter.filesystem.snapshot.block.branch;

import io.datarouter.bytes.Bytes;
import io.datarouter.filesystem.snapshot.block.Block;
import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.scanner.Scanner;

public interface BranchBlock extends Block{

	static final boolean LOG_COMPARISONS = false;

	/**
	 * level 0 is a leaf index pointing at LeafBlocks
	 * @return  level
	 */
	int level();

	int numRecords();

	long recordId(int index);

	Bytes key(int index);

	Scanner<Bytes> keys();

	default Bytes lastKey(){
		return key(numRecords() - 1);
	}

	default Scanner<byte[]> keyCopies(){
		return keys()
				.map(Bytes::toArray);
	}

	int childBlock(int recordIndex);

	default long lastRecordId(){
		return recordId(numRecords());
	}

	default int findChildBlockIndex(long recordId){
		int numRecords = numRecords();
		int low = 0;
		int high = numRecords - 1;
		while(low <= high){
			int mid = (low + high) >>> 1;
			long midVal = recordId(mid);
			int diff = Long.compare(midVal, recordId);
			if(diff < 0){
				low = mid + 1;
			}else if(diff > 0){
				high = mid - 1;
			}else{
				return mid;
			}
		}
		return low;
	}

	default int findChildBlockIndex(byte[] searchKey){
		var searchKeyRange = new Bytes(searchKey);
		int numKeys = numRecords();
		int low = 0;
		int high = numKeys - 1;
		while(low <= high){
			int mid = (low + high) >>> 1;
			Bytes midVal = key(mid);
			int diff = midVal.compareTo(searchKeyRange);
			if(diff < 0){
				low = mid + 1;
			}else if(diff > 0){
				high = mid - 1;
			}else{
				return mid;
			}
		}
		return low;
	}

	Scanner<Integer> childBlockIds();

	BlockKey childBranchBlockKey(SnapshotKey snapshotKey, int childBlockId);

	BlockKey leafBlockKey(SnapshotKey snapshotKey, int leafBlockId);

	String toDetailedString();

}
