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

import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.CountingInputStream;
import io.datarouter.bytes.kvfile.KvFileCompactorFileCache.KvFileMergePlan;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;

public class KvFileMerger{
	private static final Logger logger = LoggerFactory.getLogger(KvFileMerger.class);

	/*---------- params -----------*/

	public record KvFileMergerStorageParams(
			KvFileStorage storage,
			Supplier<String> filenameSupplier){
	}

	public record KvFileMergerByteReaderParams(
			int memoryFanIn,
			int streamingFanIn,
			Threads makeReadersThreads,
			boolean readParallel,
			ExecutorService readParallelExec,
			ByteLength readBufferSize,
			ByteLength chunkSize){

		public int totalThreads(){
			return Math.toIntExact(readBufferSize.toBytes() / chunkSize.toBytes());
		}
	}

	public record KvFileMergerKvReaderParams(
			int parseBatchSize,
			Threads parseThreads){
	}

	public record KvFileMergerEncodeParams(
			ByteLength minBlockSize,
			int encodeBatchSize,
			Threads encodeThreads){
	}

	public record KvFileMergerWriteParams(
			Threads writeThreads,
			ByteLength partSize){
	}

	public record KvFileMergerParams(
			KvFileMergerStorageParams storageParams,
			KvFileMergerByteReaderParams byteReaderParams,
			KvFileMergerKvReaderParams kvReaderParams,
			KvFileMergerEncodeParams encodeParams,
			KvFileMergerWriteParams writeParams,
			Duration heartbeatPeriod,
			Duration logPeriod){
	}

	/*---------- fields -----------*/

	private final KvFileMergerParams params;
	private final KvFileMergePlan plan;
	private final Supplier<Boolean> shouldStop;
	private final Instant startTime;
	private final AtomicLong lastLogTimeNs;

	private final AtomicLong waitForChunksNs = new AtomicLong();
	private final AtomicLong waitForCollatorNs = new AtomicLong();
	private final AtomicLong waitForShouldStopNs = new AtomicLong();
	private final AtomicLong waitForEncoderNs = new AtomicLong();
	private final AtomicLong recordsRead = new AtomicLong();// TODO count in KvReader?
	private final AtomicLong recordsWritten = new AtomicLong();
	private final AtomicLong recordsWrittenSinceLastLog = new AtomicLong();
	private final AtomicLong blocksRead = new AtomicLong();// TODO count in KvReader?
	private final AtomicLong blocksWritten = new AtomicLong();
	private final AtomicLong bytesRead = new AtomicLong();
	private final AtomicLong bytesWritten = new AtomicLong();

	public KvFileMerger(
			KvFileMergerParams params,
			KvFileMergePlan plan,
			Supplier<Boolean> shouldStop){
		this.params = params;
		this.plan = plan;
		this.shouldStop = shouldStop;
		startTime = Instant.now();
		lastLogTimeNs = new AtomicLong(0);
		ByteLength maxUploadSizeForS3 = ByteLength.ofBytes(10_000 * params.writeParams().partSize().toBytes());
		if(plan.totalInputSize().toBytes() > (maxUploadSizeForS3.toBytes() / 2)){
			logger.warn(
					"totalInputSize={} is greater than half of maxUploadSizeForS3={}.  Consider increasing partSize.",
					plan.totalInputSize().toDisplay(),
					maxUploadSizeForS3.toDisplay());
		}
	}

	public KvFileNameAndSize merge(){
		KvFileNameAndSize newFile = makeReaders()
				.listTo(plan.collatorStrategy().method)
				// combine entries into blocks quickly to avoid operations on individual entries
				.batchByMinSize(params.encodeParams().minBlockSize().toBytes(), KvFileEntry::length)
				.batch(params.encodeParams().encodeBatchSize())// group blocks for parallel encoding
				.timeNanos(waitForCollatorNs::addAndGet)// includes batching time
				.periodic(params.heartbeatPeriod(), $ -> throwIfShouldStop())
				.timeNanos(waitForShouldStopNs::addAndGet)
				.periodic(params.logPeriod(), $ -> logIntermediateProgress())
				.apply(this::encodeBlocksToBytes)
				.timeNanos(waitForEncoderNs::addAndGet)
				.concat(Scanner::of)
				.apply(this::write);
		logProgress(true, newFile);
		return newFile;
	}

