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
package io.datarouter.bytes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.bytes.compress.gzip.GzipBlockStream;
import io.datarouter.bytes.compress.gzip.GzipBlockStream.GzipBlockStreamRow;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;

public class GzipBlockStreamTests{
	private static final Logger logger = LoggerFactory.getLogger(GzipBlockStreamTests.class);

	private static final List<GzipBlockStreamRow> ROWS = Scanner.iterate(0L, i -> i + 1)
			.limit(1_000_000)
			.map(i -> i.toString())//variable length tokens (ambiguous method reference)
			.map(StringCodec.US_ASCII::encode)
			.map(token -> new GzipBlockStreamRow(List.of(token, new byte[2])))
			.list();
	private static final int RAW_SIZE = GzipBlockStreamRow.totalLength(ROWS);

	@Test
	public void testRoundTrip(){
		var blockStream = new GzipBlockStream();
		var outputStream = new ByteArrayOutputStream();
		blockStream.encode(Scanner.of(ROWS))
				.forEach(gzipBlock -> gzipBlock.toOutputStream(outputStream));
		int encodedSize = outputStream.size();
		var inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		byte[] allOutputBytes = blockStream.decode(inputStream)
				.listTo(ByteTool::concat);

		logger.warn("rawSize={}", RAW_SIZE);
		logger.warn("numBlocksEncoded={}", blockStream.getNumBlocksEncoded());
		logger.warn("encodedSize={}", encodedSize);

		//verify the data is splitting into blocks
		Assert.assertTrue(blockStream.getNumBlocksEncoded() > 3);
		//verify it's compressed
		Assert.assertTrue(encodedSize < RAW_SIZE);
		//verify it's correct
		Assert.assertEquals(allOutputBytes, GzipBlockStreamRow.concatRows(ROWS));
	}

	@Test
	public void testRoundTripParallel(){
		var blockStream = new GzipBlockStream();
		var threads = new Threads(Executors.newFixedThreadPool(4), 4);
		var outputStream = new ByteArrayOutputStream();
		blockStream.encodeParallel(Scanner.of(ROWS), threads)
				.forEach(gzipBlock -> gzipBlock.toOutputStream(outputStream));
		int encodedSize = outputStream.size();
		var inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		byte[] allOutputBytes = blockStream.decodeParallel(inputStream, threads)
				.listTo(ByteTool::concat);

		threads.exec().shutdownNow();

		logger.warn("rawSize={}", RAW_SIZE);
		logger.warn("numBlocksEncoded={}", blockStream.getNumBlocksEncoded());
		logger.warn("encodedSize={}", encodedSize);

		//verify the data is splitting into blocks
		Assert.assertTrue(blockStream.getNumBlocksEncoded() > 3);
		//verify it's compressed
		Assert.assertTrue(encodedSize < RAW_SIZE);
		//verify it's correct
		Assert.assertEquals(allOutputBytes, GzipBlockStreamRow.concatRows(ROWS));
	}

	// Output shows blockSize=2048 compresses best and is fastest
	// blockSize=256, numBlocks=266057, encodedSize=23959457, timeMs=3873
	// blockSize=512, numBlocks=133037, encodedSize=19030593, timeMs=3305
	// blockSize=1024, numBlocks=66965, encodedSize=18024268, timeMs=3128
	// blockSize=2048, numBlocks=33587, encodedSize=17403633, timeMs=2924
	// blockSize=4096, numBlocks=16796, encodedSize=17466394, timeMs=3012
	// blockSize=8192, numBlocks=8405, encodedSize=18537026, timeMs=3194
	// blockSize=16384, numBlocks=4204, encodedSize=19512810, timeMs=3372
	// blockSize=32768, numBlocks=2102, encodedSize=20185154, timeMs=3481
	// blockSize=65536, numBlocks=1052, encodedSize=20638401, timeMs=3835
	@Test(enabled = false)
	public void testTryVariousBlockSizes(){
		for(int blockSize = 256; blockSize <= 65_536; blockSize *= 2){
			var blockStream = new GzipBlockStream(blockSize);
			var outputStream = new ByteArrayOutputStream();
			long startMs = System.currentTimeMillis();
			blockStream.encode(Scanner.of(ROWS))
					.forEach(gzipBlock -> gzipBlock.toOutputStream(outputStream));
			logger.warn("blockSize={}, numBlocks={}, encodedSize={}, timeMs={}",
					blockSize,
					blockStream.getNumBlocksEncoded(),
					outputStream.size(),
					System.currentTimeMillis() - startMs);
		}
	}

}
