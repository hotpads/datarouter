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
package io.datarouter.bytes.kvfile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.InputStreamTool;
import io.datarouter.bytes.MultiByteArrayInputStream;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;

/**
 * Named wrapper around an InputStream so we can trace where errors came from.
 * Can offload block parsing to helper threads for faster reading.
 */
public class KvFileReader{
	private static final Logger logger = LoggerFactory.getLogger(KvFileReader.class);

	private static final int DEFAULT_PARSE_BATCH_SIZE = 3;

	private final InputStream inputStream;
	private final String name;
	private final int parseBatchSize;
	private final Threads parseThreads;

	public KvFileReader(
			InputStream inputStream,
			String name,
			int parseBatchSize,
			Threads parseThreads){
		this.inputStream = inputStream;
		this.name = name;
		this.parseBatchSize = parseBatchSize;
		this.parseThreads = parseThreads;
	}

	public KvFileReader(
			byte[] bytes,
			String name,
			int parseBatchSize,
			Threads parseThreads){
		this(new ByteArrayInputStream(bytes),
				name,
				parseBatchSize,
				parseThreads);
	}

	public KvFileReader(
			Scanner<byte[]> chunkScanner,
			String name,
			int parseBatchSize,
			Threads parseThreads){
		this(chunkScanner.apply(MultiByteArrayInputStream::new),
				name,
				parseBatchSize,
				parseThreads);
	}

	public KvFileReader(
			InputStream inputStream,
			Threads parseThreads){
		this(inputStream, null, DEFAULT_PARSE_BATCH_SIZE, parseThreads);
	}

	public KvFileReader(InputStream inputStream, String name){
		this(inputStream, name, DEFAULT_PARSE_BATCH_SIZE, null);
	}

	public KvFileReader(InputStream inputStream){
		this(inputStream, null, DEFAULT_PARSE_BATCH_SIZE, null);
	}

	/*------------ scan blocks ----------------*/

	public Scanner<byte[]> scanBlockByteArrays(){
		return Scanner.generate(() -> {
			try{
				byte[] bytes = KvFileBlock.blockBytesFromInputStream(inputStream);
				if(bytes == null){
					InputStreamTool.close(inputStream);
				}
				return bytes;
			}catch(RuntimeException e){
				String message = String.format(
						"error on %s, inputStreamType=%s, name=%s",
						getClass().getSimpleName(),
						inputStream.getClass().getSimpleName(),
						name);
				throw new RuntimeException(message, e);
			}
		})
		.advanceUntil(Objects::isNull);
	}

	public Scanner<KvFileBlock> scanBlocks(){
		if(parseThreads == null){
			return scanBlockByteArrays()
					.map(KvFileBlock::fromBytes);
		}
		return scanBlockByteArrays()
				.batch(parseBatchSize)
				// The prefetcher is allocating block byte[]s and filling them with data from the chunks
				// TODO determine if it actually helps
//				.prefetch(parseExec, 1 * numParseThreads)
				.parallelOrdered(parseThreads)
				.map(byteArrays -> Scanner.of(byteArrays)
						.map(KvFileBlock::fromBytes)
						.collect(() -> new ArrayList<>(byteArrays.size())))
				.concat(Scanner::of);
	}

	public Scanner<KvFileEntry> scanBlockEntries(){
		return scanBlocks()
				.concat(KvFileBlock::scanEntries);
	}

}
