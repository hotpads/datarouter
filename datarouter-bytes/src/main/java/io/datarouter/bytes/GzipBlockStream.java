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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;

/**
 * Gzip normally encodes and decodes in a single thread which underutilizes multi-threaded hardware.
 * The data must be written to a single OutputStream and read from a single InputStream which is not parallelizable.
 * Alternatively we can split the data into blocks and run the gzip encoding on each block in separate threads.
 *
 * This class splits the incoming bytes into blocks and gzips each block independently.
 * Small tokens can be combined into GzipBlockStreamRow objects, where each row will be fully owned by one block.
 * When writing, it prepends a block length header before writing each gzipped data block.
 *
 * When reading, the main thread can pull gzipped blocks from the InputStream and pass them to other threads to decode.
 * Besides un-gzipping, blocks can further decoded in helper threads.
 */
public class GzipBlockStream{

	// 4 KiB seems to be smallest and fastest, but round up so the worker threads have bigger chunks of work
	private static int DEFAULT_BLOCK_SIZE = ByteLength.ofKiB(8).toBytesInt();
	// assume the gzipped data is smaller than the blockSize
	private static int DEFAULT_ENCODER_BUFFER_SIZE = ByteLength.ofKiB(8).toBytesInt();
	private static int DEFAULT_GZIP_BUFFER_SIZE = ByteLength.ofKiB(8).toBytesInt();
	// extra space in case the last row overflowed the blockSize.
	private static int DEFAULT_DECODER_BUFFER_SIZE = ByteLength.ofKiB(10).toBytesInt();

	private final int blockSize;
	private final int encodeBufferSize;
	private final int gzipBufferSize;
	private final int decodeBufferSize;
	private final AtomicLong numBlocksEncoded;

	public GzipBlockStream(){
		this(DEFAULT_BLOCK_SIZE,
				DEFAULT_ENCODER_BUFFER_SIZE,
				DEFAULT_GZIP_BUFFER_SIZE,
				DEFAULT_DECODER_BUFFER_SIZE);
	}

	public GzipBlockStream(int blockSize){
		this(blockSize,
				blockSize,
				DEFAULT_GZIP_BUFFER_SIZE,
				blockSize + ByteLength.ofKiB(2).toBytesInt());
	}

	public GzipBlockStream(int blockSize, int encodeBufferSize, int gzipBufferSize, int decodeBufferSize){
		this.blockSize = blockSize;
		this.encodeBufferSize = encodeBufferSize;
		this.gzipBufferSize = gzipBufferSize;
		this.decodeBufferSize = decodeBufferSize;
		numBlocksEncoded = new AtomicLong(0);
	}

	/*--------------- write ---------------*/

	/**
	 * Split the provided rows into larger blocks.
	 * Encode the blocks to gzip.
	 */
	public Scanner<GzipBlockStreamEncodedBlock> encode(Scanner<GzipBlockStreamRow> rows){
		return splitRowsIntoBlocks(rows)
				.map(this::encodeRowsToGzipBlock);
	}

	/**
	 * Split the provided rows into larger blocks.
	 * Pass each block to the provided executor for parallel encoding to gzip.
	 */
	public Scanner<GzipBlockStreamEncodedBlock> encodeParallel(
			Scanner<GzipBlockStreamRow> rows,
			ExecutorService exec,
			int numThreads){
		return splitRowsIntoBlocks(rows)
				//do not allow unorderedResults
				.parallel(new ParallelScannerContext(exec, numThreads, false))
				.map(this::encodeRowsToGzipBlock);
	}

	/**
	 * Split rows into bigger blocks when >= blockSize.
	 */
	private Scanner<List<GzipBlockStreamRow>> splitRowsIntoBlocks(Scanner<GzipBlockStreamRow> rows){
		var blockId = new AtomicLong();
		var currentBlockLength = new AtomicInteger();
		return rows
				.each(row -> {
					if(currentBlockLength.addAndGet(row.length()) >= blockSize){
						blockId.incrementAndGet();
						currentBlockLength.set(0);
					}
				})
				.splitBy($ -> blockId.get())
				.map(Scanner::list);
	}

	/**
	 * Combine the rows and gzip into a single block.
	 */
	private GzipBlockStreamEncodedBlock encodeRowsToGzipBlock(List<GzipBlockStreamRow> rows){
		var byteArrayOutputStream = new ByteArrayOutputStream(encodeBufferSize);
		try(var gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream, gzipBufferSize)){
			// make all the small writes inside a single try/catch block
			for(GzipBlockStreamRow row : rows){
				for(byte[] input : row.tokens()){
					gzipOutputStream.write(input);
				}
			}
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
		numBlocksEncoded.incrementAndGet();
		byte[] gzipBytes = byteArrayOutputStream.toByteArray();
		return new GzipBlockStreamEncodedBlock(gzipBytes);
	}

