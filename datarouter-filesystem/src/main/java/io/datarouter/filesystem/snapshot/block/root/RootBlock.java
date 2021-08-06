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

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;

import io.datarouter.filesystem.snapshot.block.Block;
import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.util.bytes.ByteReader;
import io.datarouter.util.number.NumberFormatter;

public interface RootBlock extends Block{

	static String type(byte[] bytes){
		return new ByteReader(bytes).varUtf8();
	}

	boolean sorted();

	String pathFormat();

	String branchBlockType();
	String leafBlockType();
	String valueBlockType();

	String branchBlockCompressor();
	String leafBlockCompressor();
	String valueBlockCompressor();

	int branchBytesPerFile();
	int leafBytesPerFile();
	int valueBytesPerFile();

	int branchBlocksPerFile();
	int leafBlocksPerFile();
	int valueBlocksPerFile();

	long numRecords();
	int numBranchBlocks();
	int numBranchBlocks(int level);
	int numLeafBlocks();
	int numColumns();
	int numValueBlocks(int column);
	int numValueBlocks();

	int numBranchLevels();

	default int maxBranchLevel(){
		return numBranchLevels() - 1;
	}

	/*----------- sizes -----------------*/

	long numRootBytesEncoded();
	long numBranchBytesEncoded();
	long numLeafBytesEncoded();
	long numValueBytesEncoded();

	default long totalBytesEncoded(){
		return numRootBytesEncoded()
				+ numBranchBytesEncoded()
				+ numLeafBytesEncoded()
				+ numValueBytesEncoded();
	}

	long numBranchBytesCompressed();
	long numLeafBytesCompressed();
	long numValueBytesCompressed();

	default long totalBytesCompressed(){
		return numRootBytesEncoded()
				+ numBranchBytesCompressed()
				+ numLeafBytesCompressed()
				+ numValueBytesCompressed();
	}

	/*------------ timings ------------*/

	long writeStartTimeMs();
	long writeDurationMs();

	default Duration writeDuration(){
		return Duration.ofMillis(writeDurationMs());
	}

	/*---------- block file ids ------------*/

	default int branchFileId(
			@SuppressWarnings("unused") int level, //to support variable length files in the future
			int blockId){
		return blockId / branchBlocksPerFile();
	}

	default int leafFileId(int blockId){
		return blockId / leafBlocksPerFile();
	}

	default int valueFileId(int blockId){
		return blockId / valueBlocksPerFile();
	}

	/*----------- block keys -----------------*/

	int rootBranchBlockEnding();

	default BlockKey rootBranchBlockKey(SnapshotKey snapshotKey){
		return BlockKey.branch(snapshotKey, maxBranchLevel(), 0, 0, 0, rootBranchBlockEnding());
	}

	/*------------- dictionary ----------------*/

	int numDictionaryEntries();
	byte[] dictionaryKey(int index);
	byte[] dictionaryValue(int index);

	/*------------- info ----------------*/

	default LinkedHashMap<String,String> toKeyValueStrings(){
		var kvs = new LinkedHashMap<String,String>();

		kvs.put("sorted", Boolean.toString(sorted()));

		kvs.put("branchBlockType", branchBlockType());
		kvs.put("leafBlockType", leafBlockType());
		kvs.put("valueBlockType", valueBlockType());

		kvs.put("branchBlockCompressor", branchBlockCompressor());
		kvs.put("leafBlockCompressor", leafBlockCompressor());
		kvs.put("valueBlockCompressor", valueBlockCompressor());

		kvs.put("branchBytesPerFile", NumberFormatter.addCommas(branchBytesPerFile()));
		kvs.put("leafBytesPerFile", NumberFormatter.addCommas(leafBytesPerFile()));
		kvs.put("valueBytesPerFile", NumberFormatter.addCommas(valueBytesPerFile()));

		kvs.put("branchBlocksPerFile", NumberFormatter.addCommas(branchBlocksPerFile()));
		kvs.put("leafBlocksPerFile", NumberFormatter.addCommas(leafBlocksPerFile()));
		kvs.put("valueBlocksPerFile", NumberFormatter.addCommas(valueBlocksPerFile()));

		kvs.put("numEntries", NumberFormatter.addCommas(numRecords()));
		kvs.put("numBranchLevels", NumberFormatter.addCommas(numBranchLevels()));

		kvs.put("numBranchBlocks", NumberFormatter.addCommas(numBranchBlocks()));
		kvs.put("numLeafBlocks", NumberFormatter.addCommas(numLeafBlocks()));
		kvs.put("numValueBlocks", NumberFormatter.addCommas(numValueBlocks()));

		kvs.put("numBranchBytesEncoded", NumberFormatter.addCommas(numBranchBytesEncoded()));
		kvs.put("numLeafBytesEncoded", NumberFormatter.addCommas(numLeafBytesEncoded()));
		kvs.put("numValueBytesEncoded", NumberFormatter.addCommas(numValueBytesEncoded()));

		kvs.put("numBranchBytesCompressed", NumberFormatter.addCommas(numBranchBytesCompressed()));
		kvs.put("numLeafBytesCompressed", NumberFormatter.addCommas(numLeafBytesCompressed()));
		kvs.put("numValueBytesCompressed", NumberFormatter.addCommas(numValueBytesCompressed()));

		kvs.put("writeStartTime", Instant.ofEpochMilli(writeStartTimeMs()).toString());
		kvs.put("writeDuration", writeDuration().toString());
		kvs.put("writeItemsPerSecond", perSecond(numRecords(), writeDuration()));
		kvs.put("writeEncodedBytesPerSecond", perSecond(totalBytesEncoded(), writeDuration()));
		kvs.put("writeCompressedBytesPerSecond", perSecond(totalBytesCompressed(), writeDuration()));

		kvs.put("numDictionaryEntries", NumberFormatter.addCommas(numDictionaryEntries()));

		return kvs;
	}

	private static String perSecond(long count, Duration duration){
		double perSecond = (double)count * 1000 / duration.toMillis();
		return NumberFormatter.addCommas((long)perSecond);
	}

}
