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
package io.datarouter.filesystem.snapshot.reader.block;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.model.util.Bytes;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.Require;
import io.datarouter.util.collection.ListTool;

//TODO more generic version for branch/value blocks
public class LeafBlockRangeLoader{

	public static Scanner<LeafBlockRange> splitByFileAndBatch(Scanner<BlockKey> leafBlockKeys, int blocksPerBatch){
		return leafBlockKeys
				.splitBy(key -> key.fileId)
				.concat(keysInFile -> keysInFile
						.batch(blocksPerBatch)
						.map(LeafBlockRange::new))
				.concat(Scanner::of);
	}

	public static class LeafBlockRange{

		public final List<BlockKey> blockKeys;
		public final BlockKey firstBlockKey;
		public final BlockKey lastBlockKey;
		public final SnapshotKey snapshotKey;
		public final int fileId;
		public final int fileFrom;
		public final int fileTo;

		public LeafBlockRange(List<BlockKey> blockKeys){
			this.blockKeys = blockKeys;
			firstBlockKey = blockKeys.get(0);
			lastBlockKey = ListTool.getLast(blockKeys);
			snapshotKey = firstBlockKey.snapshotKey;
			fileId = firstBlockKey.fileId;
			Require.equals(firstBlockKey.fileId, lastBlockKey.fileId);
			fileFrom = firstBlockKey.offset;
			fileTo = lastBlockKey.offset + lastBlockKey.length;
		}

		/**
		 * @return A "virtual" BlockKey spanning multiple blocks
		 */
		public BlockKey rangeBlockKey(){
			int blockId = -1;//for lack of a better value
			int length = fileTo - fileFrom;
			return BlockKey.leaf(firstBlockKey.snapshotKey, blockId, fileId, fileFrom, length);
		}

		public Scanner<Bytes> parse(byte[] multiBlockBytes){
			return Scanner.of(blockKeys)
					.map(blockKey -> {
						int from = blockKey.offset - fileFrom;
						return new Bytes(multiBlockBytes, from, blockKey.length);
					});
		}

		@Override
		public String toString(){
			LinkedHashMap<String,Object> kvs = new LinkedHashMap<>();
			kvs.put("fileId", fileId);
			kvs.put("numBlocks", blockKeys.size());
			kvs.put("firstBlockId", firstBlockKey.blockId);
			kvs.put("lastBlockId", lastBlockKey.blockId);
			kvs.put("fileFrom", fileFrom);
			kvs.put("fileTo", fileTo);
			return kvs.entrySet().stream()
					.map(kv -> String.format("%s=%s", kv.getKey(), kv.getValue()))
					.collect(Collectors.joining(", "));
		}

	}

}
