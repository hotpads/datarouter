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
import java.util.List;
import java.util.stream.Collectors;

import io.datarouter.bytes.ByteReader;
import io.datarouter.bytes.IntegerByteTool;
import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.block.BlockSizeCalculator;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.model.util.Bytes;
import io.datarouter.scanner.Scanner;
/**
 * Sections:
 * - num keys
 * - key endings: relative to the start of the section
 * - keys
 * - value block ids
 * - value block indexes
 */
public class LeafBlockV1 implements LeafBlock{

	public static final String FORMAT = "keyV1";

	private static final int HEAP_SIZE_OVERHEAD = new BlockSizeCalculator()
			.addObjectHeaders(1)
			.addArrays(1)
			.addLongs(1)
			.addInts(7)
			.calculate();

	private final byte[] bytes;

	private final long firstRecordId;
	private final int numRecords;
	private final int numColumns;

	//keys
	private final int keyEndingSectionOffset;
	private final int keySectionOffset;

	//values
	private final int valueEndingSectionOffset;
	private final int valueSectionOffset;

	//value block references
	private final int[] firstValueBlockId;
	private final int[] valueFileIdsSectionOffset;
	private final int[] valueBlockEndingsSectionOffset;
	private final int[] firstValueIndex;
	private final int[] numValueBlockOffsets;
	private final int[] valueBlockOffsetSectionOffset;


	public LeafBlockV1(byte[] bytes){
		this.bytes = bytes;
		var reader = new ByteReader(bytes);

		firstRecordId = reader.varLong();
		numRecords = reader.varInt();
		numColumns = reader.varInt();

		//keys
		keyEndingSectionOffset = reader.position();
		reader.skipInts(numRecords);
		keySectionOffset = reader.position();
		reader.skip(keyEnding(numRecords - 1));

		//values
		valueEndingSectionOffset = reader.position();
		reader.skipInts(numRecords);
		valueSectionOffset = reader.position();
		reader.skip(valueEnding(numRecords - 1));

		//value block references
		firstValueBlockId = new int[numColumns];
		valueFileIdsSectionOffset = new int[numColumns];
		valueBlockEndingsSectionOffset = new int[numColumns];
		firstValueIndex = new int[numColumns];
		numValueBlockOffsets = new int[numColumns];
		valueBlockOffsetSectionOffset = new int[numColumns];
		for(int column = 0; column < numColumns; ++column){
			firstValueBlockId[column] = reader.varInt();

			//fileIds
			int numFileIds = reader.varInt();
			valueFileIdsSectionOffset[column] = reader.position();
			reader.skipInts(numFileIds);

			//endings
			int numValueBlockEndings = reader.varInt();
			valueBlockEndingsSectionOffset[column] = reader.position();
			reader.skipInts(numValueBlockEndings);

			//offsets
			firstValueIndex[column] = reader.varInt();
			numValueBlockOffsets[column] = reader.varInt();
			valueBlockOffsetSectionOffset[column] = reader.position();
			reader.skipInts(numValueBlockOffsets[column]);
		}

		reader.assertFinished();
	}

	@Override
	public String toString(){
		return "LeafBlockV1 [numKeys=" + numRecords + ", keyOffsetSectionOffset=" + keyEndingSectionOffset
				+ ", keySectionOffset=" + keySectionOffset + "]";
	}

	@Override
	public int heapSize(){
		return HEAP_SIZE_OVERHEAD + BlockSizeCalculator.pad(bytes.length);
	}

	@Override
	public long recordId(int index){
		return firstRecordId + index;
	}

	@Override
	public int numRecords(){
		return numRecords;
	}

	@Override
	public Bytes blockKey(int index){
		int start = index == 0 ? 0 : keyEnding(index - 1);
		int end = keyEnding(index);
		int length = end - start;
		return new Bytes(bytes, keySectionOffset + start, length);
	}

	@Override
	public Bytes blockValue(int index){
		int start = index == 0 ? 0 : valueEnding(index - 1);
		int end = valueEnding(index);
		int length = end - start;
		return new Bytes(bytes, valueSectionOffset + start, length);
	}

	@Override
	public int firstValueBlockId(int column){
		return firstValueBlockId[column];
	}

	@Override
	public int numValueBlocks(int column){
		return numValueBlockOffsets[column];
	}

	private int fileId(int column, int valueBlockId){
		int valueBlockIndex = valueBlockId - firstValueBlockId[column] + 1;
		int cursor = valueFileIdsSectionOffset[column] + 4 * valueBlockIndex;
		return IntegerByteTool.fromRawBytes(bytes, cursor);
	}

	@Override
	public int valueBlockEnding(int column, int valueBlockId){
		int valueBlockIndex = valueBlockId - firstValueBlockId[column] + 1;
		int cursor = valueBlockEndingsSectionOffset[column] + 4 * valueBlockIndex;
		return IntegerByteTool.fromRawBytes(bytes, cursor);
	}

	@Override
	public BlockKey valueBlockKey(SnapshotKey snapshotKey, int column, int valueBlockId){
		int fileId = fileId(column, valueBlockId);
		int start = isFirstBlockInFile(column, valueBlockId)
				? 0
				: valueBlockEnding(column, valueBlockId - 1);
		int end = valueBlockEnding(column, valueBlockId);
		int length = end - start;
		return BlockKey.value(snapshotKey, column, valueBlockId, fileId, start, length);
	}

	private boolean isFirstBlockInFile(int column, int valueBlockId){
		if(valueBlockId == 0){
			return true;
		}
		int previousFileId = fileId(column, valueBlockId - 1);
		int fileId = fileId(column, valueBlockId);
		return previousFileId != fileId;
	}

	@Override
	public int firstValueIndex(int column){
		return firstValueIndex[column];
	}

	@Override
	public int valueBlockOffset(int column, int valueBlockOffsetIndex){
		int bytesOffset = valueBlockOffsetSectionOffset[column] + (4 * valueBlockOffsetIndex);
		return IntegerByteTool.fromRawBytes(bytes, bytesOffset);
	}

	private int keyEnding(int index){
		int endingOffset = 4 * index;
		return IntegerByteTool.fromRawBytes(bytes, keyEndingSectionOffset + endingOffset);
	}

	private int valueEnding(int index){
		int endingOffset = 4 * index;
		return IntegerByteTool.fromRawBytes(bytes, valueEndingSectionOffset + endingOffset);
	}

	public String toDetailedString(){
		List<String> lines = new ArrayList<>();
		lines.add("  numRecords" + numRecords);
		lines.add("  keyEndings" + Scanner.iterate(0, i -> i + 1)
				.limit(numRecords)
				.map(this::keyEnding)
				.map(Object::toString)
				.collect(Collectors.joining(",", "[", "]")));
		lines.add("valueEndings" + Scanner.iterate(0, i -> i + 1)
				.limit(numRecords)
				.map(this::valueEnding)
				.map(Object::toString)
				.collect(Collectors.joining(",", "[", "]")));
		return "\n" + lines.stream()
				.map(Object::toString)
				.collect(Collectors.joining("\n"));
	}

}
