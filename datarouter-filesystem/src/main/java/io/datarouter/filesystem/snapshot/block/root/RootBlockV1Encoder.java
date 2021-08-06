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
import java.util.SortedMap;
import java.util.TreeMap;

import io.datarouter.filesystem.snapshot.encode.EncodedBlock;
import io.datarouter.filesystem.snapshot.encode.RootBlockEncoder;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields;
import io.datarouter.util.bytes.ByteTool;
import io.datarouter.util.bytes.ByteWriter;

public class RootBlockV1Encoder implements RootBlockEncoder{

	private RootBlockFields fields;

	private SortedMap<byte[],byte[]> dictionary;

	@Override
	public void set(RootBlockFields fields){
		this.fields = fields;
		this.dictionary = new TreeMap<>((a, b) -> Arrays.compareUnsigned(a, b));
	}

	public void addDictionaryEntry(byte[] key, byte[] value){
		byte[] previousKey = dictionary.put(key, value);
		if(previousKey != null){
			throw new IllegalArgumentException("duplicate key " + ByteTool.getIntString(key));
		}
	}

	@Override
	public EncodedBlock encode(){
		var writer = new ByteWriter(256);

		writer.varUtf8(format());
		writer.booleanByte(fields.sorted);
		writer.varUtf8(fields.paths.format());
		writer.varUtf8(fields.formats.branchBlockFormat);
		writer.varUtf8(fields.formats.leafBlockFormat);
		writer.varUtf8(fields.formats.valueBlockFormat);

		writer.varUtf8(fields.compressors.branchBlockCompressor);
		writer.varUtf8(fields.compressors.leafBlockCompressor);
		writer.varUtf8(fields.compressors.valueBlockCompressor);

		writer.varInt(fields.bytesPerFile.branchBytesPerFile);
		writer.varInt(fields.bytesPerFile.leafBytesPerFile);
		writer.varInt(fields.bytesPerFile.valueBytesPerFile);

		writer.varInt(fields.blocksPerFile.branchBlocksPerFile);
		writer.varInt(fields.blocksPerFile.leafBlocksPerFile);
		writer.varInt(fields.blocksPerFile.valueBlocksPerFile);

		writer.varLong(fields.numEntries);

		writer.varInt(fields.numBranchLevels);
		Arrays.stream(fields.blockCounts.numBranchBlocksByLevel)
				.forEach(writer::varInt);

		writer.varInt(fields.blockCounts.numLeafBlocks);

		writer.varInt(fields.blockCounts.numValueBlocksByColumn.length);
		Arrays.stream(fields.blockCounts.numValueBlocksByColumn)
				.forEach(writer::varInt);

		writer.varLong(fields.byteCountsEncoded.numBranchBytesEncoded);
		writer.varLong(fields.byteCountsEncoded.numLeafBytesEncoded);
		writer.varLong(fields.byteCountsEncoded.numValueBytesEncoded);

		writer.varLong(fields.byteCountsCompressed.numBranchBytesCompressed);
		writer.varLong(fields.byteCountsCompressed.numLeafBytesCompressed);
		writer.varLong(fields.byteCountsCompressed.numValueBytesCompressed);

		writer.varLong(fields.timings.writeStartTimeMs);
		writer.varLong(fields.timings.writeDurationMs);

		writer.varInt(fields.blockEndings.rootBranchBlockLength);

		//dictionary
		writer.varInt(dictionary.size());
		dictionary.forEach((key, value) -> {
			writer.varBytes(key);
			writer.varBytes(value);
		});

		return new EncodedBlock(writer.trimmedPages());
	}

	@Override
	public String format(){
		return RootBlockV1.FORMAT;
	}

}
