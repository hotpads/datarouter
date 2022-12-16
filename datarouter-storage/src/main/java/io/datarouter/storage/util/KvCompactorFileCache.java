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

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import io.datarouter.bytes.ByteLength;
import io.datarouter.scanner.Scanner;

/**
 * Keeps track of files during a compaction.
 * Can be enhanced with different selection strategies for efficient compaction.
 */
public class KvCompactorFileCache{

	private final int targetNumFiles;
	private final int fanIn;
	private final ByteLength streamReadsLargerThan;
	private final ByteLength streamWritesLargerThan;
	private final Set<FilenameAndSize> files;

	public KvCompactorFileCache(
			int targetNumFiles,
			int fanIn,
			ByteLength streamReadsLargerThan,
			ByteLength streamWritesLargerThan,
			List<FilenameAndSize> files){
		this.targetNumFiles = targetNumFiles;
		this.fanIn = fanIn;
		this.streamReadsLargerThan = streamReadsLargerThan;
		this.streamWritesLargerThan = streamWritesLargerThan;
		this.files = new HashSet<>(files);
	}

	public int numFiles(){
		return files.size();
	}

	public ByteLength totalSize(){
		return FilenameAndSize.totalSize(files);
	}

	public void add(FilenameAndSize file){
		files.add(file);
	}

	public void remove(FilenameAndSize file){
		files.remove(file);
	}

	public boolean hasMoreToMerge(){
		return files.size() > targetNumFiles;
	}

	public List<FilenameAndSize> getFilesToMerge(){
		//TODO include more small files than the fanIn
		int maxFiles = Math.min(files.size() - targetNumFiles + 1, fanIn);
		//TODO use real maxTotalSize for small file merges
		ByteLength maxTotalSize = ByteLength.MAX;
		var sizeLimiter = new SizeLimiter(maxTotalSize);
		return Scanner.of(files)
				.minN(Comparator.comparing(FilenameAndSize::size), maxFiles)
				.advanceWhile(sizeLimiter::fits)
				.each(sizeLimiter::add)
				.list();
	}

	public Optional<KvFileMergePlan> findNextMergePlan(){
		if(!hasMoreToMerge()){
			return Optional.empty();
		}
		List<FilenameAndSize> toMerge = getFilesToMerge();
		ByteLength totalSize = FilenameAndSize.totalSize(toMerge);
		boolean streamingWrite = totalSize.toBytes() > streamWritesLargerThan.toBytes();
		var mergePlan = new KvFileMergePlan(toMerge, streamingWrite);
		return Optional.of(mergePlan);
	}

	public record KvFileMergePlan(
			List<FilenameAndSize> files,
			boolean streamingWrite){
	}

	private static class SizeLimiter{
		ByteLength maxSize;
		AtomicLong currentSize = new AtomicLong();

		SizeLimiter(ByteLength maxSize){
			this.maxSize = maxSize;
			currentSize = new AtomicLong();
		}

		boolean fits(FilenameAndSize file){
			return currentSize.get() + file.size() <= maxSize.toBytes();
		}

		void add(FilenameAndSize file){
			currentSize.addAndGet(file.size());
		}
	}

}
