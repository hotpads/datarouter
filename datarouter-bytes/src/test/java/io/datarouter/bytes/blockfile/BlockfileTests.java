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
package io.datarouter.bytes.blockfile;

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
import io.datarouter.bytes.HexBlockTool;
import io.datarouter.bytes.blockfile.dto.BlockfileNameAndSize;
import io.datarouter.bytes.blockfile.dto.BlockfileTokens;
import io.datarouter.bytes.blockfile.listener.BlockfileEndingsListener;
import io.datarouter.bytes.blockfile.listener.BlockfileTokensListener;
import io.datarouter.bytes.blockfile.section.BlockfileFooter;
import io.datarouter.bytes.blockfile.storage.BlockfileLocalStorage;
import io.datarouter.bytes.blockfile.storage.BlockfileStorage;
import io.datarouter.bytes.blockfile.write.BlockfileWriter;
import io.datarouter.bytes.blockfile.write.BlockfileWriter.BlockfileWriteResult;
import io.datarouter.bytes.codec.bytestringcodec.HexByteStringCodec;
import io.datarouter.bytes.codec.list.longlist.RawLongListCodec;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.bytes.io.InputStreamTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;

public class BlockfileTests{
	private static final Logger logger = LoggerFactory.getLogger(BlockfileTests.class);

	private static final BlockfileStorage STORAGE = new BlockfileLocalStorage("/tmp/datarouterTest/blockfile/");
	private static final String FILENAME = "blockfile";

	private static final String HEADER_DICT_KEY_0 = "usrkey0";
	private static final String HEADER_DICT_VAL_0 = "usrval0";
	private static final BinaryDictionary HEADER_USER_DICT = new BinaryDictionary()
			.put(HEADER_DICT_KEY_0, StringCodec.UTF_8.encode(HEADER_DICT_VAL_0));

	// these strings will become very small blocks
	private static final List<String> BLOCK_STRINGS = List.of("This", "is", "a", "blockfile");
	private static final long BLOCK_COUNT = BLOCK_STRINGS.size();

	private static final String FOOTER_DICT_ENDINGS = "endings";
	private static final Codec<List<Long>,byte[]> ENDINGS_CODEC = RawLongListCodec.INSTANCE;

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

		var blockfile = new BlockfileBuilder<String>(STORAGE)
				.registerCompressor(BlockfileTestCompressor.INSTANCE)
				.registerChecksummer(BlockfileTestChecksummer.INSTANCE)
				.build();
		var writer = blockfile.newWriterBuilder(FILENAME, StringCodec.UTF_8::encode)
				.setHeaderDictionary(HEADER_USER_DICT)
				.setFooterDictionarySupplier(footerUserDictionarySupplier)
				.addListeners(tokensListener, endingsListener)
				.setEncodeBatchSize(2)
				.setEncodeThreads(ENCODE_THREADS)
				.setCompressor(BlockfileTestCompressor.INSTANCE)
				.setChecksummer(BlockfileTestChecksummer.INSTANCE)
				.setWriteThreads(WRITE_THREADS)
				.build();
		var metadataReader = blockfile.newMetadataReaderBuilder(FILENAME)
				.build();
		var reader = blockfile.newReaderBuilder(metadataReader, $ -> StringCodec.UTF_8::decode)
				.setReadThreads(READ_THREADS)
				.setReadChunkSize(ByteLength.ofKiB(4))
				.setDecodeBatchSize(2)
				.setDecodeThreads(DECODE_THREADS)
				.enableChecksumValidation()
				.build();

		// write
		var blockCounter = new AtomicLong();
		BlockfileWriteResult writeResult = Scanner.of(BLOCK_STRINGS)
				.each($ -> blockCounter.incrementAndGet())
				.apply(writer::write);

		// file named "blockfile" should exist
		// view in terminal "xxd blockfile"

		/*------- test write --------*/

		// validate result
		long blobLength = STORAGE.length(FILENAME);
		Assert.assertEquals(writeResult.fileLength().toBytes(), blobLength);
		Assert.assertEquals(writeResult.numDataBlocks(), BLOCK_COUNT);

