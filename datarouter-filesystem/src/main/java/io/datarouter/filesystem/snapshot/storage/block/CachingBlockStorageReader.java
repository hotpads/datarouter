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

import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.compress.CompressedBlock;
import io.datarouter.filesystem.snapshot.encode.EncodedBlock;
import io.datarouter.filesystem.snapshot.path.SnapshotPaths;

public class CachingBlockStorageReader implements SnapshotBlockStorageReader{

	private static final int MAX_CACHEABLE_BYTES = 1024 * 1024;

	private final SnapshotBlockStorageReader source;
	private final SnapshotBlockStorage cache;

	public CachingBlockStorageReader(
			SnapshotBlockStorageReader back,
			SnapshotBlockStorage front){
		this.source = back;
		this.cache = front;
	}

	/*---------------- get ---------------*/

	@Override
	public byte[] getRootBlock(){
		byte[] bytes = cache.getRootBlock();
		if(bytes == null){
			bytes = source.getRootBlock();
			if(bytes.length < MAX_CACHEABLE_BYTES){
				cache.addRootBlock(new EncodedBlock(bytes));
			}
		}
		return bytes;
	}

	@Override
	public byte[] getBranchBlock(SnapshotPaths paths, BlockKey blockKey){
		byte[] bytes = cache.getBranchBlock(paths, blockKey);
		if(bytes == null){
			bytes = source.getBranchBlock(paths, blockKey);
			if(bytes.length < MAX_CACHEABLE_BYTES){
				cache.addBranchBlock(paths, blockKey.toCacheBlockKey(), new CompressedBlock(bytes));
			}
		}
		return bytes;
	}

	@Override
	public byte[] getLeafBlock(SnapshotPaths paths, BlockKey blockKey){
		byte[] bytes = cache.getLeafBlock(paths, blockKey);
		if(bytes == null){
			bytes = source.getLeafBlock(paths, blockKey);
			if(bytes.length < MAX_CACHEABLE_BYTES){
				cache.addLeafBlock(paths, blockKey.toCacheBlockKey(), new CompressedBlock(bytes));
			}
		}
		return bytes;
	}

	@Override
	public byte[] getValueBlock(SnapshotPaths paths, BlockKey blockKey){
		byte[] bytes = cache.getValueBlock(paths, blockKey);
		if(bytes == null){
			bytes = source.getValueBlock(paths, blockKey);
			if(bytes.length < MAX_CACHEABLE_BYTES){
				cache.addValueBlock(paths, blockKey.toCacheBlockKey(), new CompressedBlock(bytes));
			}
		}
		return bytes;
	}

}
