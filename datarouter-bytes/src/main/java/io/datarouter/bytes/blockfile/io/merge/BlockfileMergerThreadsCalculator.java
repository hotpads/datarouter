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
package io.datarouter.bytes.blockfile.io.merge;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import io.datarouter.bytes.blockfile.io.merge.BlockfileMergerParams.BlockfileMergerReadParams;
import io.datarouter.bytes.blockfile.io.storage.BlockfileNameAndSize;
import io.datarouter.scanner.Scanner;

public class BlockfileMergerThreadsCalculator{

	/*
	 * Allocate more prefetch threads to bigger files.
	 * Zero is a special value for small files loaded by the prefetcher that don't need threads later.
	 */
	public record ThreadsForFile(
			BlockfileNameAndSize file,
			int threads){
	}

	private final BlockfileMergePlan plan;
	private final BlockfileMergerReadParams readParams;

	public BlockfileMergerThreadsCalculator(BlockfileMergePlan plan, BlockfileMergerReadParams readParams){
		this.plan = plan;
		this.readParams = readParams;
	}

	public List<ThreadsForFile> calc(){
		long bufferSize = readParams.readBufferSize().toBytes();
		long chunkSize = readParams.readChunkSize().toBytes();
		// Each thread will effectively cache a chunk, so max threads is the max chunks that fit in the buffer
		int maxThreads = Double.valueOf(bufferSize / chunkSize).intValue();
		var remainingFiles = new AtomicInteger(plan.files().size());
		var remainingBytes = new AtomicLong(plan.totalInputSize().toBytes());
		var remainingThreads = new AtomicInteger(maxThreads);
		Predicate<BlockfileNameAndSize> isSmallFilePredicate = file -> file.size() <= chunkSize;
		List<ThreadsForFile> result = new ArrayList<>();

		// Small files will be loaded by the prefetch exec so don't allocate streaming read threads
		Scanner.of(plan.files())
				.include(isSmallFilePredicate)
				.forEach(file -> {
					remainingFiles.decrementAndGet();
					remainingBytes.addAndGet(-file.size());
					// The small file won't actually use a thread so don't count it against the thread budget
					result.add(new ThreadsForFile(file, 1));
				});

		// Divvy up the remaining large files that do use streaming reads
		long fixedRemainingBytes = remainingBytes.get();
		Scanner.of(plan.files())
				.exclude(isSmallFilePredicate)
				// Biggest first for more reliable double calculations
				.sort(Comparator.comparing(BlockfileNameAndSize::size).reversed())
				.forEach(file -> {
					int numThreads;
					if(remainingFiles.get() > 1){
						double pctOfTotalSize = (double)file.size() / (double)fixedRemainingBytes;
						double numThreadsCalc = pctOfTotalSize * maxThreads;
						numThreads = Math.max(1, (int)numThreadsCalc);
					}else{// give all remaining threads to the last/biggest file
						numThreads = Math.max(1, remainingThreads.get());
					}
					remainingFiles.decrementAndGet();
					remainingBytes.addAndGet(-file.size());
					remainingThreads.addAndGet(-numThreads);
					result.add(new ThreadsForFile(file, numThreads));
				});
		return Scanner.of(result)
				.sort(Comparator.comparing(ThreadsForFile::threads).reversed())
				.list();
	}

}
