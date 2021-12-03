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
package io.datarouter.filesystem.snapshot.block.root;

import java.util.Arrays;

import io.datarouter.bytes.ByteReader;
import io.datarouter.filesystem.snapshot.block.BlockSizeCalculator;
import io.datarouter.scanner.Scanner;

public class RootBlockV1 implements RootBlock{

	public static final String FORMAT = "rootV1";

	private static final int HEAP_SIZE_OVERHEAD = new BlockSizeCalculator()
			.addObjectHeaders(1)
			.addInts(9)
			.addLongs(7)
			.addRefs(14)//Strings and byte[]s
			.calculate();

	private final int numRootBytesEncoded;

	private final boolean sorted;

	private final String pathFormat;

	private final String branchBlockType;
	private final String leafBlockType;
	private final String valueBlockType;

	private final String branchBlockCompressor;
	private final String leafBlockCompressor;
	private final String valueBlockCompressor;

	private final int branchBytesPerFile;
	private final int leafBytesPerFile;
	private final int valueBytesPerFile;

	private final int branchBlocksPerFile;
	private final int leafBlocksPerFile;
	private final int valueBlocksPerFile;

	private final long numEntries;
	private final int numBranchLevels;
	private final int[] numBranchBlocksByLevel;
	private final int numBranchBlocks;
	private final int numLeafBlocks;
	private final int numColumns;
	private final int[] numValueBlocksByColumn;
	private final int numValueBlocks;
	private final long numBranchBytesEncoded;
	private final long numLeafBytesEncoded;
	private final long numValueBytesEncoded;
	private final long numBranchBytesCompressed;
	private final long numLeafBytesCompressed;
	private final long numValueBytesCompressed;

	private final long writeStartTimeMs;
	private final long writeDurationMs;

	private final int rootBranchBlockEnding;

	private final byte[][] dictionaryKeys;// the encoder will sort these to enable binary search when reading
	private final byte[][] dictionaryValues;

	public RootBlockV1(byte[] bytes){
		this.numRootBytesEncoded = bytes.length;
		var reader = new ByteReader(bytes);
		reader.varUtf8();// skip over root block type string

		//sorted
		sorted = reader.booleanByte();

		//paths
		pathFormat = reader.varUtf8();

		// block types
		branchBlockType = reader.varUtf8();
		leafBlockType = reader.varUtf8();
		valueBlockType = reader.varUtf8();

		// compressors
		branchBlockCompressor = reader.varUtf8();
		leafBlockCompressor = reader.varUtf8();
		valueBlockCompressor = reader.varUtf8();

		// bytesPerFile
		branchBytesPerFile = reader.varInt();
		leafBytesPerFile = reader.varInt();
		valueBytesPerFile = reader.varInt();

		// blocksPerFile
		branchBlocksPerFile = reader.varInt();
		leafBlocksPerFile = reader.varInt();
		valueBlocksPerFile = reader.varInt();

		// counts
		numEntries = reader.varLong();
		numBranchLevels = reader.varInt();
		numBranchBlocksByLevel = reader.varInts(numBranchLevels);
		numBranchBlocks = Arrays.stream(numBranchBlocksByLevel).sum();
		numLeafBlocks = reader.varInt();
		numColumns = reader.varInt();
		numValueBlocksByColumn = reader.varInts(numColumns);
		numValueBlocks = Arrays.stream(numValueBlocksByColumn).sum();

		numBranchBytesEncoded = reader.varLong();
		numLeafBytesEncoded = reader.varLong();
		numValueBytesEncoded = reader.varLong();

		numBranchBytesCompressed = reader.varLong();
		numLeafBytesCompressed = reader.varLong();
		numValueBytesCompressed = reader.varLong();

		writeStartTimeMs = reader.varLong();
		writeDurationMs = reader.varLong();

		rootBranchBlockEnding = reader.varInt();

		//dictionary
		int numDictionaryEntries = reader.varInt();
		dictionaryKeys = new byte[numDictionaryEntries][];
		dictionaryValues = new byte[numDictionaryEntries][];
		for(int i = 0; i < numDictionaryEntries; ++i){
			dictionaryKeys[i] = reader.varBytes();
			dictionaryValues[i] = reader.varBytes();
		}
	}