	/*
	 * Allocate more prefetch threads to bigger files.
	 */
	private Scanner<KvFileReader> makeReaders(){
		var remainingFiles = new AtomicInteger(plan.files().size());
		var remainingThreads = new AtomicInteger(params.byteReaderParams().totalThreads());
		return Scanner.of(plan.files())
				.sort(Comparator.comparing(KvFileNameAndSize::size))//biggest last
				.parallelUnordered(params.byteReaderParams().makeReadersThreads())
				.map(file -> {
					int numThreadsActual;
					if(remainingFiles.get() == 1){//give all remaining threads to the last/biggest file
						numThreadsActual = Math.max(1, remainingThreads.get());
					}else{
						double pctOfTotalSize = (double)file.size() / (double)plan.totalInputSize().toBytes();
						double numThreadsCalc = pctOfTotalSize * params.byteReaderParams().totalThreads();
						numThreadsActual = Math.max(1, (int)numThreadsCalc);
					}
					remainingFiles.decrementAndGet();
					remainingThreads.addAndGet(-numThreadsActual);
					logger.info(
							"making KvReader size={}, threads={}, chunkSize={}",
							ByteLength.ofBytes(file.size()).toDisplay(),
							numThreadsActual,
							params.byteReaderParams().chunkSize().toDisplay());
					return makeReader(file, numThreadsActual, params.byteReaderParams().chunkSize());
				});
	}

	private KvFileReader makeReader(KvFileNameAndSize file, int numThreads, ByteLength chunkSize){
		if(file.size() <= chunkSize.toBytesInt()){
			long startNs = System.nanoTime();
			byte[] fullBytes = params.storageParams().storage().read(file.name());
			waitForChunksNs.addAndGet(System.nanoTime() - startNs);
			bytesRead.addAndGet(fullBytes.length);
			return new KvFileReader(
					fullBytes,
					file.name(),
					params.kvReaderParams().parseBatchSize(),
					params.kvReaderParams().parseThreads());
		}else if(params.byteReaderParams().readParallel()){
			return params.storageParams().storage()
					.readParallel(
							file.name(),
							0L,
							file.size(),
							new Threads(params.byteReaderParams().readParallelExec(), numThreads),
							chunkSize)
					.timeNanos(waitForChunksNs::addAndGet)
					.each(chunk -> bytesRead.addAndGet(chunk.length))
					.apply(chunks -> new KvFileReader(
							chunks,
							file.name(),
							params.kvReaderParams().parseBatchSize(),
							params.kvReaderParams().parseThreads()));
		}else{
			InputStream inputStream = params.storageParams().storage().readInputStream(file.name());
			var countingInputStream = new CountingInputStream(
					inputStream,
					ByteLength.ofMiB(1).toBytesInt(),
					bytesRead::addAndGet);
			return new KvFileReader(
					countingInputStream,
					file.name(),
					params.kvReaderParams().parseBatchSize(),
					params.kvReaderParams().parseThreads());
		}
	}

	private Scanner<List<byte[]>> encodeBlocksToBytes(Scanner<List<List<KvFileEntry>>> entryBlockBatches){
		return entryBlockBatches
				.parallelOrdered(params.encodeParams().encodeThreads())
				.map(entryBlockBatch -> Scanner.of(entryBlockBatch)
						.each(batch -> {
							recordsWritten.addAndGet(batch.size());
							recordsWrittenSinceLastLog.addAndGet(batch.size());
							blocksWritten.incrementAndGet();
						})
						.map(KvFileBlock::new)
						.map(KvFileBlock::toBytes)
						.each(blockBytes -> bytesWritten.addAndGet(blockBytes.length))
						.list());
	}

