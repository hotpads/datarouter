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
package io.datarouter.bytes.kvfile.merge;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.bytes.blockfile.dto.BlockfileNameAndSize;
import io.datarouter.bytes.blockfile.read.BlockfileReader.BlockfileDecodedBlock;
import io.datarouter.bytes.blockfile.read.BlockfileReader.BlockfileDecodedBlockBatch;
import io.datarouter.bytes.blockfile.write.BlockfileWriter;
import io.datarouter.bytes.blockfile.write.BlockfileWriter.BlockfileWriteResult;
import io.datarouter.bytes.kvfile.kv.KvFileEntry;
import io.datarouter.bytes.kvfile.merge.KvFileMergerThreadsCalculator.ThreadsForFile;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;

public class KvFileMerger{

	private final KvFileMergerParams params;
	private final KvFileMergePlan plan;
	private final Supplier<Boolean> shouldStop;
	private final String filename;
	private final KvFileMergerTracker tracker;
	private final int numVcpus;
	private final List<ThreadsForFile> threadsForFileListDesc;

	public KvFileMerger(
			KvFileMergerParams params,
			KvFileMergePlan plan,
			Supplier<Boolean> shouldStop){
		this.params = params;
		this.plan = plan;
		this.shouldStop = shouldStop;
		filename = params.storageParams().filenameSupplier().get();
		tracker = new KvFileMergerTracker(plan, filename);
		numVcpus = Runtime.getRuntime().availableProcessors();
		threadsForFileListDesc = new KvFileMergerThreadsCalculator(plan, params.readParams()).calc();
	}

	public BlockfileNameAndSize merge(){
		tracker.startTime = Instant.now();
		// Expect a network read spike as all readers are buffering data as fast as possible.
		// Expect a cpu spike because each reader is decoding the first batch of blocks.
		// Things should calm down after that because we have to collate and write the data.
		List<Scanner<BlockfileDecodedBlockBatch<List<KvFileEntry>>>> blockBatchScanners = Scanner.of(plan.files())
				.parallelUnordered(new Threads(params.readParams().prefetchExec(), plan.files().size()))
				.map(this::makeReader)
				.list();
		// Add the waitForBlocksNs to each input scanner after they're initialized
		List<Scanner<KvFileEntry>> kvScanners = Scanner.of(blockBatchScanners)
				.map(inputScanner -> inputScanner
						.timeNanos(tracker.waitForBlocksNs::addAndGet)
						.concatIter(BlockfileDecodedBlockBatch::blocks)
						.map(BlockfileDecodedBlock::value)
						.concat(Scanner::of))
				.list();
		tracker.logInitializationStats();
		tracker.resetCountersSinceLastLog();
		tracker.mergeStartTime = Instant.now();
		tracker.waitForReadersNs.addAndGet(Duration.between(tracker.startTime, tracker.mergeStartTime).toNanos());
		BlockfileWriteResult writeResult = plan.collatorStrategy().method.apply(kvScanners)
				.batchByMinSize(params.writeParams().minBlockSize().toBytes(), KvFileEntry::length)
				.timeNanos(tracker.waitForCollatorNs::addAndGet)
				.each(batchForBlock -> {
					tracker.blocksWritten.incrementAndGet();
					tracker.blocksWrittenSinceLastLog.incrementAndGet();
					tracker.recordsWritten.addAndGet(batchForBlock.size());
					tracker.recordsWrittenSinceLastLog.addAndGet(batchForBlock.size());
				})
				.periodic(params.heartbeatPeriod(), $ -> {
					tracker.logIntermediateProgress();
					tracker.resetCountersSinceLastLog();
					throwIfShouldStop();
				})
				.apply(makeWriter(filename)::write);
		var newFile = new BlockfileNameAndSize(filename, writeResult.fileLength().toBytes());
		tracker.logProgress(true, newFile);
		return newFile;
	}

	// Called in parallel
	private Scanner<BlockfileDecodedBlockBatch<List<KvFileEntry>>> makeReader(BlockfileNameAndSize file){
		int numReadThreads = Scanner.of(threadsForFileListDesc)
				.include(forFile -> forFile.file().equals(file))
				.map(ThreadsForFile::threads)
				.findFirst()
				.orElseThrow();
		var metadataReader = params.storageParams().blockfile().newMetadataReaderBuilder(file.name())
				.setKnownFileLength(file.size())
				.build();
		var readerBuilder = params.storageParams().blockfile().newReaderBuilder(
				metadataReader,
				$ -> params.blockFormat().newBlockCodec()::decodeAll)
				.setReadChunkSize(params.readParams().readChunkSize())
				.setDecodeBatchSize(params.readParams().decodeBatchSize())
				.setDecodeThreads(new Threads(params.readParams().readExec(), numVcpus));
		if(numReadThreads > 0){
			readerBuilder
				.setReadThreads(Threads.useExecForSingleThread(params.readParams().readExec(), numReadThreads));
		}
		var reader = readerBuilder.build();
		return reader.scanDecodedBlockBatches()
				.each(blockBatch -> {
					tracker.blocksRead.addAndGet(blockBatch.blocks().size());
					tracker.blocksReadSinceLastLog.addAndGet(blockBatch.blocks().size());
					tracker.compressedBytesRead.addAndGet(blockBatch.totalCompressedSize());
					tracker.compressedBytesReadSinceLastLog.addAndGet(blockBatch.totalCompressedSize());
					tracker.decompressedBytesRead.addAndGet(blockBatch.totalDecompressedSize());
					tracker.decompressedBytesReadSinceLastLog.addAndGet(blockBatch.totalDecompressedSize());
					blockBatch.blocks().forEach(block -> {
						tracker.recordsRead.addAndGet(block.value().size());
						tracker.recordsReadSinceLastLog.addAndGet(block.value().size());
					});
				})
				// Trigger the first batch of internal decoding in this parallel method.
				// Otherwise the prefetcher or collator will trigger them sequentially.
				.peekFirst($ -> {});
	}

	private BlockfileWriter<List<KvFileEntry>> makeWriter(String filename){
		boolean multipartWrite = plan.totalInputSize().toBytes()
				> params.writeParams().multipartUploadThreshold().toBytes();
		var writeThreads = new Threads(
				params.writeParams().writeExec(),
				params.writeParams().writeThreads());
		return params.storageParams().blockfile().newWriterBuilder(
				filename,
				params.blockFormat().newBlockCodec()::encodeAll)
				.setCompressor(params.writeParams().compressor())
				.setEncodeBatchSize(params.writeParams().encodeBatchSize())
				.setEncodeThreads(new Threads(params.writeParams().encodeExec(), numVcpus))
				.setMultipartWrite(multipartWrite)
				.setWriteThreads(writeThreads)
				.build();
	}

	/*---------- shouldStop ------------*/

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