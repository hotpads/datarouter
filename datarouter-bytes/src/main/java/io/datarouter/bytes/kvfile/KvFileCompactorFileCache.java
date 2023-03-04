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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.kvfile.KvFileCollator.KvFileCollatorStrategy;
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
	private final Set<KvFileNameAndSize> files;

	public KvFileCompactorFileCache(
			int targetNumFiles,
			boolean prune,
			ByteLength readBufferSize,
			int memoryFanIn,
			int streamingFanIn,
			List<KvFileNameAndSize> files){
		this.targetNumFiles = targetNumFiles;
		this.prune = prune;
		this.readBufferSize = readBufferSize;
		this.memoryFanIn = memoryFanIn;
		this.streamingFanIn = streamingFanIn;
		this.files = new TreeSet<>(KvFileNameAndSize.COMPARE_SIZE_AND_NAME);
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
		return KvFileNameAndSize.totalSize(files);
	}

	public void add(KvFileNameAndSize file){
		files.add(file);
	}

	public void remove(KvFileNameAndSize file){
		files.remove(file);
	}

	public boolean hasMoreToMerge(){
		return files.size() > targetNumFiles;
	}

	private List<KvFileNameAndSize> listFilesToMergeInMemory(){
		int maxFiles = Math.min(files.size() - targetNumFiles + 1, memoryFanIn);
		var sizeLimiter = new SizeLimiter(readBufferSize);
		return Scanner.of(files)
				.limit(maxFiles)
				.advanceWhile(sizeLimiter::fits)
				.each(sizeLimiter::add)
				.list();
	}

	private List<KvFileNameAndSize> listFilesToMergeStreaming(){
		int maxFiles = Math.min(files.size() - targetNumFiles + 1, streamingFanIn);
		return Scanner.of(files)
				.limit(maxFiles)
				.list();
	}

	public Optional<KvFileMergePlan> findNextMergePlan(){
		if(!hasMoreToMerge()){
			return Optional.empty();
		}
		List<KvFileNameAndSize> toMergeMemory = listFilesToMergeInMemory();
		List<KvFileNameAndSize> toMergeStreaming = listFilesToMergeStreaming();
		List<KvFileNameAndSize> toMerge = toMergeMemory.size() > toMergeStreaming.size()
				? toMergeMemory
				: toMergeStreaming;
		int numRemainingFiles = files.size() - toMerge.size() + 1;
		logger.warn(
				"selecting {}/{}->{} from memory={} or streaming={}",
				toMerge.size(),
				files.size(),
				numRemainingFiles,
				toMergeMemory.size(),
				toMergeStreaming.size());
		KvFileCollatorStrategy collatorStrategy = KvFileCollatorStrategy.KEEP_ALL;// fastest option
		if(prune && numRemainingFiles == 1){// delay pruning till the final merge
			collatorStrategy = KvFileCollatorStrategy.PRUNE_ALL;
		}
		var mergePlan = new KvFileMergePlan(toMerge, collatorStrategy);
		return Optional.of(mergePlan);
	}

	public record KvFileMergePlan(
			List<KvFileNameAndSize> files,
			KvFileCollatorStrategy collatorStrategy){

		/* Note that total output size could be less or more based on metadata and compression effects.
		 * It could also be significantly less based on pruning versions and deletes.
		 */
		public ByteLength totalInputSize(){
			return KvFileNameAndSize.totalSize(files);
		}
	}

	private static class SizeLimiter{
		ByteLength maxSize;
		AtomicLong currentSize = new AtomicLong();

		SizeLimiter(ByteLength maxSize){
			this.maxSize = maxSize;
			currentSize = new AtomicLong();
		}

		boolean fits(KvFileNameAndSize file){
			return currentSize.get() + file.size() <= maxSize.toBytes();
		}

		void add(KvFileNameAndSize file){
			currentSize.addAndGet(file.size());
		}
	}

}
