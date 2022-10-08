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
package io.datarouter.filesystem.snapshot.storage.block;

import io.datarouter.filesystem.snapshot.block.BlockType;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;

public record CacheBlockKey(
		SnapshotKey snapshotKey,
		BlockType type,
		int level,
		int column,
		int blockId){

	public static CacheBlockKey root(SnapshotKey snapshotKey){
		return new CacheBlockKey(snapshotKey, BlockType.ROOT, -1, -1, -1);
	}

	public static CacheBlockKey branch(SnapshotKey snapshotKey, int level, int id){
		return new CacheBlockKey(snapshotKey, BlockType.BRANCH, level, -1, id);
	}

	public static CacheBlockKey leaf(SnapshotKey snapshotKey, int id){
		return new CacheBlockKey(snapshotKey, BlockType.LEAF, -1, -1, id);
	}

	public static CacheBlockKey value(SnapshotKey snapshotKey, int column, int id){
		return new CacheBlockKey(snapshotKey, BlockType.VALUE, -1, column, id);
	}

}
