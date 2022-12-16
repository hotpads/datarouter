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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.op.raw.BlobStorage;
import io.datarouter.storage.util.KvCompactorFileCache.KvFileMergePlan;
import io.datarouter.storage.util.KvFileMerger.MergeResult;

/**
 * For looping over a directory of KvFiles and merging N files at a time until only targetNumFiles remain.
 * We gather the list of files only once, then track the remaining files in memory.
 * Files added to the directory after processing starts are ignored, otherwise it could run forever.
 * Currently merges smallest N files in a loop until targetNumFiles is met.
 */
public class KvFileCompactor{
	private static final Logger logger = LoggerFactory.getLogger(KvFileCompactor.class);

	/*---------- params ---------*/

	public record KvFileCompactorMergeParams(
			int fanIn,
			boolean useChunkScanner,
			int prefetchChunksPerInputStream,
			ByteLength chunkSize,
			ExecutorService exec){
	}

	public record KvFileCompactorReaderParams(
			int prefetchSize,
			ExecutorService prefetchBytesExec,
			ExecutorService prefetchEntriesExec){
	}

	public record KvFileCompactorTransferThreadParams(
			int messageSize,
			int messageBufferSize,
			int logEveryNMessages){
	}

	public record KvFileCompactorWriteParams(
			ByteLength streamWritesLargerThan,
			ExecutorService exec,
			int numThreads,
			ByteLength partSize){
	}

	public record KvFileCompactorParams(
			TaskTracker taskTracker,
			BlobStorage blobStorage,
			Supplier<String> filenameSupplier,
			int targetNumFiles,
			KvFileCompactorMergeParams mergeParams,
			KvFileCompactorReaderParams readerParams,
			KvFileCompactorTransferThreadParams transferThreadParams,
			KvFileCompactorWriteParams writeParams){
	}

	/*---------- fields ---------*/

	private final KvFileCompactorParams params;
	private final KvCompactorFileCache fileCache;

	public KvFileCompactor(KvFileCompactorParams params){
		this.params = params;
		List<FilenameAndSize> filesAtStart = params.blobStorage().scan(Subpath.empty())
				.map(pathbean -> new FilenameAndSize(pathbean.getKey().getFile(), pathbean.getSize()))
				.list();
		fileCache = new KvCompactorFileCache(
				params.targetNumFiles(),
				params.mergeParams().fanIn(),
				params.mergeParams().chunkSize(),
				params.writeParams().streamWritesLargerThan(),
				filesAtStart);
	}

	public void compact(){
		Scanner.generate(() -> fileCache.findNextMergePlan())
				.advanceWhile(Optional::isPresent)
				.map(Optional::orElseThrow)
				.forEach(this::merge);
	}

	private void merge(KvFileMergePlan plan){
		logger.warn(
				"startingMerging {}/{}, inputSize={}, files={}",
				plan.files().size(),
				fileCache.numFiles(),
				FilenameAndSize.totalSize(plan.files()).toDisplay(),
				makeFileSummaryMessage(plan.files()));
		var compactorMerge = new KvFileMerger(params, plan);
		MergeResult mergeResult = compactorMerge.merge(params.taskTracker());
		fileCache.add(mergeResult.newFile());
		Scanner.of(plan.files())
				.each(fileCache::remove)
				.map(FilenameAndSize::name)
				.map(PathbeanKey::of)
				.parallel(new ParallelScannerContext(
						params.writeParams().exec(),
						params.writeParams().numThreads(),
						true))
				.forEach(params.blobStorage()::delete);
		logger.warn("finishedMerging {}", mergeResult);
	}

	private static String makeFileSummaryMessage(List<FilenameAndSize> files){
		return Scanner.of(files)
				.map(file -> String.format(
						"%s[%s]",
						file.name(),
						ByteLength.ofBytes(file.size()).toDisplay()))
				.collect(Collectors.joining(", "));
	}

}