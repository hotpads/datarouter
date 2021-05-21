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

import io.datarouter.filesystem.snapshot.block.BlockSizeCalculator;
import io.datarouter.model.util.Bytes;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.bytes.ByteReader;
import io.datarouter.util.bytes.IntegerByteTool;

/**
 * Simple format using 4-byte ints.  Contains 4 parts:
 * 1) numValues: encoded 4 byte int
 * 2) endingSection:
 *   - 4 byte ints representing the lastByte of each value relative to the valueSection
 * 3) valueSection: variable bytes for each value
 * 4) checksumSection: 8 byte long
 */
public class ValueBlockV1 implements ValueBlock{

	public static final String FORMAT = "valueV1";

	private static final int HEAP_SIZE_OVERHEAD = new BlockSizeCalculator()
			.addObjectHeaders(1)
			.addArrays(1)
			.addInts(3)
			.calculate();

	private final byte[] bytes;
	private final int numValues;
	private final int endingSectionOffset;
	private final int valueSectionOffset;

	public ValueBlockV1(byte[] bytes){
		this.bytes = bytes;
		var reader = new ByteReader(bytes);
		numValues = reader.varInt();
		endingSectionOffset = reader.position();
		reader.skipInts(numValues);
		valueSectionOffset = reader.position();
		reader.skip(valueEnding(numValues - 1));
		reader.assertFinished();
	}

	@Override
	public int heapSize(){
		return HEAP_SIZE_OVERHEAD + BlockSizeCalculator.pad(bytes.length);
	}

	@Override
	public int numValues(){
		return numValues;
	}

	@Override
	public Bytes value(int index){
		int start = index == 0 ? 0 : valueEnding(index - 1);
		int end = valueEnding(index);
		int length = end - start;
		return new Bytes(bytes, valueSectionOffset + start, length);
	}

	@Override
	public Scanner<Bytes> values(){
		return Scanner.iterate(0, i -> i + 1)
				.limit(numValues)
				.map(this::value);
	}

	private int valueEnding(int index){
		int endingOffset = 4 * index;
		return IntegerByteTool.fromRawBytes(bytes, endingSectionOffset + endingOffset);
	}

}