	/*--------------- read ---------------*/

	/**
	 * Convert an InputStream containing gzip blocks into a Scanner of raw blocks.
	 */
	public Scanner<byte[]> decode(InputStream inputStream){
		return rawInputStreamToGzipBlocks(inputStream)
				.map(this::gzipBlockToRawBlock);
	}

	/**
	 * Convert an InputStream containing gzip blocks into a Scanner of raw blocks.
	 * Offload the gzip decoding to the provided executor.
	 */
	public Scanner<byte[]> decodeParallel(
			InputStream inputStream,
			ExecutorService exec,
			int numThreads){
		return rawInputStreamToGzipBlocks(inputStream)
				//do not allow unorderedResults
				.parallel(new ParallelScannerContext(exec, numThreads, false))
				.map(this::gzipBlockToRawBlock);
	}

	/**
	 * Split the InputStream into blocks of gzip-encoded bytes that another thread can decode.
	 * Discards the length bytes that were prepended to each block.
	 */
	private Scanner<byte[]> rawInputStreamToGzipBlocks(InputStream rawInputStream){
		return Scanner.generate(() -> VarIntTool.fromInputStreamInt(rawInputStream))
				.advanceWhile(Optional::isPresent)
				.map(Optional::orElseThrow)
				.map(length -> InputStreamTool.readNBytes(rawInputStream, length));
	}

	/**
	 * Decode one block of gzip bytes.
	 */
	private byte[] gzipBlockToRawBlock(byte[] gzipBlock){
		var byteArrayInputStream = new ByteArrayInputStream(gzipBlock);
		var byteArrayOutputStream = new ByteArrayOutputStream(decodeBufferSize);
		try(var gzipInputStream = new GZIPInputStream(byteArrayInputStream, gzipBufferSize)){
			gzipInputStream.transferTo(byteArrayOutputStream);
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
		return byteArrayOutputStream.toByteArray();
	}

	/*----------- records -----------*/

	/**
	 * One or more tokens that make up a "row" of data.
	 * The splitter keeps rows together in the same block.
	 * It's fine to have just one token.
	 * Lets the caller avoid concatenating the tokens, since they'll just be appended to a GzipOutputStream.
	 */
	public record GzipBlockStreamRow(
			List<byte[]> tokens,
			int length){

		public GzipBlockStreamRow(List<byte[]> tokens){
			this(tokens, ByteTool.totalLength(tokens));
		}

		public byte[] concatTokens(){
			return ByteTool.concat(tokens);
		}

		public static int totalLength(List<GzipBlockStreamRow> rows){
			return rows.stream()
					.mapToInt(GzipBlockStreamRow::length)
					.sum();
		}

		public static byte[] concatRows(List<GzipBlockStreamRow> rows){
			return Scanner.of(rows)
					.concatIter(GzipBlockStreamRow::tokens)
					.listTo(ByteTool::concat);
		}
	}

	/**
	 * Returned while encoding with convenience methods.
	 * Each of these can be appended to an OutputStream.
	 * A Scanner of these can be converted to an InputStream.
	 */
	public static class GzipBlockStreamEncodedBlock{

		//not exposed directly because it needs a length header
		private final byte[] gzipBytes;

		private GzipBlockStreamEncodedBlock(byte[] gzipBytes){
			this.gzipBytes = gzipBytes;
		}

		/**
		 * Write the length bytes followed by the data bytes.
		 */
		public void toOutputStream(OutputStream outputStream){
			VarIntTool.writeBytes(gzipBytes.length, outputStream);
			OutputStreamTool.write(outputStream, gzipBytes);
		}

		public static InputStream toInputStream(Scanner<GzipBlockStreamEncodedBlock> blocks){
			return blocks
					.map(block -> block.gzipBytes)
					.concat(gzipBytes -> Scanner.of(
							VarIntTool.encode(gzipBytes.length),
							gzipBytes))
					.apply(MultiByteArrayInputStream::new);
		}
	}

	/*---------- instrumentation ---------*/

	public long getNumBlocksEncoded(){
		return numBlocksEncoded.get();
	}

	public GzipBlockStream resetCounters(){
		numBlocksEncoded.set(0);
		return this;
	}

}