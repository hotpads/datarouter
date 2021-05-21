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
package io.datarouter.filesystem.snapshot.storage.file;

import io.datarouter.filesystem.snapshot.block.BlockType;

public class FileKey{

	public final BlockType type;
	public final int level;
	public final int column;
	public final int fileId;

	public FileKey(
			BlockType type,
			int level,
			int column,
			int fileId){
		this.type = type;
		this.level = level;
		this.column = column;
		this.fileId = fileId;
	}

	public static FileKey root(){
		return new FileKey(BlockType.ROOT, -1, -1, -1);
	}

	public static FileKey branch(int level, int fileId){
		return new FileKey(BlockType.BRANCH, level, -1, fileId);
	}

	public static FileKey leaf(int fileId){
		return new FileKey(BlockType.LEAF, -1, -1, fileId);
	}

	public static FileKey value(int column, int fileId){
		return new FileKey(BlockType.VALUE, -1, column, fileId);
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + column;
		result = prime * result + fileId;
		result = prime * result + level;
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
		FileKey other = (FileKey)obj;
		if(column != other.column){
			return false;
		}
		if(fileId != other.fileId){
			return false;
		}
		if(level != other.level){
			return false;
		}
		if(type != other.type){
			return false;
		}
		return true;
	}

	@Override
	public String toString(){
		return "FileKey ["
				+ "type=" + type + ", "
				+ "level=" + level + ", "
				+ "column=" + column + ", "
				+ "fileId=" + fileId + "]";
	}

}
