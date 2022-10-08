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

import io.datarouter.filesystem.snapshot.compress.CompressedBlock;
import io.datarouter.filesystem.snapshot.encode.EncodedBlock;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.util.Count;
import io.datarouter.util.Count.Counts;
import io.datarouter.util.number.NumberFormatter;

public class SnapshotWriterTracker{

	private final SnapshotKey snapshotKey;

	private final Counts counts = new Counts();

	public final Count entriesProcessed = counts.add("entriesProcessed");
	public final Count entriesQueued = counts.add("entriesQueued");
	public final Count readStallNs = counts.add("readStallNs");
	public final Count valueStallNs = counts.add("valueStallNs");
	public final Count leafStallNs = counts.add("leafStallNs");

	public final Count valueTasks = counts.add("valueTasks");
	public final Count leafTasks = counts.add("leafTasks");
	public final Count branchTasks = counts.add("branchTasks");

	public final Count valueBlocks = counts.add("valueBlocks");
	public final Count leafBlocks = counts.add("leafBlocks");
	public final Count branchBlocks = counts.add("branchBlocks");

	public final Count valueBytesEncoded = counts.add("valueBytesEncoded");
	public final Count leafBytesEncoded = counts.add("leafBytesEncoded");
	public final Count branchBytesEncoded = counts.add("branchBytesEncoded");

	public final Count valueBytesCompressed = counts.add("valueBytesCompressed");
	public final Count leafBytesCompressed = counts.add("leafBytesCompressed");
	public final Count branchBytesCompressed = counts.add("branchBytesCompressed");

	public final Count valueBlocksInMemory = counts.add("valueBlocksInMemory");
	public final Count leafBlocksInMemory = counts.add("leafBlocksInMemory");
	public final Count branchBlocksInMemory = counts.add("branchBlocksInMemory");

	public final Count valueBytesInMemory = counts.add("valueBytesInMemory");
	public final Count leafBytesInMemory = counts.add("leafBytesInMemory");
	public final Count branchBytesInMemory = counts.add("branchBytesInMemory");

	public SnapshotWriterTracker(SnapshotKey snapshotKey){
		this.snapshotKey = snapshotKey;
	}

	public long totalTasks(){
		return valueTasks.value() + leafTasks.value() + branchTasks.value();
	}

	public void valueBlock(EncodedBlock encodedBlock, CompressedBlock compressedBlock){
		valueBlocks.increment();
		valueBytesEncoded.incrementBy(encodedBlock.totalLength);
		valueBytesCompressed.incrementBy(compressedBlock.totalLength);
	}

	public void leafBlock(EncodedBlock encodedBlock, CompressedBlock compressedBlock){
		leafBlocks.increment();
		leafBytesEncoded.incrementBy(encodedBlock.totalLength);
		leafBytesCompressed.incrementBy(compressedBlock.totalLength);
	}

	public void branchBlock(EncodedBlock encodedBlock, CompressedBlock compressedBlock){
		branchBlocks.increment();
		branchBytesEncoded.incrementBy(encodedBlock.totalLength);
		branchBytesCompressed.incrementBy(compressedBlock.totalLength);
	}

	public void valueMemory(boolean increment, int numBlocks, long numBytes){
		if(increment){
			valueBlocksInMemory.incrementBy(numBlocks);
			valueBytesInMemory.incrementBy(numBytes);
		}else{
			valueBlocksInMemory.decrementBy(numBlocks);
			valueBytesInMemory.decrementBy(numBytes);
		}
	}

	public void leafMemory(boolean increment, int numBlocks, long numBytes){
		if(increment){
			leafBlocksInMemory.incrementBy(numBlocks);
			leafBytesInMemory.incrementBy(numBytes);
		}else{
			leafBlocksInMemory.decrementBy(numBlocks);
			leafBytesInMemory.decrementBy(numBytes);
		}
	}

	public void branchMemory(boolean increment, int numBlocks, long numBytes){
		if(increment){
			branchBlocksInMemory.incrementBy(numBlocks);
			branchBytesInMemory.incrementBy(numBytes);
		}else{
			branchBlocksInMemory.decrementBy(numBlocks);
			branchBytesInMemory.decrementBy(numBytes);
		}
	}

	public String toLog(long elapsedMs){
		long elapsedNs = elapsedMs * 1_000_000;
		double elapsedSeconds = elapsedMs / 1000d;

		double readStallNsDbl = readStallNs.value();
		double valueStallNsDbl = valueStallNs.value();
		double leafStallNsDbl = leafStallNs.value();
		double itemsPerSecDbl = entriesProcessed.value() / elapsedSeconds;

		int readStallPct = (int)(readStallNsDbl / elapsedNs * 100);
		int valueStallPct = (int)(valueStallNsDbl / elapsedNs * 100);
		int leafStallPct = (int)(leafStallNsDbl / elapsedNs * 100);
		long itemsPerSec = (long)itemsPerSecDbl;

		return String.format("group=%s, id=%s, readStall=%s%%, valueStall=%s%%, leafStall=%s%%, itemsPerSec=%s, %s",
				snapshotKey.groupId(),
				snapshotKey.snapshotId(),
				readStallPct,
				valueStallPct,
				leafStallPct,
				NumberFormatter.addCommas(itemsPerSec),
				counts);
	}

}
