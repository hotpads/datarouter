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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import io.datarouter.bytes.BinaryDictionary;
import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.HexBlockTool;
import io.datarouter.bytes.RecordByteArrayField;
import io.datarouter.bytes.blockfile.BlockfileGroupBuilder;
import io.datarouter.bytes.blockfile.block.decoded.BlockfileFooterBlock;
import io.datarouter.bytes.blockfile.block.decoded.BlockfileHeaderBlock;
import io.datarouter.bytes.blockfile.block.tokens.BlockfileBaseTokens;
import io.datarouter.bytes.blockfile.index.BlockfileIndexEntry;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileBlockIdReader.BlockfileBlockIdSearchResult;
import io.datarouter.bytes.blockfile.io.read.query.BlockfileRowIdReader.BlockfileRowIdSearchResult;
import io.datarouter.bytes.blockfile.io.storage.BlockfileNameAndSize;
import io.datarouter.bytes.blockfile.io.storage.BlockfileStorage;
import io.datarouter.bytes.blockfile.io.storage.impl.BlockfileLocalStorage;
import io.datarouter.bytes.blockfile.io.write.BlockfileWriter;
import io.datarouter.bytes.blockfile.io.write.BlockfileWriter.BlockfileWriteResult;
import io.datarouter.bytes.blockfile.io.write.listener.impl.BlockfileEndingsListener;
import io.datarouter.bytes.blockfile.io.write.listener.impl.BlockfileTokensListener;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.bytes.blockfile.test.encoding.BlockfileTestChecksummer;
import io.datarouter.bytes.blockfile.test.encoding.BlockfileTestCompressor;
import io.datarouter.bytes.codec.bytestringcodec.HexByteStringCodec;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.bytes.io.InputStreamTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;

public class BlockfileStringTests{
	private static final Logger logger = LoggerFactory.getLogger(BlockfileStringTests.class);

	private static final BlockfileStorage STORAGE = new BlockfileLocalStorage("/tmp/datarouterTest/blockfile/string/");
	private static final String FILENAME = "blockfile";

	private static final String HEADER_DICT_KEY_0 = "usrkey0";
	private static final String HEADER_DICT_VAL_0 = "usrval0";
	private static final BinaryDictionary HEADER_USER_DICT = new BinaryDictionary()
			.put(HEADER_DICT_KEY_0, StringCodec.UTF_8.encode(HEADER_DICT_VAL_0));

	private static final Codec<String,BlockfileRow> ROW_CODEC = Codec.of(
			str -> BlockfileRow.putWithoutVersion(str.getBytes(), EmptyArray.BYTE),
			row -> new String(row.copyOfKey()));

	// these strings will become very small blocks
	private static final List<List<String>> BLOCK_VALUES = List.of(
			List.of("a0", "a1", "a2"),
			List.of("b0", "b1", "b2"),
			List.of("c0", "c1", "c2"),
			List.of("d0", "d1", "d2"),
			List.of("e0", "e1", "e2"),
			List.of("f0", "f1", "f2"),
			List.of("g0", "g1", "g2"));
	private static final List<List<BlockfileRow>> BLOCK_ROWS = Scanner.of(BLOCK_VALUES)
			.map(blockValues -> Scanner.of(blockValues)
					.map(ROW_CODEC::encode)
					.list())
			.list();
	private static final long NUM_VALUE_BLOCKS = BLOCK_VALUES.size();
	private static final long NUM_ROWS = Scanner.of(BLOCK_VALUES)
			.concat(Scanner::of)
			.count();

	private static final int INDEX_FAN_OUT = 3;
	private static final int NUM_INDEX_BLOCKS = 4;// 3 at level=0, 1 at level=1

	private static final String FOOTER_DICT_ENDINGS = "endings";
	private static final Codec<List<Long>,byte[]> ENDINGS_CODEC = Codec.of(
			longs -> longs.stream().map(Number::toString).collect(Collectors.joining(",")).getBytes(),
			bytes -> Scanner.of(new String(bytes).split(",")).stream().map(Long::valueOf).toList());

