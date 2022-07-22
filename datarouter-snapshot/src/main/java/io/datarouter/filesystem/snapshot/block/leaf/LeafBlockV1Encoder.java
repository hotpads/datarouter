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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.ByteWriter;
import io.datarouter.bytes.PagedObjectArray;
import io.datarouter.bytes.codec.bytestringcodec.CsvIntByteStringCodec;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;
import io.datarouter.filesystem.snapshot.encode.EncodedBlock;
import io.datarouter.filesystem.snapshot.encode.LeafBlockEncoder;
import io.datarouter.filesystem.snapshot.entry.SnapshotEntry;
import io.datarouter.filesystem.snapshot.writer.BlockQueue.FileIdsAndEndings;

public class LeafBlockV1Encoder implements LeafBlockEncoder{

	private static final RawIntCodec RAW_INT_CODEC = RawIntCodec.INSTANCE;

	private final int leafEncoderChunkSize;

	private int blockId;

	//keys
	private long firstRecordId;
	private int numRecords;
	private int numKeyBytes;
	private int numValueBytes;
	private int numBytes;
	private final PagedObjectArray<SnapshotEntry> entries;

	//value block references
	private int numColumns;
	private int[] firstValueBlockIds;
	private int[] firstValueIndexes;
	private int[] latestValueBlockIds;
	private List<List<Integer>> valueBlockOffsets;

	public LeafBlockV1Encoder(int leafEncoderChunkSize){
		this.leafEncoderChunkSize = leafEncoderChunkSize;

		//keys
		firstRecordId = -1;
		numRecords = 0;
		numKeyBytes = 0;
		numValueBytes = 0;
		numBytes = 0;
		entries = new PagedObjectArray<>(256);
	}

	@Override
	public String format(){
		return LeafBlockV1.FORMAT;
	}

	@Override
	public void add(int blockId, long recordId, SnapshotEntry entry, int[] valueBlockIds, int[] valueIndexes){
		this.blockId = blockId;

		if(numRecords == 0){
			//keys
			firstRecordId = recordId;
			firstValueBlockIds = valueBlockIds;
			firstValueIndexes = valueIndexes;

			//value block references
			numColumns = valueBlockIds.length;
			latestValueBlockIds = new int[numColumns];
			valueBlockOffsets = new ArrayList<>();
			for(int column = 0; column < numColumns; ++column){
				latestValueBlockIds[column] = -1;
				valueBlockOffsets.add(new ArrayList<>());
			}
		}
		for(int column = 0; column < numColumns; ++column){
			if(valueBlockIds[column] != latestValueBlockIds[column]){
				valueBlockOffsets.get(column).add(numRecords);
				latestValueBlockIds[column] = valueBlockIds[column];
			}
		}
		++numRecords;
		numKeyBytes += entry.keyLength();
		numValueBytes += entry.valueLength();
		numBytes = numKeyBytes + numValueBytes;
		entries.add(entry);
	}

	@Override
	public int numRecords(){
		return entries.size();
	}

	@Override
	public int numBytes(){
		return numBytes;
	}

	@Override
	public byte[] firstKey(){
		return entries.get(0).key();
	}

	//TODO move to encoding stage?
	@Override
	public void assertKeysSorted(){
		Iterator<SnapshotEntry> iter = entries.iterator();
		SnapshotEntry previous = null;
		while(iter.hasNext()){
			SnapshotEntry current = iter.next();
			if(previous != null && !SnapshotEntry.isSorted(previous, current, false)){
				String message = String.format("key=[%s] must sort after previous=[%s]",
						CsvIntByteStringCodec.INSTANCE.encode(current.key()),
						CsvIntByteStringCodec.INSTANCE.encode(previous.key()));
				throw new IllegalStateException(message);
			}
			previous = current;
		}
	}

	@Override
	public int blockId(){
		return blockId;
	}

	@Override
	public int firstValueBlockId(int column){
		return firstValueBlockIds[column];
	}

	@Override
	public int numValueBlocks(int column){
		return valueBlockOffsets.get(column).size();
	}

	@Override
	public EncodedBlock encode(FileIdsAndEndings[] fileIdsAndEndings){
		var headerWriter = new ByteWriter(32);
		headerWriter.varLong(firstRecordId);
		headerWriter.varInt(numRecords);
		headerWriter.varInt(numColumns);

		//key/value endings
		byte[] keyEndings = new byte[entries.size() * 4];
		int latestKeyEnding = 0;
		byte[] valueEndings = new byte[entries.size() * 4];
		int latestValueEnding = 0;
		int cursor = 0;
		for(SnapshotEntry entry : entries){
			latestKeyEnding += entry.keyLength();
			RAW_INT_CODEC.encode(latestKeyEnding, keyEndings, cursor);
			latestValueEnding += entry.valueLength();
			RAW_INT_CODEC.encode(latestValueEnding, valueEndings, cursor);
			cursor += 4;
		}

		//key/value data
		byte[] keys = new byte[numKeyBytes];
		int keyCursor = 0;
		byte[] values = new byte[numValueBytes];
		int valueCursor = 0;
		for(SnapshotEntry entry : entries){//seems faster with this as a separate loop
			int keyLength = entry.keyLength();
			System.arraycopy(entry.keySlab(), entry.keyFrom(), keys, keyCursor, keyLength);
			keyCursor += keyLength;
			int valueLength = entry.valueLength();
			System.arraycopy(entry.valueSlab(), entry.valueFrom(), values, valueCursor, valueLength);
			valueCursor += valueLength;
		}

		//column block references
		ByteWriter[] columnWriters = new ByteWriter[numColumns];
		for(int column = 0; column < numColumns; ++column){
			ByteWriter columnWriter = new ByteWriter(leafEncoderChunkSize);
			columnWriters[column] = columnWriter;

			columnWriter.varInt(firstValueBlockIds[column]);

			//fileIds
			columnWriter.varInt(fileIdsAndEndings[column].fileIds.length);
			columnWriter.rawInts(fileIdsAndEndings[column].fileIds);

			//endings
			columnWriter.varInt(fileIdsAndEndings[column].endings.length);
			columnWriter.rawInts(fileIdsAndEndings[column].endings);

			//offsets
			columnWriter.varInt(firstValueIndexes[column]);
			columnWriter.varInt(valueBlockOffsets.get(column).size());
			valueBlockOffsets.get(column).forEach(columnWriter::rawInt);
		}

		List<byte[]> chunks = new ArrayList<>();
		chunks.addAll(Arrays.asList(headerWriter.trimmedPages()));
		chunks.add(keyEndings);
		chunks.add(keys);
		chunks.add(valueEndings);
		chunks.add(values);
		Arrays.stream(columnWriters)
				.map(ByteWriter::trimmedPages)
				.map(Arrays::asList)
				.forEach(chunks::addAll);
		return new EncodedBlock(chunks.toArray(ByteTool.EMPTY_ARRAY_2));
	}

}
