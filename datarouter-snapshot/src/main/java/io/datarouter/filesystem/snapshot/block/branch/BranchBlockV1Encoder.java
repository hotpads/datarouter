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
import io.datarouter.bytes.codec.intcodec.RawIntCodec;
import io.datarouter.bytes.codec.longcodec.RawLongCodec;
import io.datarouter.filesystem.snapshot.encode.BranchBlockEncoder;
import io.datarouter.filesystem.snapshot.encode.EncodedBlock;
import io.datarouter.filesystem.snapshot.entry.SnapshotEntry;
import io.datarouter.filesystem.snapshot.writer.BlockQueue.FileIdsAndEndings;
import io.datarouter.scanner.PagedList;

public class BranchBlockV1Encoder implements BranchBlockEncoder{

	private static final RawIntCodec RAW_INT_CODEC = RawIntCodec.INSTANCE;
	private static final RawLongCodec RAW_LONG_CODEC = RawLongCodec.INSTANCE;

	private int blockId;

	private int level;
	private Integer firstChildBlockId;
	private int numRecords;
	private int numBytes;
	private final PagedList<Long> recordIds;
	private final PagedList<byte[]> keys;

	public BranchBlockV1Encoder(int level){
		this.level = level;
		this.numRecords = 0;
		this.numBytes = 0;
		this.recordIds = new PagedList<>(256);
		this.keys = new PagedList<>(256);
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
			RAW_LONG_CODEC.encode(recordId, recordIdsChunk, recordIdsCursor);
			recordIdsCursor += 8;
		}

		byte[] keyEndingsChunk = new byte[numRecords * 4];
		int keyEnding = 0;
		int keyEndingsCursor = 0;
		for(byte[] key : keys){
			int keyLength = key.length;
			keyEnding += keyLength;
			RAW_INT_CODEC.encode(keyEnding, keyEndingsChunk, keyEndingsCursor);
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
			RAW_INT_CODEC.encode(fileId, fileIdsChunk, fileIdsCursor);
			fileIdsCursor += 4;
		}

		byte[] fileEndingsChunk = new byte[fileIdsAndEndings.endings.length * 4];
		int fileEndingsCursor = 0;
		for(int fileEnding : fileIdsAndEndings.endings){
			RAW_INT_CODEC.encode(fileEnding, fileEndingsChunk, fileEndingsCursor);
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
