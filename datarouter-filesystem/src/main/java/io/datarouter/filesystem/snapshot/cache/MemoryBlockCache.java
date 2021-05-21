/**
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
package io.datarouter.filesystem.snapshot.cache;

import java.util.concurrent.Executors;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import io.datarouter.filesystem.snapshot.block.Block;
import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlock;
import io.datarouter.filesystem.snapshot.reader.block.BlockLoader;
import io.datarouter.filesystem.snapshot.reader.block.LeafBlockRangeLoader.LeafBlockRange;
import io.datarouter.scanner.Scanner;

/**
 * Thread-safe cache that can be shared between multiple readers.
 *
 * TODO: support mixing multiple snapshots into the same cache
 */
public class MemoryBlockCache implements BlockLoader{

	private final LoadingCache<BlockKey,Block> blocks;

	public MemoryBlockCache(int maxSizeBytes, BlockLoader blockLoader){
		blocks = Caffeine.newBuilder()
				.executor(Executors.newCachedThreadPool())// avoid ForkJoinPool, but not sure of best configuration
				.maximumWeight(maxSizeBytes)
				.weigher((BlockKey key, Block block) -> block.heapSize())
				.build(blockLoader::get);
	}

	@Override
	public Scanner<LeafBlock> leafRange(LeafBlockRange range){
		return Scanner.of(range.blockKeys)
				.map(this::leaf);
	}

	@Override
	public Block get(BlockKey key){
		return blocks.get(key);
	}

}
