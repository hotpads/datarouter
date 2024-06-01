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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.blockfile.BlockfileGroup;
import io.datarouter.bytes.blockfile.BlockfileGroupBuilder;
import io.datarouter.bytes.blockfile.index.BlockfileIndexEntryRange;
import io.datarouter.bytes.blockfile.io.read.BlockfileReader;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileRowKeyRangeReader;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileRowKeyRangeReader.BlockfileKeyRange;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileRowKeyReader;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileRowKeyReader.BlockfileRowKeySearchResult;
import io.datarouter.bytes.blockfile.io.storage.BlockfileStorage;
import io.datarouter.bytes.blockfile.io.storage.impl.BlockfileLocalStorage;
import io.datarouter.bytes.blockfile.io.write.BlockfileWriter;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.bytes.blockfile.row.BlockfileRowOp;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.scanner.Scanner;

public class BlockfileIndexTests{

	/*-------- static ---------*/

	private static final StringCodec STRING_CODEC = StringCodec.UTF_8;
	private static final BlockfileStorage STORAGE = new BlockfileLocalStorage("/tmp/datarouterTest/blockfile/index/");
	private static final String BLOCK_FILENAME = "block";
	private static final int BLOCK_SIZE = 10;
	private static final int NUM_VALUE_BLOCKS = 1_000;
	private static final int INDEX_FAN_OUT = 10;

	private record TestDto(
			String key,
			String version,
			BlockfileRowOp op,
			String value){

		static final Codec<TestDto,BlockfileRow> BLOCKFILE_ROW_CODEC = Codec.of(
				dto -> BlockfileRow.create(
						STRING_CODEC.encode(dto.key),
						STRING_CODEC.encode(dto.version),
						dto.op,
						STRING_CODEC.encode(dto.value)),
				row -> new TestDto(
						STRING_CODEC.decode(row.copyOfKey()),
						STRING_CODEC.decode(row.copyOfVersion()),
						row.op(),
						STRING_CODEC.decode(row.copyOfValue())));
	}

	private static final List<TestDto> DTOS = Scanner.iterate(0, i -> i + 2)
			.limit(NUM_VALUE_BLOCKS * BLOCK_SIZE)
			.map(BlockfileIndexTests::makeDto)
			.list();

	/*---------- fields -----------*/

	private final BlockfileGroup<TestDto> blockfileGroup = new BlockfileGroupBuilder<TestDto>(STORAGE)
			.build();
	private final BlockfileWriter<TestDto> writer = blockfileGroup.newWriterBuilder(BLOCK_FILENAME)
			.setIndexFanOut(INDEX_FAN_OUT)
			.build();
	private final BlockfileReader<TestDto> reader = blockfileGroup.newReaderBuilder(
			BLOCK_FILENAME,
			TestDto.BLOCKFILE_ROW_CODEC::decode)
			.build();

	@BeforeClass
	private void beforeClass(){
		Scanner.of(DTOS)
				.map(TestDto.BLOCKFILE_ROW_CODEC::encode)
				.batch(BLOCK_SIZE)
				.apply(writer::writeBlocks);
	}

	@Test
	private void testScan(){
		List<TestDto> decoded = reader.sequential().scan().list();
		Assert.assertEquals(decoded, DTOS);
		Assert.assertEquals(reader.metadata().footer().numValueBlocks(), NUM_VALUE_BLOCKS);
	}

	@Test
	private void testSearchValueBlockId(){
		var result1 = reader.blockId().valueBlockId(0);
		Assert.assertEquals(result1.globalBlockId(), 1);//first block after header
		Assert.assertEquals(result1.valueBlockId(), 0);
		Assert.assertEquals(result1.firstItemId(), 0);

		var result2 = reader.blockId().valueBlockId(3);
		Assert.assertEquals(result2.globalBlockId(), 4);
		Assert.assertEquals(result2.valueBlockId(), 3);
		Assert.assertEquals(result2.firstItemId(), 30);

		var result3 = reader.blockId().valueBlockId(13);
		// after: headerBlock, 10 valueBlocks, indexBlock, 3 valueBlocks
		Assert.assertEquals(result3.globalBlockId(), 15);
		Assert.assertEquals(result3.firstItemId(), 130);
	}

