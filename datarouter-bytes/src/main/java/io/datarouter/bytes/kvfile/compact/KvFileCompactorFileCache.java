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
package io.datarouter.bytes.kvfile.compact;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.dto.BlockfileNameAndSize;
import io.datarouter.bytes.kvfile.merge.KvFileMergePlan;
import io.datarouter.bytes.kvfile.read.KvFileCollator.KvFileCollatorStrategy;
import io.datarouter.scanner.Scanner;

/**
 * Keeps track of files during a compaction.
 * Can be enhanced with different selection strategies for efficient compaction.
 */
public class KvFileCompactorFileCache{
	private static final Logger logger = LoggerFactory.getLogger(KvFileCompactorFileCache.class);

	private final int targetNumFiles;
	private final boolean prune;
	private final ByteLength readBufferSize;
	private final int memoryFanIn;
	private final int streamingFanIn;
	private final SortedSet<BlockfileNameAndSize> files;

	public KvFileCompactorFileCache(
			int targetNumFiles,
			boolean prune,
			ByteLength readBufferSize,
			int memoryFanIn,
			int streamingFanIn,
			List<BlockfileNameAndSize> files){
		this.targetNumFiles = targetNumFiles;
		this.prune = prune;
		this.readBufferSize = readBufferSize;
		this.memoryFanIn = memoryFanIn;
		this.streamingFanIn = streamingFanIn;
		this.files = new TreeSet<>(BlockfileNameAndSize.COMPARE_SIZE_AND_NAME);
		this.files.addAll(files);
		if(this.files.size() != files.size()){
			String message = String.format("%s != %s", this.files.size(), files.size());
			throw new RuntimeException(message);
		}
	}

	public int numFiles(){
		return files.size();
	}

	public ByteLength totalSize(){
		return BlockfileNameAndSize.totalSize(files);
	}

	public void add(BlockfileNameAndSize file){
		files.add(file);
	}

	public void remove(BlockfileNameAndSize file){
		files.remove(file);
	}

	public boolean hasMoreToMerge(){
		return files.size() > targetNumFiles;
	}

	public Optional<KvFileMergePlan> findNextMergePlan(){
		if(!hasMoreToMerge()){
			return Optional.empty();
		}
		Map<Integer,FilesAtLevel> filesByLevel = splitFilesByLevel();
		Set<BlockfileNameAndSize> candidates = new TreeSet<>(BlockfileNameAndSize.COMPARE_SIZE_AND_NAME);
		List<Integer> candidateLevels = new ArrayList<>();
		FilesAtLevel lowerLevel = Scanner.of(filesByLevel.values()).findFirst().orElseThrow();
		candidates.addAll(lowerLevel.files());
		candidateLevels.add(lowerLevel.level());
		if(candidates.size() == 1 && filesByLevel.size() > 1){
			FilesAtLevel upperLevel = Scanner.of(filesByLevel.values()).skip(1).findFirst().orElseThrow();
			candidates.addAll(upperLevel.files());
			candidateLevels.add(upperLevel.level());
		}
		List<BlockfileNameAndSize> toMerge = chooseFiles(candidates);
		int numRemainingFiles = files.size() - toMerge.size() + 1;
		KvFileCollatorStrategy collatorStrategy = KvFileCollatorStrategy.KEEP_ALL;// fastest option

		//TODO account for targetFiles > 1
		if(prune && numRemainingFiles == 1){// delay pruning till the final merge
			collatorStrategy = KvFileCollatorStrategy.PRUNE_ALL;
		}
		var mergePlan = new KvFileMergePlan(
				files.size(),
				BlockfileNameAndSize.totalSize(files),
				candidateLevels,
				toMerge,
				collatorStrategy);
		return Optional.of(mergePlan);
	}

	private List<BlockfileNameAndSize> chooseFiles(Set<BlockfileNameAndSize> candidates){
		int remainingToMergeAllLevels = files.size() - targetNumFiles + 1;
		int maxMemoryFiles = Math.min(remainingToMergeAllLevels, memoryFanIn);
		var sizeLimiter = new SizeLimiter(readBufferSize);
		List<BlockfileNameAndSize> toMergeMemory = Scanner.of(candidates)
				.limit(maxMemoryFiles)
				.advanceWhile(sizeLimiter::fits)
				.each(sizeLimiter::add)
				.list();
		int maxStreamingFiles = Math.min(remainingToMergeAllLevels, streamingFanIn);
		List<BlockfileNameAndSize> toMergeStreaming = Scanner.of(candidates)
				.limit(maxStreamingFiles)
				.list();
		return toMergeMemory.size() > toMergeStreaming.size() ? toMergeMemory : toMergeStreaming;
	}

	private static class SizeLimiter{
		ByteLength maxSize;
		AtomicLong currentSize = new AtomicLong();

		SizeLimiter(ByteLength maxSize){
			this.maxSize = maxSize;
			currentSize = new AtomicLong();
		}

		boolean fits(BlockfileNameAndSize file){
			return currentSize.get() + file.size() <= maxSize.toBytes();
		}

		void add(BlockfileNameAndSize file){
			currentSize.addAndGet(file.size());
		}
	}

	private record FilesAtLevel(
			int level,
			List<BlockfileNameAndSize> files){
	}

	private Map<Integer,FilesAtLevel> splitFilesByLevel(){
		Map<Integer,List<BlockfileNameAndSize>> grouped = Scanner.of(files)
				.groupBy(file -> level(file.size()));
		return Scanner.of(grouped.entrySet())
				.toMapSupplied(
						Entry::getKey,
						kv -> Scanner.of(kv.getValue())
								.sort(BlockfileNameAndSize.COMPARE_SIZE_AND_NAME)
								.listTo(files -> new FilesAtLevel(kv.getKey(), files)),
						TreeMap::new);
	}

	/**
	 * Effectively rounds up to an exact power of 2.
	 */
	public static int level(long fileSize){
		if(fileSize == 0 || fileSize == 1){
			return 0;
		}
		long highestOneBit = Long.highestOneBit(fileSize - 1);
		return 1 + Long.numberOfTrailingZeros(highestOneBit);
	}

}