		// validate bytes and tokens match and don't change in the future
		InputStream fileInputStream = STORAGE.readInputStream(
				FILENAME,
				reader.config().readThreads(),
				reader.config().readChunkSize());
		byte[] actualBytes = InputStreamTool.toArray(fileInputStream);
		Assert.assertEquals(
				actualBytes,
				Scanner.of(tokensListener.blockTokens())
						.concatIter(BlockfileTokens::toList)
						.listTo(ByteTool::concat));
//		HexBlockTool.print(actualBytes);
		String hex = """
				00000065480412434845434b53554d5f414c474f524954484d04544553540f434845434b53554d5f
				4c454e47544804000000040a434f4d50524553534f5204544553540f555345525f44494354494f4e
				4152591101077573726b6579300775737276616c300000000f00000006423c546869733e0000000d
				00000004423c69733e0000000c00000003423c613e000000140000000b423c626c6f636b66696c65
				3e0000005746020b424c4f434b5f434f554e5401040f555345525f44494354494f4e415259320107
				656e64696e677328000000000000006500000000000000740000000000000081000000000000008d
				00000000000000a10000006500000057""";
		Assert.assertEquals(HexByteStringCodec.INSTANCE.encode(actualBytes), HexBlockTool.trim(hex));
		byte[] hexBytes = HexBlockTool.fromHexBlock(hex);
		Assert.assertEquals(actualBytes, hexBytes);

		// validate endings
		Assert.assertEquals(
				endingsListener.lastEnding().orElseThrow().intValue(),
				tokensListener.scanHeaderAndBlockTokens()
						.concat(BlockfileTokens::scan)
						.listTo(ByteTool::totalLength));

		/*-------- test list -------*/

		List<BlockfileNameAndSize> files = STORAGE.list();
		Assert.assertEquals(files.size(), 1);
		Assert.assertEquals(files.get(0).name(), FILENAME);
		Assert.assertEquals(files.get(0).size(), writeResult.fileLength().toBytes());

		/*------- test read --------*/

		// validate headers
		Assert.assertEquals(
				reader.header().checksumLength(),
				writer.config().checksummer().numBytes());
		Assert.assertEquals(
				reader.header().checksummer().encodedName(),
				writer.config().checksummer().encodedName());
		Assert.assertEquals(reader.header().userDictionary().size(), 1);
		Assert.assertEquals(
				reader.header().userDictionary().get(HEADER_DICT_KEY_0),
				StringCodec.UTF_8.encode(HEADER_DICT_VAL_0));

		// validate blocks
		Assert.assertEquals(
				reader.scanDecodedValues().list(),
				BLOCK_STRINGS);

		// validate footer dictionary contents
		Assert.assertEquals(reader.footer().blockCount(), BLOCK_COUNT);
		Assert.assertEquals(reader.footer().userDictionary().size(), 1);
		var expectedFooterUserDict = new BinaryDictionary()
				.put(FOOTER_DICT_ENDINGS, ENDINGS_CODEC.encode(endingsListener.allEndings()));
		byte[] actualEndingsBytes = reader.footer().userDictionary().get(FOOTER_DICT_ENDINGS);
		List<Long> actualDecodedEndings = ENDINGS_CODEC.decode(actualEndingsBytes);
		Assert.assertEquals(actualDecodedEndings, endingsListener.allEndings());

		// validate trailer
		Assert.assertEquals(
				reader.trailer().headerBlockLength(),
				reader.headerBlockLength());
		var expectedFooter = new BlockfileFooter(expectedFooterUserDict, BLOCK_COUNT);
		byte[] footerValueBytes = BlockfileFooter.VALUE_CODEC.encode(expectedFooter);
		byte[] expectedFooterBytes = BlockfileWriter.encodeFooter(footerValueBytes).concat();
		Assert.assertEquals(
				reader.trailer().footerBlockLength(),
				expectedFooterBytes.length);
	}

	private static Threads makeThreads(int count){
		var threads = new Threads(Executors.newFixedThreadPool(count), count);
		REGISTERED_THREADS.add(threads);
		return threads;
	}

	@AfterClass
	public void afterClass(){
		Scanner.of(REGISTERED_THREADS)
				.map(Threads::exec)
				.forEach(ExecutorService::shutdown);
	}

}
