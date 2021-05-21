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
package io.datarouter.filesystem.snapshot.block;

import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.storage.block.CacheBlockKey;
import io.datarouter.filesystem.snapshot.storage.file.FileKey;

public class BlockKey{

	public final SnapshotKey snapshotKey;
	public final BlockType type;
	public final int level;
	public final int column;
	public final int blockId;
	public final int fileId;
	public final int offset;
	public final int length;

	private BlockKey(
			SnapshotKey snapshotKey,
			BlockType type,
			int level,
			int column,
			int blockId,
			int fileId,
			int offset,
			int length){
		this.snapshotKey = snapshotKey;
		this.type = type;
		this.level = level;
		this.column = column;
		this.blockId = blockId;
		this.fileId = fileId;
		this.offset = offset;
		this.length = length;
	}

	public static BlockKey root(SnapshotKey snapshotKey){
		return new BlockKey(snapshotKey, BlockType.ROOT, -1, -1, -1, -1, -1, -1);
	}

	public static BlockKey branchRoot(SnapshotKey snapshotKey, int maxLevel, int length){
		return branch(snapshotKey, maxLevel, 0, 0, 0, length);
	}

	public static BlockKey branch(SnapshotKey snapshotKey, int level, int blockId, int fileId, int offset, int length){
		return new BlockKey(snapshotKey, BlockType.BRANCH, level, -1, blockId, fileId, offset, length);
	}

	public static BlockKey leaf(SnapshotKey snapshotKey, int blockId, int fileId, int offset, int length){
		return new BlockKey(snapshotKey, BlockType.LEAF, -1, -1, blockId, fileId, offset, length);
	}

	public static BlockKey value(SnapshotKey snapshotKey, int column, int blockId, int fileId, int offset, int length){
		return new BlockKey(snapshotKey, BlockType.VALUE, -1, column, blockId, fileId, offset, length);
	}

	public FileKey toFileKey(){
		return new FileKey(type, level, column, fileId);
	}

	public CacheBlockKey toCacheBlockKey(){
		return new CacheBlockKey(snapshotKey, type, level, column, blockId);
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + column;
		result = prime * result + fileId;
		result = prime * result + blockId;
		result = prime * result + length;
		result = prime * result + level;
		result = prime * result + offset;
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
		BlockKey other = (BlockKey)obj;
		if(column != other.column){
			return false;
		}
		if(fileId != other.fileId){
			return false;
		}
		if(blockId != other.blockId){
			return false;
		}
		if(length != other.length){
			return false;
		}
		if(level != other.level){
			return false;
		}
		if(offset != other.offset){
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
		return "BlockKey ["
				+ "snapshotKey=" + snapshotKey + ", "
				+ "type=" + type + ", "
				+ "level=" + level + ", "
				+ "column=" + column + ", "
				+ "blockId=" + blockId + ", "
				+ "fileId=" + fileId + ", "
				+ "offset=" + offset + ", "
				+ "length=" + length + "]";
	}

}
