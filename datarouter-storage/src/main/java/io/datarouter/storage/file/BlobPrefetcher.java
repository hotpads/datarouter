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
package io.datarouter.storage.file;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.io.MultiByteArrayInputStream;
import io.datarouter.bytes.split.ChunkScannerTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.node.op.raw.read.BlobStorageReader;
import io.datarouter.util.concurrent.BlockingQueueTool;

public class BlobPrefetcher{
	private static final Logger logger = LoggerFactory.getLogger(BlobPrefetcher.class);

	private final Scanner<BlobPrefetchRequest> requests;
	private final Threads scanChunksThreads;
	private final ByteLength chunkSize;
	private final int bufferSizeKiB;// Number of semaphore permits
	private final ExecutorService prefetchExec;
	private final Semaphore semaphore;
	private final BlockingQueue<PrefetchMessage> buffer;
	private Future<Void> prefetchFuture;

	public BlobPrefetcher(
			Scanner<BlobPrefetchRequest> requests,
			Threads scanChunksThreads,
			ByteLength chunkSize,
			ByteLength bufferSize,
			ExecutorService prefetchExec){
		if(bufferSize.toBytes() < chunkSize.toBytes()){
			String message = String.format("bufferSize=%s must be >= chunkSize=%s", bufferSize, chunkSize);
			throw new IllegalArgumentException(message);
		}
		this.requests = requests;
		this.scanChunksThreads = scanChunksThreads;
		this.chunkSize = chunkSize;
		this.prefetchExec = prefetchExec;
		this.bufferSizeKiB = (int)bufferSize.toKiB();
		semaphore = new Semaphore(bufferSizeKiB);
		buffer = new LinkedBlockingQueue<>(Integer.MAX_VALUE);
	}

	public record BlobPrefetchRequest(
			BlobStorageReader reader,
			PathbeanKey key,
			long fileLength){
	}

	/*-------- fill buffer -----------*/

	private Void prefetch(){
		try{
			requests
					.concat(request -> ChunkScannerTool.scanChunks(0, request.fileLength(), chunkSize.toBytesInt())
							.map(chunkRange -> new BlobChunkRequest(
									request.reader(),
									request.key(),
									chunkRange.start,
									chunkRange.length)))
					.parallelOrdered(scanChunksThreads)
					.map(chunkRequest -> {
						byte[] bytes = chunkRequest.reader().readPartial(
								chunkRequest.key(),
								chunkRequest.offset(),
								chunkRequest.length())
								.orElseThrow();
						return new BlobChunkResponse(
								chunkRequest.reader(),
								chunkRequest.key(),
								chunkRequest.offset(),
								bytes);
					})
					.forEach(this::addToBuffer);
		}catch(RuntimeException e){
			BlockingQueueTool.put(buffer, PrefetchMessage.error(e));
		}
		// Add non-present item to signal the end.
		BlockingQueueTool.put(buffer, PrefetchMessage.absent());
		return null;
	}

	/*--------- drain buffer -----------*/

	public record BlobChunkRequest(
			BlobStorageReader reader,
			PathbeanKey key,
			long offset,
			int length){
	}

	public record BlobChunkResponse(
			BlobStorageReader reader,
			PathbeanKey key,
			long offset,
			byte[] bytes){
	}

	private Scanner<BlobChunkResponse> scanChunksFromBuffer(){
		prefetchFuture = prefetchExec.submit(this::prefetch);
		return Scanner.generate(this::takeNextFromBuffer)
				.advanceWhile(Optional::isPresent)
				.concatOpt(Function.identity());
	}

	public record PathbeanKeyAndInputStream(
			PathbeanKey key,
			InputStream inputStream){
	}

	public Scanner<PathbeanKeyAndInputStream> scanInputStreams(){
		return scanChunksFromBuffer()
				.splitByWithSplitKey(BlobChunkResponse::key)
				.map(keyAndChunks -> {
					InputStream inputStream = keyAndChunks.scanner()
							.map(BlobChunkResponse::bytes)
							.apply(MultiByteArrayInputStream::new);
					return new PathbeanKeyAndInputStream(
							keyAndChunks.splitKey(),
							inputStream);
				});
	}

	/*--------- helpers ---------------*/

	private static int calcKiB(BlobChunkResponse item){
		int itemBytes = item.bytes().length;
		int fullKiBs = itemBytes / 1024;
		boolean hasPartialKiB = itemBytes % 1024 > 0;
		return hasPartialKiB ? fullKiBs + 1 : fullKiBs;
	}

	private void addToBuffer(BlobChunkResponse item){
		int kiB = calcKiB(item);
		if(kiB > bufferSizeKiB){
			String message = String.format("Item KiB=%s is greater than maxKiB=%s", kiB, bufferSizeKiB);
			throw new RuntimeException(message);
		}
		try{
			semaphore.acquire(kiB);
		}catch(InterruptedException e){
			throw new RuntimeException(e);
		}
		buffer.add(PrefetchMessage.present(item));
	}

	private Optional<BlobChunkResponse> takeNextFromBuffer(){
		PrefetchMessage message = BlockingQueueTool.take(buffer);
		if(message.isPresent()){
			semaphore.release(calcKiB(message.value()));
			return Optional.of(message.value());
		}
		if(message.isError()){
			if(prefetchFuture != null){
				try{
					prefetchFuture.cancel(true);
				}catch(RuntimeException e){
					logger.warn("Exception canceling prefetch Future", e);
				}
			}
			throw message.error();
		}
		return Optional.empty();
	}

	/*-------- queue message -----------*/

	private record PrefetchMessage(
			boolean isPresent,
			BlobChunkResponse value,
			boolean isError,
			RuntimeException error){

		private static PrefetchMessage present(BlobChunkResponse item){
			return new PrefetchMessage(true, item, false, null);
		}

		private static PrefetchMessage absent(){
			return new PrefetchMessage(false, null, false, null);
		}

		private static PrefetchMessage error(RuntimeException error){
			return new PrefetchMessage(false, null, true, error);
		}
	}

}
