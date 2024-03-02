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
package io.datarouter.bytes.blockfile.io.write;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.blockfile.index.BlockfileIndexBlockInput;
import io.datarouter.bytes.blockfile.index.BlockfileIndexEntry;

public class BlockfileIndexBlockBuilder{

	private final int maxRecordsPerLevel;
	private final Optional<ByteLength> optTargetBlockSize;
	private final int level;
	private final List<BlockfileIndexEntry> children = new ArrayList<>();
	private long estEncodedSize;

	public BlockfileIndexBlockBuilder(
			int maxRecordsPerLevel,
			Optional<ByteLength> optTargetBlockSize,
			int level){
		this.maxRecordsPerLevel = maxRecordsPerLevel;
		this.optTargetBlockSize = optTargetBlockSize;
		this.level = level;
		this.estEncodedSize = 0;
	}

	public void addChild(BlockfileIndexEntry child, int addToEstEncodedSize){
		children.add(child);
		estEncodedSize += addToEstEncodedSize;
	}

	public int numChildren(){
		return children.size();
	}

	public boolean hasChildren(){
		return !children.isEmpty();
	}

	public long estEncodedSize(){
		return estEncodedSize;
	}

	public boolean isFull(){
		if(optTargetBlockSize.isPresent()
				&& estEncodedSize >= optTargetBlockSize.orElseThrow().toBytes()){
			return true;
		}else if(children.size() >= maxRecordsPerLevel){
			return true;
		}
		return false;
	}

	/*--------- level --------*/

	public int level(){
		return level;
	}

	/*--------- build -------*/

	public BlockfileIndexBlockInput build(
			long globalBlockId,
			long indexBlockId){
		return new BlockfileIndexBlockInput(
				globalBlockId,
				indexBlockId,
				level,
				children);
	}

}
