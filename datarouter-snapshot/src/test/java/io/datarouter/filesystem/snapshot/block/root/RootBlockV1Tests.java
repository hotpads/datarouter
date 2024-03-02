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

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.filesystem.snapshot.block.branch.BranchBlockV1;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlockV1;
import io.datarouter.filesystem.snapshot.block.value.ValueBlockV1;
import io.datarouter.filesystem.snapshot.compress.PassthroughBlockCompressor;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.RootBlockEncoderBlockCounts;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.RootBlockEncoderBlockEndings;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.RootBlockEncoderBlocksPerFile;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.RootBlockEncoderByteCountsCompressed;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.RootBlockEncoderByteCountsEncoded;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.RootBlockEncoderBytesPerFile;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.RootBlockEncoderCompressors;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.RootBlockEncoderFormats;
import io.datarouter.filesystem.snapshot.encode.RootBlockFields.RootBlockEncoderTimings;
import io.datarouter.filesystem.snapshot.path.SnapshotPathsV1;

public class RootBlockV1Tests{

	@Test
	public void testEncoding(){
		var encoder = new RootBlockV1Encoder();
		var rootBlockFields = new RootBlockFields(
				true,//sorted
				new SnapshotPathsV1(),
				new RootBlockEncoderFormats(
						BranchBlockV1.FORMAT,
						LeafBlockV1.FORMAT,
						ValueBlockV1.FORMAT),
				new RootBlockEncoderCompressors(
						PassthroughBlockCompressor.NAME,
						PassthroughBlockCompressor.NAME,
						PassthroughBlockCompressor.NAME),
				new RootBlockEncoderBytesPerFile(
						1000,
						2000,
						3000),
				new RootBlockEncoderBlocksPerFile(
						2,
						2,
						2),
				2,//numEntries
				2,//numIndexLevels
				new RootBlockEncoderBlockCounts(
						new int[]{2, 1},//numIndexBlocksByLevel
						2,//numLeafBlocks
						new int[]{2, 2}),//numValueBlocksByColumn
				new RootBlockEncoderByteCountsEncoded(
						123,//numBranchBytesEncoded
						124,//numLeafBytesEncoded
						125),//numValueBytesEncoded
				new RootBlockEncoderByteCountsCompressed(
						256,//numBranchBytesCompressed
						257,//numLeafBytesCompressed
						258),//numValueBytesCompressed
				new RootBlockEncoderBlockEndings(
						3),
				new RootBlockEncoderTimings(
						10,
						20));
		encoder.set(rootBlockFields);
		encoder.addDictionaryEntry("dictC".getBytes(), "C".getBytes());// unordered to test sort-on-encode
		encoder.addDictionaryEntry("dictA".getBytes(), "A".getBytes());
		encoder.addDictionaryEntry("dictB".getBytes(), "B".getBytes());
		byte[] bytes = encoder.encode().concat();

		var block = new RootBlockV1(bytes);

		//paths
		Assert.assertEquals(block.pathFormat(), SnapshotPathsV1.FORMAT);

		//encoders
		Assert.assertEquals(block.branchBlockType(), BranchBlockV1.FORMAT);
		Assert.assertEquals(block.leafBlockType(), LeafBlockV1.FORMAT);
		Assert.assertEquals(block.valueBlockType(), ValueBlockV1.FORMAT);

		//compressors
		Assert.assertEquals(block.branchBlockCompressor(), PassthroughBlockCompressor.NAME);
		Assert.assertEquals(block.leafBlockCompressor(), PassthroughBlockCompressor.NAME);
		Assert.assertEquals(block.valueBlockCompressor(), PassthroughBlockCompressor.NAME);

		//bytesPerFile
		Assert.assertEquals(block.branchBytesPerFile(), 1000);
		Assert.assertEquals(block.leafBytesPerFile(), 2000);
		Assert.assertEquals(block.valueBytesPerFile(), 3000);

		//blocksPerFile
		Assert.assertEquals(block.branchBlocksPerFile(), 2);
		Assert.assertEquals(block.leafBlocksPerFile(), 2);
		Assert.assertEquals(block.valueBlocksPerFile(), 2);

		//counts
		Assert.assertEquals(block.numItems(), 2);

		Assert.assertEquals(block.numBranchLevels(), 2);
		Assert.assertEquals(block.maxBranchLevel(), 1);
		Assert.assertEquals(block.numBranchBlocks(), 3);

		Assert.assertEquals(block.numLeafBlocks(), 2);

		Assert.assertEquals(block.numColumns(), 2);
		Assert.assertEquals(block.numValueBlocks(0), 2);
		Assert.assertEquals(block.numValueBlocks(1), 2);
		Assert.assertEquals(block.numValueBlocks(), 4);

		Assert.assertEquals(block.numBranchBytesEncoded(), 123);
		Assert.assertEquals(block.numLeafBytesEncoded(), 124);
		Assert.assertEquals(block.numValueBytesEncoded(), 125);

		Assert.assertEquals(block.numBranchBytesCompressed(), 256);
		Assert.assertEquals(block.numLeafBytesCompressed(), 257);
		Assert.assertEquals(block.numValueBytesCompressed(), 258);

		Assert.assertEquals(block.writeStartTimeMs(), 10);
		Assert.assertEquals(block.writeDurationMs(), 20);
		Assert.assertEquals(block.writeDuration(), Duration.ofMillis(20));

		Assert.assertEquals(block.rootBranchBlockEnding(), 3);

		//dictionary
		Assert.assertEquals(block.dictionaryKey(0), "dictA".getBytes());
		Assert.assertEquals(block.dictionaryValue(0), "A".getBytes());
		Assert.assertEquals(block.dictionaryKey(1), "dictB".getBytes());
		Assert.assertEquals(block.dictionaryValue(1), "B".getBytes());
		Assert.assertEquals(block.dictionaryKey(2), "dictC".getBytes());
		Assert.assertEquals(block.dictionaryValue(2), "C".getBytes());
	}

}