	@Override
	public int heapSize(){
		var calculator = new BlockSizeCalculator()
				.addStringValue(pathFormat)
				.addStringValue(branchBlockType)
				.addStringValue(leafBlockType)
				.addStringValue(valueBlockType)
				.addStringValue(branchBlockCompressor)
				.addStringValue(leafBlockCompressor)
				.addStringValue(valueBlockCompressor)
				.addIntArrayValue(numBranchBlocksByLevel)
				.addIntArrayValue(numValueBlocksByColumn);
		Scanner.of(dictionaryKeys).forEach(calculator::addByteArrayValue);
		Scanner.of(dictionaryValues).forEach(calculator::addByteArrayValue);
		return HEAP_SIZE_OVERHEAD + calculator.calculate();
	}

	@Override
	public boolean sorted(){
		return sorted;
	}

	@Override
	public String pathFormat(){
		return pathFormat;
	}

	/*------------- encoders ----------------*/

	@Override
	public String branchBlockType(){
		return branchBlockType;
	}

	@Override
	public String leafBlockType(){
		return leafBlockType;
	}

	@Override
	public String valueBlockType(){
		return valueBlockType;
	}

	/*------------- compressors ----------------*/

	@Override
	public String branchBlockCompressor(){
		return branchBlockCompressor;
	}

	@Override
	public String leafBlockCompressor(){
		return leafBlockCompressor;
	}

	@Override
	public String valueBlockCompressor(){
		return valueBlockCompressor;
	}

	/*------------- bytesPerFile ----------------*/

	@Override
	public int branchBytesPerFile(){
		return branchBytesPerFile;
	}

	@Override
	public int leafBytesPerFile(){
		return leafBytesPerFile;
	}

	@Override
	public int valueBytesPerFile(){
		return valueBytesPerFile;
	}

	/*------------- blocksPerFile ----------------*/

	@Override
	public int branchBlocksPerFile(){
		return branchBlocksPerFile;
	}

	@Override
	public int leafBlocksPerFile(){
		return leafBlocksPerFile;
	}

	@Override
	public int valueBlocksPerFile(){
		return valueBlocksPerFile;
	}

	/*------------- counts ----------------*/

	@Override
	public long numRecords(){
		return numEntries;
	}

	@Override
	public int numBranchLevels(){
		return numBranchLevels;
	}

	@Override
	public int numBranchBlocks(){
		return numBranchBlocks;
	}

	@Override
	public int numBranchBlocks(int level){
		return numBranchBlocksByLevel[level];
	}

	@Override
	public int numLeafBlocks(){
		return numLeafBlocks;
	}

	@Override
	public int numColumns(){
		return numColumns;
	}

	@Override
	public int numValueBlocks(int column){
		return numValueBlocksByColumn[column];
	}

	@Override
	public int numValueBlocks(){
		return numValueBlocks;
	}

	/*---------- encoded size --------------*/

	@Override
	public long numRootBytesEncoded(){
		return numRootBytesEncoded;
	}

	@Override
	public long numBranchBytesEncoded(){
		return numBranchBytesEncoded;
	}

	@Override
	public long numLeafBytesEncoded(){
		return numLeafBytesEncoded;
	}

	@Override
	public long numValueBytesEncoded(){
		return numValueBytesEncoded;
	}

	/*---------- compressed size --------------*/

	@Override
	public long numBranchBytesCompressed(){
		return numBranchBytesCompressed;
	}

	@Override
	public long numLeafBytesCompressed(){
		return numLeafBytesCompressed;
	}

	@Override
	public long numValueBytesCompressed(){
		return numValueBytesCompressed;
	}

	/*------------- timings ----------------*/

	@Override
	public long writeStartTimeMs(){
		return writeStartTimeMs;
	}

	@Override
	public long writeDurationMs(){
		return writeDurationMs;
	}

	/*------------- branch ----------------*/

	@Override
	public int rootBranchBlockEnding(){
		return rootBranchBlockEnding;
	}

	/*------------- dictionary ----------------*/

	@Override
	public int numDictionaryEntries(){
		return dictionaryKeys.length;
	}

	@Override
	public byte[] dictionaryKey(int index){
		return dictionaryKeys[index];
	}

	@Override
	public byte[] dictionaryValue(int index){
		return dictionaryValues[index];
	}

}
