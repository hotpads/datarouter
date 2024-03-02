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
package io.datarouter.bytes.blockfile.io.compact;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.io.merge.BlockfileMergePlan;
import io.datarouter.bytes.blockfile.io.merge.BlockfileMerger;
import io.datarouter.bytes.blockfile.io.merge.BlockfileMergerParams;
import io.datarouter.bytes.blockfile.io.storage.BlockfileNameAndSize;
import io.datarouter.scanner.Scanner;

/**
 * For looping over a directory of KvFiles and merging N files at a time until only targetNumFiles remain.
 * We gather the list of files only once, then track the remaining files in memory.
 * Files added to the directory after processing starts are ignored, otherwise it could run forever.
 * Currently merges smallest N files in a loop until targetNumFiles is met.
 */
public class BlockfileCompactor{
	private static final Logger logger = LoggerFactory.getLogger(BlockfileCompactor.class);

	public record BlockfileCompactorParams(
			Supplier<Boolean> shouldStop,
			int targetNumFiles,
			boolean prune,
			BlockfileMergerParams mergerParams){
	}

	private final BlockfileCompactorParams params;
	private final BlockfileCompactorFileCache fileCache;

	public BlockfileCompactor(BlockfileCompactorParams params){
		this.params = params;
		fileCache = new BlockfileCompactorFileCache(
				params.targetNumFiles(),
				params.prune(),
				params.mergerParams().readParams().readBufferSize(),
				params.mergerParams().readParams().memoryFanIn(),
				params.mergerParams().readParams().streamingFanIn(),
				listFilesInDirectory());
	}

	public void compact(){
		Scanner.generate(fileCache::findNextMergePlan)
				.advanceWhile(Optional::isPresent)
				.map(Optional::orElseThrow)
				.forEach(this::merge);
	}

	private void merge(BlockfileMergePlan plan){
		logger.warn(
				"startingMerging {}/{}->{}, inputSize={}, files={}",
				plan.files().size(),
				fileCache.numFiles(),
				fileCache.numFiles() - plan.files().size() + 1,
				BlockfileNameAndSize.totalSize(plan.files()).toDisplay(),
				makeFileSummaryMessage(plan.files()));
		var merger = new BlockfileMerger(params.mergerParams(), plan, params.shouldStop()::get);
		BlockfileNameAndSize newFile = merger.merge();
		fileCache.add(newFile);
		Scanner.of(plan.files())
				.each(fileCache::remove)
				.map(BlockfileNameAndSize::name)
				.batch(1_000)
				.forEach(params.mergerParams().storageParams().blockfileGroup().storage()::deleteMulti);
	}

	private List<BlockfileNameAndSize> listFilesInDirectory(){
		return params.mergerParams().storageParams().blockfileGroup().storage().list();
	}

	@SuppressWarnings("unused")
	private void validateFileCacheSize(){
		int numFilesInDirectory = listFilesInDirectory().size();
		if(fileCache.numFiles() != numFilesInDirectory){
			String message = String.format(
					"fileCacheSize=%s != filesInDirectory=%s",
					fileCache.numFiles(),
					numFilesInDirectory);
			throw new IllegalStateException(message);
		}
	}

	private static String makeFileSummaryMessage(List<BlockfileNameAndSize> files){
		return Scanner.of(files)
				.map(file -> String.format(
						"%s[%s]",
						file.name(),
						ByteLength.ofBytes(file.size()).toDisplay()))
				.collect(Collectors.joining(", "));
	}

}
