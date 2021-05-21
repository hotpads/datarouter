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
package io.datarouter.filesystem.snapshot.block.branch;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.block.BlockSizeCalculator;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.model.util.Bytes;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.bytes.ByteReader;
import io.datarouter.util.bytes.IntegerByteTool;
import io.datarouter.util.bytes.LongByteTool;

/**
 * Sections:
 * - level
 * - num keys
 * - key endings
 * - keys
 * - child block ids
 */
public class BranchBlockV1 implements BranchBlock{

	public static final String FORMAT = "branchV1";

	private static final int HEAP_SIZE_OVERHEAD = new BlockSizeCalculator()
			.addObjectHeaders(1)
			.addArrays(1)
			.addInts(6)
			.calculate();

	private final byte[] bytes;
	private final int level;
	private final int firstChildBlockId;
	private final int numRecords;
	private final int recordIdsSectionOffset;
	private final int endingSectionOffset;
	private final int keySectionOffset;
	private final int childFileIdsSectionOffset;
	private final int childEndingsSectionOffset;

	public BranchBlockV1(byte[] bytes){
		this.bytes = bytes;
		var reader = new ByteReader(bytes);
		level = reader.varInt();
		firstChildBlockId = reader.varInt();
		numRecords = reader.varInt();
		recordIdsSectionOffset = reader.position();
		reader.skipLongs(numRecords);
		endingSectionOffset = reader.position();
		reader.skipInts(numRecords);
		keySectionOffset = reader.position();
		reader.skip(keyEnding(numRecords - 1));
		int numEndings = numRecords + 1;// because it includes the previous block's final ending
		childFileIdsSectionOffset = reader.position();
		reader.skipInts(numEndings);
		childEndingsSectionOffset = reader.position();
		reader.skipInts(numEndings);
		reader.assertFinished();
	}

	@Override
	public int heapSize(){
		return HEAP_SIZE_OVERHEAD + BlockSizeCalculator.pad(bytes.length);
	}

	@Override
	public int level(){
		return level;
	}

	@Override
	public int numRecords(){
		return numRecords;
	}

	@Override
	public long recordId(int index){
		int bytesOffset = recordIdsSectionOffset + index * 8;
		return LongByteTool.fromRawBytes(bytes, bytesOffset);
	}

	@Override
	public Bytes key(int index){
		int start = index == 0 ? 0 : keyEnding(index - 1);
		int end = keyEnding(index);
		int length = end - start;
		return new Bytes(bytes, keySectionOffset + start, length);
	}

	@Override
	public Scanner<Bytes> keys(){
		return Scanner.iterate(0, i -> i + 1)
				.limit(numRecords)
				.map(this::key);
	}

	@Override
	public int childBlock(int index){
		return firstChildBlockId + index;
	}

	@Override
	public Scanner<Integer> childBlockIds(){
		return Scanner.iterate(firstChildBlockId, i -> i + 1)
				.limit(numRecords);
	}

	@Override
	public BlockKey childBranchBlockKey(SnapshotKey snapshotKey, int childBlockId){
		int childLevel = level - 1;
		int fileId = childBlockFileId(childBlockId);
		int start = isFirstBlockInFile(childBlockId)
				? 0
				: childBlockEnding(childBlockId - 1);
		int end = childBlockEnding(childBlockId);
		int length = end - start;
		return BlockKey.branch(snapshotKey, childLevel, childBlockId, fileId, start, length);
	}

	@Override
	public BlockKey leafBlockKey(SnapshotKey snapshotKey, int leafBlockId){
		int fileId = childBlockFileId(leafBlockId);
		int start = isFirstBlockInFile(leafBlockId)
				? 0
				: childBlockEnding(leafBlockId - 1);
		int end = childBlockEnding(leafBlockId);
		int length = end - start;
		return BlockKey.leaf(snapshotKey, leafBlockId, fileId, start, length);
	}

	private int keyEnding(int index){
		int endingOffset = 4 * index;
		return IntegerByteTool.fromRawBytes(bytes, endingSectionOffset + endingOffset);
	}

	private boolean isFirstBlockInFile(int childBlockId){
		if(childBlockId == 0){
			return true;
		}
		int previousFileId = childBlockFileId(childBlockId - 1);
		int fileId = childBlockFileId(childBlockId);
		return previousFileId != fileId;
	}

	private int childBlockFileId(int childBlockId){
		int childBlockIndex = childBlockId - firstChildBlockId;
		int indexExcludingPreviousEndings = childBlockIndex + 1;
		int endingOffset = 4 * indexExcludingPreviousEndings;
		return IntegerByteTool.fromRawBytes(bytes, childFileIdsSectionOffset + endingOffset);
	}

	private int childBlockEnding(int childBlockId){
		int childBlockIndex = childBlockId - firstChildBlockId;
		int indexExcludingPreviousEndings = childBlockIndex + 1;
		int endingOffset = 4 * indexExcludingPreviousEndings;
		return IntegerByteTool.fromRawBytes(bytes, childEndingsSectionOffset + endingOffset);
	}

	@Override
	public String toDetailedString(){
		List<String> lines = new ArrayList<>();
		lines.add("                   level " + level);
		lines.add("previousBlockFinalEnding " + childBlockEnding(-1));
		lines.add("       firstChildBlockId " + firstChildBlockId);
		lines.add("              numRecords " + numRecords());
		lines.add("               recordIds " + Scanner.iterate(0, i -> i + 1)
				.limit(numRecords)
				.map(this::recordId)
				.map(Object::toString)
				.collect(Collectors.joining(",", "[", "]")));
		lines.add("              keyEndings " + Scanner.iterate(0, i -> i + 1)
				.limit(numRecords)
				.map(this::keyEnding)
				.map(Object::toString)
				.collect(Collectors.joining(",", "[", "]")));
		lines.add("                childIds " + Scanner.iterate(0, i -> i + 1)
				.limit(numRecords)
				.map(this::childBlock)
				.map(Object::toString)
				.collect(Collectors.joining(",", "[", "]")));
		lines.add("            childEndings " + Scanner.iterate(0, i -> i + 1)
				.limit(numRecords)
				.map(this::childBlockEnding)
				.map(Object::toString)
				.collect(Collectors.joining(",", "[", "]")));
		return "\n" + lines.stream()
				.map(Object::toString)
				.collect(Collectors.joining("\n"));
	}

}