	@Test
	private void testSearchValueBlockWithRowId(){
		var result1 = reader.rowId().valueBlockWithRowId(0);
		Assert.assertEquals(result1.globalBlockId(), 1);//first block after header
		Assert.assertEquals(result1.valueBlockId(), 0);
		Assert.assertEquals(result1.firstItemId(), 0);

		var result2 = reader.rowId().valueBlockWithRowId(15);
		Assert.assertEquals(result2.globalBlockId(), 2);//first block after header
		Assert.assertEquals(result2.valueBlockId(), 1);
		Assert.assertEquals(result2.firstItemId(), 10);
		//TODO extract row and test

		var result3 = reader.rowId().valueBlockWithRowId(235);
		Assert.assertEquals(result3.globalBlockId(), 26);//first block after header
		Assert.assertEquals(result3.valueBlockId(), 23);
		Assert.assertEquals(result3.firstItemId(), 230);
		//TODO extract row and test
	}

	@Test
	private void testSearchItemId(){
		Assert.assertEquals(reader.rowId().item(0), makeDto(0));
		Assert.assertEquals(reader.rowId().item(1), makeDto(2));
		Assert.assertEquals(reader.rowId().item(55), makeDto(110));
		Assert.assertEquals(reader.rowId().item(311), makeDto(622));
	}

	@Test
	private void testSearchValueBlockWithRowKey(){
		byte[] key1 = STRING_CODEC.encode(makeKey(0));
		Optional<BlockfileRowKeySearchResult<TestDto>> result1 = reader.rowKey().lastValueBlockSpanningRowKey(key1);
		Assert.assertTrue(result1.isPresent());
		Assert.assertEquals(result1.orElseThrow().globalBlockId(), 1);//first block after header
		Assert.assertEquals(result1.orElseThrow().firstItemId(), 0);
		//TODO extract row and test

		byte[] key2 = STRING_CODEC.encode(makeKey(45));// key shouldn't exist, but block should
		Optional<BlockfileRowKeySearchResult<TestDto>> result2 = reader.rowKey().lastValueBlockSpanningRowKey(key2);
		Assert.assertTrue(result2.isPresent());
		Assert.assertEquals(result2.orElseThrow().globalBlockId(), 3);
		Assert.assertEquals(result2.orElseThrow().valueBlockId(), 2);
		Assert.assertEquals(result2.orElseThrow().firstItemId(), 20);
		//TODO extract row and test
	}

	@Test
	private void testSearchRowKey(){
		BlockfileRowKeyReader<TestDto> rowKeyReader = reader.rowKey();

		// Only evens exist
		List<Integer> presentDtoIds = List.of(0, 2, 54, 308);
		for(int dtoId : presentDtoIds){
			Assert.assertEquals(
					rowKeyReader.item(makeRow(dtoId).copyOfKey()).orElseThrow(),
					makeDto(dtoId));
		}

		List<Integer> missingDtoIds = List.of(1, 3, 55, 309);
		for(int dtoId : missingDtoIds){
			Assert.assertFalse(rowKeyReader.item(makeRow(dtoId).copyOfKey()).isPresent());
		}
	}

	@Test
	private void testSearchRowKeyRangeFirstBlock(){
		BlockfileRowKeyRangeReader<TestDto> rangeReader = reader.rowKeyRange();

		BlockfileIndexEntryRange indexEntryRange0 = rangeReader.indexEntryRange(makeKeyRange(1, true, 3, true));
		Assert.assertEquals(indexEntryRange0.first().childGlobalBlockId(), 1);
		Assert.assertEquals(indexEntryRange0.first().childIndexOrValueBlockId(), 0);
		Assert.assertEquals(indexEntryRange0.last().childIndexOrValueBlockId(), 0);

		Assert.assertEquals(
				rangeReader.scanRange(makeKeyRange(0, true, 0, true)).list(),
				List.of(makeDto(0)));
		Assert.assertEquals(
				rangeReader.scanRange(makeKeyRange(0, false, 1, true)).list(),
				List.of());
		Assert.assertEquals(
				rangeReader.scanRange(makeKeyRange(1, true, 4, true)).list(),
				List.of(makeDto(2), makeDto(4)));
	}

