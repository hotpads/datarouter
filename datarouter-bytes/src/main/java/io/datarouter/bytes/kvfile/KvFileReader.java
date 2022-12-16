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
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import io.datarouter.bytes.InputStreamTool;
import io.datarouter.bytes.MultiByteArrayInputStream;
import io.datarouter.scanner.Scanner;

/**
 * Named wrapper around an InputStream so we can trace where errors came from.
 */
public class KvFileReader{

	private final InputStream inputStream;
	private final String name;
	private final ExecutorService prefetchBytesExec;
	private final ExecutorService prefetchEntriesExec;
	private final int prefetchN;

	public KvFileReader(
			InputStream inputStream,
			String name,
			ExecutorService prefetchBytesExec,
			ExecutorService prefetchEntriesExec,
			int prefetchN){
		this.inputStream = inputStream;
		this.name = name;
		this.prefetchBytesExec = prefetchBytesExec;
		this.prefetchEntriesExec = prefetchEntriesExec;
		this.prefetchN = prefetchN;
	}

	public KvFileReader(byte[] bytes){
		this(new ByteArrayInputStream(bytes), null, null, -1);
	}

	public KvFileReader(
			Scanner<byte[]> chunkScanner,
			String name,
			ExecutorService prefetchBytesExec,
			ExecutorService prefetchEntriesExec,
			int prefetchN){
		this(chunkScanner.apply(MultiByteArrayInputStream::new),
				name,
				prefetchBytesExec,
				prefetchEntriesExec,
				prefetchN);
	}

	public KvFileReader(Scanner<byte[]> chunkScanner, String name){
		this(chunkScanner, name, null, null, -1);
	}

	public KvFileReader(
			InputStream inputStream,
			ExecutorService prefetchBytesExec,
			ExecutorService prefetchEntriesExec,
			int prefetchN){
		this(inputStream, null, prefetchBytesExec, prefetchEntriesExec, prefetchN);
	}

	public KvFileReader(InputStream inputStream){
		this(inputStream, null, null, -1);
	}

	public Scanner<byte[]> scanEntryByteArrays(){
		return Scanner.generate(() -> {
			try{
				byte[] bytes = KvFileEntrySerializer.entryBytesFromInputStream(inputStream);
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

	public Scanner<KvFileEntry> scanEntries(){
		Scanner<byte[]> byteScanner = scanEntryByteArrays();
		if(prefetchBytesExec != null){
			byteScanner = byteScanner.prefetch(prefetchBytesExec, prefetchN);
		}
		Scanner<KvFileEntry> entryScanner = byteScanner.map(KvFileEntrySerializer::fromBytes);
		if(prefetchEntriesExec != null){
			entryScanner = entryScanner.prefetch(prefetchEntriesExec, prefetchN);
		}
		return entryScanner;
	}

}
