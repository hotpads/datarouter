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
package io.datarouter.filesystem.snapshot.block.value;

import io.datarouter.filesystem.snapshot.encode.EncodedBlock;
import io.datarouter.filesystem.snapshot.encode.ValueBlockEncoder;
import io.datarouter.filesystem.snapshot.entry.SnapshotEntry;
import io.datarouter.util.array.PagedObjectArray;
import io.datarouter.util.bytes.IntegerByteTool;
import io.datarouter.util.bytes.VarIntTool;

public class ValueBlockV1Encoder implements ValueBlockEncoder{

	private int numRecords;
	private int numBytes;
	private final PagedObjectArray<byte[]> values;

	public ValueBlockV1Encoder(){
		this.numRecords = 0;
		this.numBytes = 0;
		this.values = new PagedObjectArray<>(256);
	}

	@Override
	public void add(SnapshotEntry entry, int column){
		++numRecords;
		byte[] value = entry.columnValues[column];
		numBytes += value.length;
		values.add(value);
	}

	@Override
	public String format(){
		return ValueBlockV1.FORMAT;
	}

	@Override
	public int numRecords(){
		return values.size();
	}

	@Override
	public int numBytes(){
		return numBytes;
	}

	@Override
	public EncodedBlock encode(){
		byte[] headerChunk = VarIntTool.encode(numRecords);
		byte[] endingsChunk = new byte[values.size() * 4];
		byte[] valuesChunk = new byte[numBytes];

		int ending = 0;
		int endingsCursor = 0;
		int valuesCursor = 0;
		for(byte[] value : values){
			int valueLength = value.length;
			ending += valueLength;
			IntegerByteTool.toRawBytes(ending, endingsChunk, endingsCursor);
			endingsCursor += 4;
			System.arraycopy(value, 0, valuesChunk, valuesCursor, valueLength);
			valuesCursor += valueLength;
		}

		byte[][] chunks = new byte[][]{
				headerChunk,
				endingsChunk,
				valuesChunk};
		return new EncodedBlock(chunks);
	}

}