	@Test
	private void testSearchRowKeyRangeMultiBlock(){
		BlockfileRowKeyRangeReader<TestDto> rangeReader = reader.rowKeyRange();

		BlockfileIndexEntryRange indexEntryRange1 = rangeReader.indexEntryRange(makeKeyRange(17, true, 22, true));
		Assert.assertEquals(indexEntryRange1.first().childGlobalBlockId(), 1);
		Assert.assertEquals(indexEntryRange1.first().childIndexOrValueBlockId(), 0);
		Assert.assertEquals(indexEntryRange1.last().childIndexOrValueBlockId(), 1);
		Assert.assertEquals(
				rangeReader.scanRange(makeKeyRange(17, true, 22, true)).list(),
				List.of(makeDto(18), makeDto(20), makeDto(22)));
	}

	@Test
	private void testSearchRowKeyRangeBlockBoundary(){
		BlockfileRowKeyRangeReader<TestDto> rangeReader = reader.rowKeyRange();

		Assert.assertEquals(
				rangeReader.scanRange(makeKeyRange(18, false, 20, true)).list(),
				List.of(makeDto(20)));
		Assert.assertEquals(
				rangeReader.scanRange(makeKeyRange(19, true, 20, true)).list(),
				List.of(makeDto(20)));
		Assert.assertEquals(
				rangeReader.scanRange(makeKeyRange(20, true, 22, true)).list(),
				List.of(makeDto(20), makeDto(22)));
	}

	@Test
	private void testSearchRowKeyRangeOpenStart(){
		BlockfileRowKeyRangeReader<TestDto> rangeReader = reader.rowKeyRange();

		Assert.assertEquals(
				rangeReader.scanRange(new BlockfileKeyRange(
						Optional.empty(),
						true,
						Optional.of(makeRow(4).copyOfKey()),
						true))
						.list(),
				List.of(makeDto(0), makeDto(2), makeDto(4)));
	}

	@Test
	private void testSearchRowKeyRangeOpenEnd(){
		BlockfileRowKeyRangeReader<TestDto> rangeReader = reader.rowKeyRange();

		Assert.assertEquals(
				rangeReader.scanRange(new BlockfileKeyRange(
						Optional.of(makeRow(19_994).copyOfKey()),
						false,
						Optional.empty(),
						true))
						.list(),
				List.of(makeDto(19_996), makeDto(19_998)));
	}

	/*------------ helper ------------*/

	private static BlockfileRow makeRow(long rowId){
		return TestDto.BLOCKFILE_ROW_CODEC.encode(makeDto(rowId));
	}

	private static TestDto makeDto(long rowId){
		return new TestDto(
				makeKey(rowId),
				makeVersion(rowId),
				BlockfileRowOp.PUT,
				makeValue(rowId));
	}

	private static String makeKey(long rowId){
		return longToPaddedString(rowId);
	}

	private static String makeVersion(long version){
		return longToPaddedString(version);
	}

	private static String makeValue(long rowId){
		return longToPaddedString(rowId).repeat(2);
	}

	private static BlockfileKeyRange makeKeyRange(
			long fromRowId,
			boolean fromInclusive,
			long toRowId,
			boolean toInclusive){
		return new BlockfileKeyRange(
				Optional.of(makeRow(fromRowId).copyOfKey()),
				fromInclusive,
				Optional.of(makeRow(toRowId).copyOfKey()),
				toInclusive);
	}

	private static String longToPaddedString(long value){
		int desiredLength = 10;
		String unpadded = Long.toString(value);
		int paddingLength = desiredLength - unpadded.length();
		return "0".repeat(paddingLength) + unpadded;
	}

}
