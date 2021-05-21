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
package io.datarouter.filesystem.snapshot.group;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

import io.datarouter.filesystem.snapshot.block.Block;
import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlock;
import io.datarouter.filesystem.snapshot.reader.block.BlockLoader;
import io.datarouter.filesystem.snapshot.reader.block.LeafBlockRangeLoader.LeafBlockRange;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.Require;

@Singleton
public class SnapshotGroups implements BlockLoader{

	private final Map<String,SnapshotGroup> groupById;

	public SnapshotGroups(){
		groupById = new ConcurrentHashMap<>();
	}

	public SnapshotGroup register(SnapshotGroup group){
		SnapshotGroup existing = groupById.putIfAbsent(group.getGroupId(), group);
		Require.isNull(existing, "SnapshotGroup was already registered:" + group.getGroupId());
		return group;
	}

	@Override
	public Block get(BlockKey blockKey){
		SnapshotGroup group = groupById.get(blockKey.snapshotKey.groupId);
		return group.get(blockKey);
	}

	@Override
	public Scanner<LeafBlock> leafRange(LeafBlockRange range){
		SnapshotGroup group = groupById.get(range.firstBlockKey.snapshotKey.groupId);
		return group.leafRange(range);
	}

	public Scanner<String> scanIds(){
		return Scanner.of(groupById.keySet());
	}

	public SnapshotGroup getGroup(String groupId){
		return groupById.get(groupId);
	}

}
