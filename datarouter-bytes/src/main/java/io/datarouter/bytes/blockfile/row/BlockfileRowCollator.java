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
package io.datarouter.bytes.blockfile.row;

import java.util.List;
import java.util.function.Function;

import io.datarouter.scanner.BaseLinkedScanner;
import io.datarouter.scanner.Scanner;

/**
 * Collate BlockfileRows using only the binary data, not requiring the objects to be decoded.
 *
 * Facilitates sorting much more data than fits in memory.
 */
public class BlockfileRowCollator{

	public enum BlockfileRowCollatorStrategy{
		KEEP_ALL(BlockfileRowCollator::keepAll),
		PRUNE_VERSIONS(BlockfileRowCollator::pruneVersions),
		PRUNE_ALL(BlockfileRowCollator::pruneAll);

		public final Function<List<Scanner<BlockfileRow>>,Scanner<BlockfileRow>> method;

		private BlockfileRowCollatorStrategy(Function<List<Scanner<BlockfileRow>>,Scanner<BlockfileRow>> method){
			this.method = method;
		}
	}

	/**
	 * Fastest option
	 * Merge entries keeping all versions and ops, including duplicates.
	 * Assumes input is sorted by key+version+op.
	 */
	public static Scanner<BlockfileRow> keepAll(List<Scanner<BlockfileRow>> readers){
		return Scanner.of(readers)
				.collateV2(Function.identity(), BlockfileRow::compareKeyVersionOpOptimized);
	}

	/**
	 * Merge entries keeping the latest version.
	 * Keeps deletes.
	 * Assumes input is sorted by key+version+op.
	 * If multiple entries have same latest version then selection is undefined.
	 */
	public static Scanner<BlockfileRow> pruneVersions(List<Scanner<BlockfileRow>> readers){
		return Scanner.of(readers)
				.collateV2(Function.identity(), BlockfileRow::compareKeyVersionOpOptimized)
				.link(BlockfileRowCollatorPruneVersionsScanner::new);
	}

	/**
	 * Merge entries keeping the latest version.
	 * If lastest version is a DELETE then it's omitted.
	 * Assumes input is sorted by key+version.
	 * If multiple entries have same latest version then selection is undefined.
	 */
	public static Scanner<BlockfileRow> pruneAll(List<Scanner<BlockfileRow>> readers){
		return Scanner.of(readers)
				.collateV2(Function.identity(), BlockfileRow::compareKeyVersionOpOptimized)
				.link(BlockfileRowCollatorPruneDeletesScanner::new);
	}

	/**
	 * Assumes non-null entries.
	 * Assumes inputs are sorted by version asc.
	 * Keep the last entry for each key, the one with the latest version.
	 */
	public static class BlockfileRowCollatorPruneVersionsScanner
	extends BaseLinkedScanner<BlockfileRow,BlockfileRow>{

		private BlockfileRow peeked;

		public BlockfileRowCollatorPruneVersionsScanner(Scanner<BlockfileRow> input){
			super(input);
		}

		@Override
		public boolean advanceInternal(){
			current = peeked;
			peeked = null;
			if(current == null && input.advance()){
				current = input.current();
			}
			while(input.advance()){
				if(BlockfileRow.equalsKeyOptimized(current, input.current())){
					// Key is the same as the previous.  Overwrite the current field and continue searching.
					current = input.current();
				}else{
					// Found a new key
					peeked = input.current();
					return true;
				}
			}
			return current != null;
		}

	}

	/**
	 * Assumes non-null entries.
	 * Assumes inputs are sorted by version asc.
	 * Keep the last entry for each key, the one with the latest version.
	 */
	public static class BlockfileRowCollatorPruneDeletesScanner
	extends BaseLinkedScanner<BlockfileRow,BlockfileRow>{

		private BlockfileRow peeked;

		public BlockfileRowCollatorPruneDeletesScanner(Scanner<BlockfileRow> input){
			super(input);
		}

		@Override
		public boolean advanceInternal(){
			current = peeked;
			peeked = null;
			if(current == null && input.advance()){
				current = input.current();
			}
			while(input.advance()){
				if(BlockfileRow.equalsKeyOptimized(current, input.current())){
					// Key is the same as the previous.  Overwrite the current field and continue searching.
					current = input.current();
				}else{
					// Found a new key
					if(current.op() == BlockfileRowOp.DELETE){
						// We're at the last entry for the key, but it's a DELETE.
						// Skip returning it and continue searching.
						current = input.current();
					}else{
						// We're at the last entry for the key, and it's not a DELETE.
						// Return it, and stash the next one in the peeked field.
						peeked = input.current();
						return true;
					}
				}
			}
			return current != null
					&& current.op() != BlockfileRowOp.DELETE;
		}

	}

}
