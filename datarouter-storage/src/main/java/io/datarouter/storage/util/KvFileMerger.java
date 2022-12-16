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
package io.datarouter.storage.util;

import java.io.InputStream;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.CountingInputStream;
import io.datarouter.bytes.MultiByteArrayInputStream;
import io.datarouter.bytes.kvfile.KvFileCollator;
import io.datarouter.bytes.kvfile.KvFileEntry;
import io.datarouter.bytes.kvfile.KvFileReader;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.KvCompactorFileCache.KvFileMergePlan;
import io.datarouter.storage.util.KvFileCompactor.KvFileCompactorParams;
import io.datarouter.util.Count;
import io.datarouter.util.Count.Counts;
import io.datarouter.util.Require;
import io.datarouter.util.concurrent.TransferThread;
import io.datarouter.util.concurrent.TransferThread.TransferThreadBuilder;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.tuple.Range;

public class KvFileMerger{
	private static final Logger logger = LoggerFactory.getLogger(KvFileMerger.class);

	private final KvFileCompactorParams params;
	private final KvFileMergePlan plan;

	public KvFileMerger(
			KvFileCompactorParams params,
			KvFileMergePlan plan){
		this.params = params;
		this.plan = plan;
	}

	public MergeResult merge(TaskTracker tracker){
		var counts = new Counts();
		var readStallNs = counts.add("readStallNs");
		var writeStallNs = counts.add("writeStallNs");
		var bytesRead = counts.add("bytesRead");
		var recordsRead = counts.add("recordsRead");
		var messagesSubmitted = counts.add("messagesSubmitted");

		Function<Scanner<List<KvFileEntry>>,FilenameAndSize> writeMethod = plan.streamingWrite()
				? this::writeLarge
				: this::writeSmall;

		// TransferThread splits the work into two threads and shows if we're bottlenecked on reading or writing
		TransferThread<List<KvFileEntry>,FilenameAndSize> transferThread = new TransferThreadBuilder<>(
						params.transferThreadParams().messageBufferSize(),
						batches -> batches.apply(writeMethod))
				.withInputStallNanosCallback(readStallNs::incrementBy)
				.withOutputStallNanosCallback(writeStallNs::incrementBy)
				.build();

		// pull data through the collator and add it to the buffer as space becomes available
		long startMs = System.currentTimeMillis();
		Scanner.of(plan.files())
				.parallel(new ParallelScannerContext(
						params.readerParams().prefetchEntriesExec(),//TODO use dedicated executor
						plan.files().size(),
						true))
				.map(file -> makeReader(file, bytesRead))
				.listTo(KvFileCollator::new)
				.mergeKeepingLatestVersion()
				//create a TransferThread message
				.batch(params.transferThreadParams().messageSize())
				.each(transferThread::submit)
				//check shouldStop: if found then throw error to prevent the output from being marked completed
				.each($ -> Require.isFalse(tracker.shouldStop()))
				//monitoring
				.each(recordsRead::incrementBySize)
				.each(messagesSubmitted::increment)
				.forEach($ -> {
					if(messagesSubmitted.value() % params.transferThreadParams().logEveryNMessages() == 0){
						logger.warn("intermediate merge progress: {}", counts);
					}
				});

		// wait for completion
		FilenameAndSize newFile = transferThread.complete();

		return new MergeResult(
				newFile,
				recordsRead,
				bytesRead,
				newFile.size(),
				System.currentTimeMillis() - startMs,
				readStallNs,
				writeStallNs);
	}

	private KvFileReader makeReader(FilenameAndSize file, Count bytesRead){
		PathbeanKey key = PathbeanKey.of(file.name());
		if(file.size() <= params.mergeParams().chunkSize().toBytesInt()){
			byte[] bytes = params.blobStorage().read(key);
			bytesRead.incrementByLength(bytes);
			return new KvFileReader(bytes);
		}else if(params.mergeParams().useChunkScanner()){
			return params.blobStorage().scanChunks(
					key,
					new Range<>(0L, true, file.size(), false),
					params.mergeParams().exec(),
					params.mergeParams().fanIn(),
					params.mergeParams().chunkSize().toBytesInt())
					.each(chunk -> bytesRead.incrementByLength(chunk))
					.apply(chunks -> new KvFileReader(
							chunks,
							file.name(),
							params.readerParams().prefetchBytesExec(),
							params.readerParams().prefetchEntriesExec(),
							params.readerParams().prefetchSize()));
		}else{
			InputStream inputStream = params.blobStorage().readInputStream(key);
			var countingInputStream = new CountingInputStream(
					inputStream,
					ByteLength.ofMiB(1).toBytesInt(),
					bytesRead::incrementBy);
			return new KvFileReader(
					countingInputStream,
					file.name(),
					params.readerParams().prefetchBytesExec(),
					params.readerParams().prefetchEntriesExec(),
					params.readerParams().prefetchSize());
		}
	}

	// Single RPC
	private FilenameAndSize writeSmall(Scanner<List<KvFileEntry>> entries){
		String filename = params.filenameSupplier().get();
		byte[] bytes = entries
				.concat(Scanner::of)
				.map(KvFileEntry::bytes)
				.listTo(ByteTool::concat);
		params.blobStorage().write(PathbeanKey.of(filename), bytes);
		return new FilenameAndSize(filename, bytes.length);
	}

	// Multi RPC overhead (at least on S3):
	//  - create multipart upload request
	//  - upload each part
	//  - mark completed
	private FilenameAndSize writeLarge(Scanner<List<KvFileEntry>> entries){
		InputStream inputStream = entries
				.concat(Scanner::of)
				.map(KvFileEntry::bytes)
				.apply(MultiByteArrayInputStream::new);
		String filename = params.filenameSupplier().get();
		PathbeanKey key = PathbeanKey.of(filename);
		params.blobStorage().writeParallel(
				key,
				inputStream,
				params.writeParams().exec(),
				params.writeParams().numThreads());
		long newLength = params.blobStorage().length(key).orElseThrow();
		return new FilenameAndSize(filename, newLength);
	}

	public record MergeResult(
			FilenameAndSize newFile,
			Count recordsRead,
			Count bytesRead,
			long bytesWritten,
			long timeMs,
			Count readStallNs,
			Count writeStallNs){

		@Override
		public String toString(){
			return "MergeResult [newFilename=" + newFile.name()
					+ ", recordsRead=" + NumberFormatter.addCommas(recordsRead.value())
					+ ", bytesRead=" + ByteLength.ofBytes(bytesRead.value()).toDisplay()
					+ ", bytesWritten=" + ByteLength.ofBytes(bytesWritten).toDisplay()
					+ ", timeMs=" + NumberFormatter.addCommas(timeMs)
					+ ", readStallMs=" + NumberFormatter.addCommas(readStallNs.value() / 1_000_000)
					+ ", writeStallMs=" + NumberFormatter.addCommas(writeStallNs.value() / 1_000_000)
					+ "]";
		}
	}

}