	private static final List<Threads> REGISTERED_THREADS = new ArrayList<>();
	private static final Threads ENCODE_THREADS = makeThreads(4);
	private static final Threads WRITE_THREADS = makeThreads(4);
	private static final Threads READ_THREADS = makeThreads(4);
	private static final Threads DECODE_THREADS = makeThreads(4);

	@Test
	public void test(){

		/*------- create --------*/

		// listeners
		var tokensListener = new BlockfileTokensListener(blockTokens -> {
			List<String> lines = Scanner.of(blockTokens)
					.map(tokens -> tokens.scan()
							.exclude(ByteTool::isEmpty)
							.map(HexByteStringCodec.INSTANCE::encode)
							.collect(Collectors.joining(" ")))
					.list();
			logger.info("Tokens:{}", lines.stream().collect(Collectors.joining("\n", "\n--- tokens ---\n", "\n---")));
		});
		var endingsListener = new BlockfileEndingsListener(endings -> {
			String csvEndings = Scanner.of(endings)
					.map(Number::toString)
					.collect(Collectors.joining(","));
			logger.info("endings={}", csvEndings);
		});

		// footerUserDictionarySupplier
		// Store the endings in the footer to verify the footer is calculated lazily.
		// Note this is not a built-in capability of Blockfile.
		// Higher layers can store endings as they choose, where this is one option.
		Supplier<BinaryDictionary> footerUserDictionarySupplier = () -> new BinaryDictionary()
				.put(FOOTER_DICT_ENDINGS, ENDINGS_CODEC.encode(endingsListener.allEndings()));

		var blockfileGroup = new BlockfileGroupBuilder<String>(STORAGE)
				.registerCompressor(BlockfileTestCompressor.INSTANCE)
				.registerChecksummer(BlockfileTestChecksummer.INSTANCE)
				.build();
		var writer = blockfileGroup.newWriterBuilder(FILENAME)
				.setHeaderDictionary(HEADER_USER_DICT)
				.setFooterDictionarySupplier(footerUserDictionarySupplier)
				.addListeners(tokensListener, endingsListener)
				.setEncodeBatchSize(2)
				.setEncodeThreads(ENCODE_THREADS)
				.setCompressor(BlockfileTestCompressor.INSTANCE)
				.setChecksummer(BlockfileTestChecksummer.INSTANCE)
				.setWriteThreads(WRITE_THREADS)
				.setIndexFanOut(INDEX_FAN_OUT)
				.build();
		var reader = blockfileGroup.newReaderBuilder(FILENAME, ROW_CODEC::decode)
				.setReadThreads(READ_THREADS)
				.setReadChunkSize(ByteLength.ofKiB(4))
				.setDecodeBatchSize(2)
				.setDecodeThreads(DECODE_THREADS)
				.enableChecksumValidation()
				.build();

		// write
		var blockCounter = new AtomicLong();
		BlockfileWriteResult writeResult = Scanner.of(BLOCK_ROWS)
				.each($ -> blockCounter.incrementAndGet())
				.apply(writer::writeBlocks);

		// file named "blockfile" should exist
		// view in terminal "xxd blockfile"

		/*------- test write --------*/

		// validate result
		long blobLength = STORAGE.length(FILENAME);
		Assert.assertEquals(writeResult.fileLength().toBytes(), blobLength);
		Assert.assertEquals(writeResult.numValueBlocks(), NUM_VALUE_BLOCKS);

		// validate bytes and tokens match and don't change in the future
		InputStream fileInputStream = STORAGE.readInputStream(
				FILENAME,
				reader.config().readThreads(),
				reader.config().readChunkSize());
		byte[] actualBytes = InputStreamTool.toArray(fileInputStream);
		Assert.assertEquals(
				actualBytes,
				Scanner.of(tokensListener.blockTokens())
						.concatIter(BlockfileBaseTokens::toList)
						.listTo(ByteTool::concat));
		HexBlockTool.print(actualBytes);
		String hex = """
				00000084480512434845434b53554d5f414c474f524954484d04544553540a434f4d50524553534f
				52045445535412494e4445585f424c4f434b5f464f524d41540256310f555345525f44494354494f
				4e4152591101077573726b6579300775737276616c301256414c55455f424c4f434b5f464f524d41
				540a53455155454e5449414c0000002056000000173c000003026130000100026131000100026132
				0001003e0000002056000000173c0103030262300001000262310001000262320001003e00000020
				56000000173c0206030263300001000263310001000263320001003e000000ed4904000003000000
				00000000010000000000000000000000000000000000000000000000000000000000000000000000
				00000000020000000000000084000000200000000000000000000000020000000000000001000000
				000000000100000000000000010000000000000003000000000000000500000000000000a4000000
				200000000c0000000000000003000000000000000200000000000000020000000000000002000000
				0000000006000000000000000800000000000000c400000020000000180502613000010502613200
				010502623000010502623200010502633000010502633200010000002056000000173c0309030264
				300001000264310001000264320001003e0000002056000000173c040c0302653000010002653100
				01000265320001003e0000002056000000173c050f03026630000100026631000100026632000100
				3e000000ed4908010003000000000000000500000000000000030000000000000003000000000000
				00030000000000000009000000000000000b00000000000001d10000002000000000000000000000
				0006000000000000000400000000000000040000000000000004000000000000000c000000000000
				000e00000000000001f1000000200000000c00000000000000070000000000000005000000000000
				00050000000000000005000000000000000f00000000000000110000000000000211000000200000
				00180502643000010502643200010502653000010502653200010502663000010502663200010000
				002056000000173c0612030267300001000267310001000267320001003e00000055490a02000100
				00000000000009000000000000000600000000000000060000000000000006000000000000001200
				00000000000014000000000000031e0000002000000000050267300001050267320001000000ed49
				0b030103000000000000000400000000000000000000000000000000000000000000000200000000
				00000000000000000000000800000000000000e4000000ed00000000000000000000000800000000
				00000001000000000000000300000000000000050000000000000009000000000000001100000000
				00000231000000ed0000000c000000000000000a0000000000000002000000000000000600000000
				0000000600000000000000120000000000000014000000000000033e000000550000001805026130
				00010502633200010502643000010502663200010502673000010502673200010000013b46060648
				45414445527f0512434845434b53554d5f414c474f524954484d04544553540a434f4d5052455353
				4f52045445535412494e4445585f424c4f434b5f464f524d41540256310f555345525f4449435449
				4f4e4152591101077573726b6579300775737276616c301256414c55455f424c4f434b5f464f524d
				41540a53455155454e5449414c154845414445525f424c4f434b5f4c4f434154494f4e0300840110
				4e554d5f494e4445585f424c4f434b530104104e554d5f56414c55455f424c4f434b53010719524f
				4f545f494e4445585f424c4f434b5f4c4f434154494f4e049307ed010f555345525f44494354494f
				4e4152593a0107656e64696e6773303133322c3136342c3139362c3232382c3436352c3439372c35
				32392c3536312c3739382c3833302c3931352c313135320000013b""";
		Assert.assertEquals(HexByteStringCodec.INSTANCE.encode(actualBytes), HexBlockTool.trim(hex));
		byte[] hexBytes = HexBlockTool.fromHexBlock(hex);
		Assert.assertEquals(actualBytes, hexBytes);

		// validate endings
		Assert.assertEquals(
				endingsListener.lastEnding().orElseThrow().intValue(),
				tokensListener.scanHeaderAndValueAndIndexTokens()
						.concat(BlockfileBaseTokens::scan)
						.listTo(ByteTool::totalLength));

		/*-------- test storage list -------*/

		List<BlockfileNameAndSize> files = STORAGE.list();
		Assert.assertEquals(files.size(), 1);
		Assert.assertEquals(files.getFirst().name(), FILENAME);
		Assert.assertEquals(files.getFirst().size(), writeResult.fileLength().toBytes());

		/*------- test read --------*/

		// validate headers
		BlockfileHeaderBlock header = reader.metadata().header();
		Assert.assertEquals(
				header.checksummer().numBytes(),
				writer.config().checksummer().numBytes());
		Assert.assertEquals(
				header.checksummer().encodedName(),
				writer.config().checksummer().encodedName());
		Assert.assertEquals(
				header.userDictionary().size(),
				1);
		Assert.assertEquals(
				reader.metadata().header().userDictionary().get(HEADER_DICT_KEY_0),
				StringCodec.UTF_8.encode(HEADER_DICT_VAL_0));

		// validate blocks
		reader.sequential().scanDecodedValues()
				.forEach(value -> logger.info("value={}", value));
		Assert.assertEquals(
				reader.sequential().scanDecodedValues().list(),
				BLOCK_VALUES);

		// validate footer dictionary contents
		Assert.assertEquals(
				reader.metadata().footer().numValueBlocks(),
				NUM_VALUE_BLOCKS);
		Assert.assertEquals(
				reader.metadata().footer().userDictionary().size(),
				1);
		var expectedFooterUserDict = new BinaryDictionary()
				.put(FOOTER_DICT_ENDINGS, ENDINGS_CODEC.encode(endingsListener.allEndings()));
		byte[] actualEndingsBytes = reader.metadata().footer().userDictionary().get(FOOTER_DICT_ENDINGS);
		List<Long> actualDecodedEndings = ENDINGS_CODEC.decode(actualEndingsBytes);
		Assert.assertEquals(actualDecodedEndings, endingsListener.allEndings());

		// validate footer
		Assert.assertEquals(
				reader.metadata().footer().headerBlockLocation(),
				writer.state().headerBlockLocation());
		var expectedFooter = new BlockfileFooterBlock(
				new RecordByteArrayField(writer.config().headerCodec().encode(header)),
				expectedFooterUserDict,
				writer.state().headerBlockLocation(),
				writer.state().latestIndexBlockLocation(),
				NUM_VALUE_BLOCKS,
				NUM_INDEX_BLOCKS);
		Assert.assertEquals(reader.metadata().footer(), expectedFooter);
		byte[] footerValueBytes = BlockfileFooterBlock.VALUE_CODEC.encode(expectedFooter);
		byte[] expectedFooterBytes = BlockfileWriter.encodeFooter(footerValueBytes).concat();
		Assert.assertEquals(
				reader.metadata().footerBlockLength(),
				expectedFooterBytes.length);

		// validate rootIndex
		Assert.assertEquals(reader.metadata().rootIndex().level(), 1);
		Assert.assertEquals(reader.metadata().rootIndex().numChildren(), 3);
		Assert.assertEquals(reader.metadata().rootIndex().indexBlockId(), NUM_INDEX_BLOCKS - 1);
		Assert.assertEquals(reader.index().numRows(), NUM_ROWS);

		// search indexEntries
		List<BlockfileIndexEntry> leafIndexEntries = reader.index().scanLeafIndexEntries().list();
		Assert.assertEquals(leafIndexEntries.size(), NUM_VALUE_BLOCKS);
		for(int i = 0; i < leafIndexEntries.size(); ++i){
			Assert.assertEquals(
					leafIndexEntries.get(i).rowIdRange().numRows(),
					BLOCK_VALUES.get(i).size());
		}

		// search valueBlockId
		BlockfileBlockIdSearchResult<String> blockIdSearchResult = reader.blockId().valueBlockId(0);
		Assert.assertEquals(blockIdSearchResult.globalBlockId(), 1);
		Assert.assertEquals(blockIdSearchResult.valueBlockId(), 0);

		// search rowId
		BlockfileRowIdSearchResult<String> rowIdSearchResult = reader.rowId().valueBlockWithRowId(0);
		Assert.assertEquals(rowIdSearchResult.globalBlockId(), 1);
		Assert.assertEquals(rowIdSearchResult.valueBlockId(), 0);
		Assert.assertEquals(rowIdSearchResult.firstItemId(), 0);

		// search itemId
		String rowIdItemSearchResult = reader.rowId().item(4);
		Assert.assertEquals(rowIdItemSearchResult, "b1");

		// search rowKey
		String rowKeyItemSearchResult = reader.rowKey().item(ROW_CODEC.encode("b1").copyOfKey()).orElseThrow();
		Assert.assertEquals(rowKeyItemSearchResult, "b1");
	}

	@AfterClass
	public void afterClass(){
		Scanner.of(REGISTERED_THREADS)
				.map(Threads::exec)
				.forEach(ExecutorService::shutdown);
	}

	private static Threads makeThreads(int count){
		var threads = new Threads(Executors.newFixedThreadPool(count), count);
		REGISTERED_THREADS.add(threads);
		return threads;
	}

}
