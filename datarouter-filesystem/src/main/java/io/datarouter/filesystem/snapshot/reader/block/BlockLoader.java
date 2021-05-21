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
package io.datarouter.filesystem.snapshot.reader.block;

import io.datarouter.filesystem.snapshot.block.Block;
import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.block.branch.BranchBlock;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlock;
import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.block.value.ValueBlock;
import io.datarouter.filesystem.snapshot.reader.block.LeafBlockRangeLoader.LeafBlockRange;
import io.datarouter.scanner.Scanner;

/**
 * Layer for copying blocks from storage to memory and validating the checksum.
 */
public interface BlockLoader{

	default RootBlock root(BlockKey key){
		return (RootBlock)get(key);
	}

	default BranchBlock branch(BlockKey key){
		return (BranchBlock)get(key);
	}

	default LeafBlock leaf(BlockKey key){
		return (LeafBlock)get(key);
	}

	Scanner<LeafBlock> leafRange(LeafBlockRange range);

	default ValueBlock value(BlockKey key){
		return (ValueBlock)get(key);
	}

	default Block get(BlockKey key){
		switch(key.type){
		case ROOT:
			return root(key);
		case BRANCH:
			return branch(key);
		case LEAF:
			return leaf(key);
		case VALUE:
			return value(key);
		default:
			throw new IllegalStateException("unknown BlockType " + key.type);
		}
	}

}
