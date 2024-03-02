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
package io.datarouter.bytes.blockfile.test;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.blockfile.BlockfileGroup;
import io.datarouter.bytes.blockfile.BlockfileGroupBuilder;
import io.datarouter.bytes.blockfile.io.read.BlockfileReader;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileRowKeyReader;
import io.datarouter.bytes.blockfile.io.storage.BlockfileStorage;
import io.datarouter.bytes.blockfile.io.storage.impl.BlockfileLocalStorage;
import io.datarouter.bytes.blockfile.io.write.BlockfileWriter;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.bytes.blockfile.row.BlockfileRowCollator.BlockfileRowCollatorPruneDeletesScanner;
import io.datarouter.bytes.blockfile.row.BlockfileRowOp;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.scanner.Scanner;

public class BlockfileVersionTests{

	/*-------- static ---------*/

	private static final StringCodec STRING_CODEC = StringCodec.UTF_8;
	private static final BlockfileStorage STORAGE = new BlockfileLocalStorage("/tmp/datarouterTest/blockfile/version/");
	private static final String BLOCK_FILENAME = "block";
	// We alternate between 1 and 2 rows per id, so blockSize=10 will split ids between block boundaries.
	private static final int BLOCK_SIZE = 10;
	private static final int INDEX_FAN_OUT = 10;
	private static final int NUM_IDS = 100;// Should generate 150 rows.
	private static final int NUM_VALUE_BLOCKS = 15;

	private record TestDto(
			String key,
			String version,
			String value){

		static final Codec<TestDto,BlockfileRow> BLOCKFILE_ROW_CODEC = Codec.of(
				dto -> BlockfileRow.create(
						STRING_CODEC.encode(dto.key),
						STRING_CODEC.encode(dto.version),
						BlockfileRowOp.PUT,
						STRING_CODEC.encode(dto.value)),
				row -> new TestDto(
						STRING_CODEC.decode(row.copyOfKey()),
						STRING_CODEC.decode(row.copyOfVersion()),
						STRING_CODEC.decode(row.copyOfValue())));
	}

	private static final List<BlockfileRow> ALL_ROWS = Scanner.iterate(0, i -> i + 1)
			.limit(NUM_IDS)
			.concatIter(BlockfileVersionTests::makeRows)
			.list();

	private static final List<BlockfileRow> EVEN_ROWS = Scanner.iterate(0, i -> i + 1)
			.limit(NUM_IDS)
			.include(id -> id % 2 == 0)
			.concatIter(BlockfileVersionTests::makeRows)
			.list();

	/*---------- fields -----------*/

	private final BlockfileGroup<BlockfileRow> rawBlockfileGroup = new BlockfileGroupBuilder<BlockfileRow>(STORAGE)
			.build();
	private final BlockfileWriter<BlockfileRow> writer = rawBlockfileGroup.newWriterBuilder(BLOCK_FILENAME)
			.setIndexFanOut(INDEX_FAN_OUT)
			.build();
	private final BlockfileReader<BlockfileRow> reader = rawBlockfileGroup.newReaderBuilder(
			BLOCK_FILENAME,
			Function.identity())
			.build();

	@BeforeClass
	private void beforeClass(){
		Scanner.of(ALL_ROWS)
				.batch(BLOCK_SIZE)
				.apply(writer::writeBlocks);
	}

	@Test
	private void testNumValueBlocks(){
		Assert.assertEquals(reader.metadata().footer().numValueBlocks(), NUM_VALUE_BLOCKS);
		Assert.assertEquals(reader.sequential().scanDecodedBlocks().count(), NUM_VALUE_BLOCKS);
	}

	@Test
	private void testScanWithDeletes(){
		List<BlockfileRow> decoded = reader.sequential().scan()
				.list();
		Assert.assertEquals(decoded, ALL_ROWS);
	}

	@Test
	private void testScanPruned(){
		List<BlockfileRow> decoded = reader.sequential().scan()
				.link(BlockfileRowCollatorPruneDeletesScanner::new)
				.list();
		Assert.assertEquals(decoded, EVEN_ROWS);
	}

	@Test
	private void testSearchRowId(){
		// valueBlockId=0
		Assert.assertEquals(reader.rowId().row(0), makePutRow(0));
		Assert.assertEquals(reader.rowId().row(1), makePutRow(1));
		Assert.assertEquals(reader.rowId().row(2), makeDeleteRow(1));
		Assert.assertEquals(reader.rowId().row(3), makePutRow(2));
		Assert.assertEquals(reader.rowId().row(4), makePutRow(3));
		Assert.assertEquals(reader.rowId().row(5), makeDeleteRow(3));
		Assert.assertEquals(reader.rowId().row(6), makePutRow(4));
		Assert.assertEquals(reader.rowId().row(7), makePutRow(5));
		Assert.assertEquals(reader.rowId().row(8), makeDeleteRow(5));
		Assert.assertEquals(reader.rowId().row(9), makePutRow(6));
		// valueBlockId=1
		Assert.assertEquals(reader.rowId().row(10), makePutRow(7));
		Assert.assertEquals(reader.rowId().row(11), makeDeleteRow(7));
		Assert.assertEquals(reader.rowId().row(19), makePutRow(13));
		// valueBlockId=2
		Assert.assertEquals(reader.rowId().row(20), makeDeleteRow(13));
		Assert.assertEquals(reader.rowId().row(28), makePutRow(19));
		Assert.assertEquals(reader.rowId().row(29), makeDeleteRow(19));
		// valueBlockId=14
		Assert.assertEquals(reader.rowId().row(149), makeDeleteRow(99));
	}