	private KvFileNameAndSize write(Scanner<byte[]> blocks){
		String filename = params.storageParams().filenameSupplier().get();
		if(plan.totalInputSize().toBytes() > params.writeParams().partSize().toBytes()){
			// Multi RPC overhead (at least on S3):
			//  - create multipart upload request
			//  - upload each part
			//  - mark completed
			Scanner<List<byte[]>> parts = groupBlocksIntoUploadParts(blocks);
			params.storageParams().storage().writeParallel(
					filename,
					parts,
					params.writeParams().writeThreads());
		}else{
			// Single RPC
			byte[] bytes = blocks
					.listTo(ByteTool::concat);
			params.storageParams().storage().write(filename, bytes);
		}
		return new KvFileNameAndSize(filename, bytesWritten.get());
	}

	private Scanner<List<byte[]>> groupBlocksIntoUploadParts(Scanner<byte[]> blocks){
		var splitId = new AtomicLong();
		var splitLength = new AtomicLong();
		return blocks
				.each(blockBytes -> {
					if(splitLength.addAndGet(blockBytes.length) >= params.writeParams().partSize().toBytes()){
						splitId.incrementAndGet();
						splitLength.set(0);
					}
				})
				.splitBy($ -> splitId.get())
				.map(Scanner::list);
	}

	private void logIntermediateProgress(){
		logProgress(false, null);
		lastLogTimeNs.set(System.nanoTime());
		recordsWrittenSinceLastLog.set(0);
	}

	private void logProgress(
			boolean complete,
			KvFileNameAndSize newFile){
		Duration duration = Duration.between(startTime, Instant.now());
		String action = complete ? "merged" : "merging";
		Function<Number,String> withCommas = number -> new DecimalFormat("###,###,###,###,###,###,###").format(number);
		Function<Long,String> nanosToString = nanos -> withCommas.apply(nanos / 1_000_000) + "ms";
		Map<String,String> kvs = new LinkedHashMap<>();
		// records written per second since last log
		long nsSinceLastLog = System.nanoTime() - lastLogTimeNs.get();
		long rwpsLatest = recordsWrittenSinceLastLog.get() * 1_000_000_000 / nsSinceLastLog;
		kvs.put("rwpsLatest", withCommas.apply(rwpsLatest));
		// records written per second since start
		long rwpsCumulative = recordsWritten.get() * 1_000_000_000 / duration.toNanos();
		kvs.put("rwpsCumulative", withCommas.apply(rwpsCumulative));
		kvs.put("recordsRead", withCommas.apply(recordsRead.get()));
		kvs.put("recordsWritten", withCommas.apply(recordsWritten.get()));
		kvs.put("blocksRead", withCommas.apply(blocksRead.get()));
		kvs.put("blocksWritten", withCommas.apply(blocksWritten.get()));
		kvs.put("bytesRead", ByteLength.ofBytes(bytesRead.get()).toDisplay());
		kvs.put("bytesWritten", ByteLength.ofBytes(bytesWritten.get()).toDisplay());
		kvs.put("duration", nanosToString.apply(duration.toNanos()).toString());
		kvs.put("waitForChunks", nanosToString.apply(waitForChunksNs.get()));
		kvs.put("waitForCollator", nanosToString.apply(waitForCollatorNs.get()));
		kvs.put("waitForShouldStop", nanosToString.apply(waitForShouldStopNs.get()));
		kvs.put("waitForEncoder", nanosToString.apply(waitForEncoderNs.get()));
		kvs.put("collator", plan.collatorStrategy().name());
		if(newFile != null){
			kvs.put("newFile", newFile.name());
		}
		String message = Scanner.of(kvs.keySet())
				.map(key -> key + "=" + kvs.get(key))
				.collect(Collectors.joining(", ", action + "[", "]"));
		logger.warn(message);
	}

	/*
	 * Throw error to prevent the output from being marked completed.
	 * For example, on S3 this should result in an AbortMultipartUploadRequest.
	 * Only check this periodically to protect from slow shouldStop implementations.
	 */
	private void throwIfShouldStop(){
		if(shouldStop.get()){
			throw new RuntimeException("stop requested");
		}
	}

}