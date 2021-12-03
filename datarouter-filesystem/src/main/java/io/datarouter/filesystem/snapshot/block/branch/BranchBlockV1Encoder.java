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

import io.datarouter.bytes.ByteWriter;
import io.datarouter.bytes.IntegerByteTool;
import io.datarouter.bytes.LongByteTool;
import io.datarouter.bytes.PagedObjectArray;
import io.datarouter.filesystem.snapshot.encode.BranchBlockEncoder;
import io.datarouter.filesystem.snapshot.encode.EncodedBlock;
import io.datarouter.filesystem.snapshot.entry.SnapshotEntry;
import io.datarouter.filesystem.snapshot.writer.BlockQueue.FileIdsAndEndings;

public class BranchBlockV1Encoder implements BranchBlockEncoder{

	private int blockId;

	private int level;
	private Integer firstChildBlockId;
	private int numRecords;
	private int numBytes;
	private final PagedObjectArray<Long> recordIds;
	private final PagedObjectArray<byte[]> keys;

	public BranchBlockV1Encoder(int level){
		this.level = level;
		this.numRecords = 0;
		this.numBytes = 0;
		this.recordIds = new PagedObjectArray<>(256);
		this.keys = new PagedObjectArray<>(256);
	}

	@Override
	public String format(){
		return BranchBlockV1.FORMAT;
	}

	@Override
	public void add(int blockId, long recordId, SnapshotEntry entry, int childBlockId){
		this.blockId = blockId;
		if(firstChildBlockId == null){
			firstChildBlockId = childBlockId;
		}
		++numRecords;
		byte[] key = entry.key();
		numBytes += key.length;
		recordIds.add(recordId);
		keys.add(key);
	}

	@Override
	public int numRecords(){
		return keys.size();
	}

	@Override
	public int numBytes(){
		return numBytes;
	}

	@Override
	public EncodedBlock encode(FileIdsAndEndings fileIdsAndEndings){
		var headerWriter = new ByteWriter(20);
		headerWriter.varInt(level);
		headerWriter.varInt(firstChildBlockId);
		headerWriter.varInt(numRecords);
		byte[] header = headerWriter.concat();

		byte[] recordIdsChunk = new byte[numRecords * 8];
		int recordIdsCursor = 0;
		for(long recordId : recordIds){
			LongByteTool.toRawBytes(recordId, recordIdsChunk, recordIdsCursor);
			recordIdsCursor += 8;
		}

		byte[] keyEndingsChunk = new byte[numRecords * 4];
		int keyEnding = 0;
		int keyEndingsCursor = 0;
		for(byte[] key : keys){
			int keyLength = key.length;
			keyEnding += keyLength;
			IntegerByteTool.toRawBytes(keyEnding, keyEndingsChunk, keyEndingsCursor);
			keyEndingsCursor += 4;
		}

		byte[] keysChunk = new byte[numBytes];
		int keysCursor = 0;
		for(byte[] key : keys){
			int keyLength = key.length;
			System.arraycopy(key, 0, keysChunk, keysCursor, keyLength);
			keysCursor += keyLength;
		}

		byte[] fileIdsChunk = new byte[fileIdsAndEndings.fileIds.length * 4];
		int fileIdsCursor = 0;
		for(int fileId : fileIdsAndEndings.fileIds){
			IntegerByteTool.toRawBytes(fileId, fileIdsChunk, fileIdsCursor);
			fileIdsCursor += 4;
		}

		byte[] fileEndingsChunk = new byte[fileIdsAndEndings.endings.length * 4];
		int fileEndingsCursor = 0;
		for(int fileEnding : fileIdsAndEndings.endings){
			IntegerByteTool.toRawBytes(fileEnding, fileEndingsChunk, fileEndingsCursor);
			fileEndingsCursor += 4;
		}

		byte[][] chunks = new byte[][]{
				header,
				recordIdsChunk,
				keyEndingsChunk,
				keysChunk,
				fileIdsChunk,
				fileEndingsChunk};
		return new EncodedBlock(chunks);
	}

	@Override
	public int level(){
		return level;
	}

	@Override
	public int blockId(){
		return blockId;
	}

	@Override
	public int firstChildBlockId(){
		return firstChildBlockId;
	}

}