	@Test
	private void testSearchValueBlockForRowKey(){
		BlockfileRowKeyReader<BlockfileRow> rowKeyReader = reader.rowKey();

		// valueBlockId=0
		Assert.assertEquals(
				rowKeyReader.lastValueBlockSpanningRowKey(makePutRow(0).copyOfKey()).orElseThrow().valueBlockId(),
				0);
		Assert.assertEquals(
				rowKeyReader.lastValueBlockSpanningRowKey(makePutRow(1).copyOfKey()).orElseThrow().valueBlockId(),
				0);
		Assert.assertEquals(
				rowKeyReader.valueBlockContainingRowKey(makePutRow(1).copyOfKey()),
				Optional.empty());
		Assert.assertEquals(
				rowKeyReader.lastValueBlockSpanningRowKey(makePutRow(6).copyOfKey()).orElseThrow().valueBlockId(),
				0);

		// valueBlockId=1
		Assert.assertEquals(
				rowKeyReader.lastValueBlockSpanningRowKey(makePutRow(7).copyOfKey()).orElseThrow().valueBlockId(),
				1);
		Assert.assertEquals(
				rowKeyReader.valueBlockContainingRowKey(makePutRow(7).copyOfKey()),
				Optional.empty());

		// valueBlockId=2
		Assert.assertEquals(
				rowKeyReader.lastValueBlockSpanningRowKey(makePutRow(18).copyOfKey()).orElseThrow().valueBlockId(),
				2);
		Assert.assertEquals(
				rowKeyReader.lastValueBlockSpanningRowKey(makePutRow(19).copyOfKey()).orElseThrow().valueBlockId(),
				2);
		Assert.assertEquals(
				rowKeyReader.valueBlockContainingRowKey(makePutRow(19).copyOfKey()),
				Optional.empty());

		// valueBlockId=14
		Assert.assertEquals(
				rowKeyReader.valueBlockContainingRowKey(makePutRow(98).copyOfKey()).orElseThrow().valueBlockId(),
				14);
		Assert.assertEquals(
				rowKeyReader.lastValueBlockSpanningRowKey(makePutRow(99).copyOfKey()).orElseThrow().valueBlockId(),
				14);
		Assert.assertEquals(
				rowKeyReader.valueBlockContainingRowKey(makePutRow(99).copyOfKey()),
				Optional.empty());
	}

	@Test
	private void testSearchRowKey(){
		BlockfileRowKeyReader<BlockfileRow> rowKeyReader = reader.rowKey();

		// Only evens exist
		List<Integer> presentDtoIds = List.of(0, 2, 54, 98);
		for(int id : presentDtoIds){
			Assert.assertEquals(
					rowKeyReader.row(makePutRow(id).copyOfKey()).orElseThrow(),
					makePutRow(id));
		}

		List<Integer> missingDtoIds = List.of(1, 3, 55, 99);
		for(int id : missingDtoIds){
			byte[] key = makePutRow(id).copyOfKey();
			Assert.assertFalse(rowKeyReader.row(key).isPresent());
		}
	}

	/*------------ helper ------------*/

	private static BlockfileRow makePutRow(long rowId){
		return TestDto.BLOCKFILE_ROW_CODEC.encode(makeDto(rowId));
	}

	private static BlockfileRow makeDeleteRow(long rowId){
		return BlockfileRow.delete(
				STRING_CODEC.encode(makeKey(rowId)),
				STRING_CODEC.encode(makeVersion(2)));// PUTs are version=1, DELETEs are version=2
	}

	private static List<BlockfileRow> makeRows(long rowId){
		// Odds are deleted
		boolean isEven = rowId % 2 == 0;
		return isEven
				? List.of(
						makePutRow(rowId))
				: List.of(
						makePutRow(rowId),
						makeDeleteRow(rowId));
	}

	private static TestDto makeDto(long rowId){
		return new TestDto(
				makeKey(rowId),
				makeVersion(1),
				makeValue(rowId));
	}

	private static String makeKey(long rowId){
		return longToPaddedString(rowId, 5);
	}

	private static String makeVersion(long version){
		return longToPaddedString(version, 1);
	}

	private static String makeValue(long rowId){
		return longToPaddedString(rowId, 5);
	}

	private static String longToPaddedString(long value, int desiredLength){
		String unpadded = Long.toString(value);
		int paddingLength = desiredLength - unpadded.length();
		return "0".repeat(paddingLength) + unpadded;
	}

}
