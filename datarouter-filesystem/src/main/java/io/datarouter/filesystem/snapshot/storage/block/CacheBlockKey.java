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
package io.datarouter.filesystem.snapshot.storage.block;

import io.datarouter.filesystem.snapshot.block.BlockType;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;

public class CacheBlockKey{

	public final SnapshotKey snapshotKey;
	public final BlockType type;
	public final int level;
	public final int column;
	public final int blockId;

	public CacheBlockKey(
			SnapshotKey snapshotKey,
			BlockType type,
			int level,
			int column,
			int blockId){
		this.snapshotKey = snapshotKey;
		this.type = type;
		this.level = level;
		this.column = column;
		this.blockId = blockId;
	}

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

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + column;
		result = prime * result + blockId;
		result = prime * result + level;
		result = prime * result + ((snapshotKey == null) ? 0 : snapshotKey.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		CacheBlockKey other = (CacheBlockKey)obj;
		if(column != other.column){
			return false;
		}
		if(blockId != other.blockId){
			return false;
		}
		if(level != other.level){
			return false;
		}
		if(snapshotKey == null){
			if(other.snapshotKey != null){
				return false;
			}
		}else if(!snapshotKey.equals(other.snapshotKey)){
			return false;
		}
		if(type != other.type){
			return false;
		}
		return true;
	}

	@Override
	public String toString(){
		return "CacheBlockKey ["
				+ "snapshotKey=" + snapshotKey + ", "
				+ "type=" + type + ", "
				+ "level=" + level + ", "
				+ "column=" + column + ", "
				+ "blockId=" + blockId + "]";
	}

}
