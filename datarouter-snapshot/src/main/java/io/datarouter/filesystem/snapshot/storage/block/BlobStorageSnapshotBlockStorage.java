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
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.file.PathbeanKey;

public class BlobStorageSnapshotBlockStorage implements SnapshotBlockStorage{

	private final Directory blobStorage;

	public BlobStorageSnapshotBlockStorage(Directory blobStorage){
		this.blobStorage = blobStorage;
	}

	/*-------------- add ----------------*/

	@Override
	public void addRootBlock(EncodedBlock encodedBlock){
		add(SnapshotPaths.rootFile(), new CompressedBlock(encodedBlock.chunks));
	}

	@Override
	public void addBranchBlock(SnapshotPaths paths, CacheBlockKey cacheBlockKey, CompressedBlock block){
		add(paths.branchBlock(cacheBlockKey), block);
	}

	@Override
	public void addLeafBlock(SnapshotPaths paths, CacheBlockKey cacheBlockKey, CompressedBlock block){
		add(paths.leafBlock(cacheBlockKey), block);
	}

	@Override
	public void addValueBlock(SnapshotPaths paths, CacheBlockKey cacheBlockKey, CompressedBlock block){
		add(paths.valueBlock(cacheBlockKey), block);
	}

	private void add(String path, CompressedBlock block){
		blobStorage.writeChunks(PathbeanKey.of(path), block.chunkScanner());
	}

	/*---------------- get ---------------*/

	@Override
	public byte[] getRootBlock(){
		return getBlock(SnapshotPaths.rootBlock());
	}

	@Override
	public byte[] getBranchBlock(SnapshotPaths paths, BlockKey blockKey){
		return getBlock(paths.branchBlock(blockKey.toCacheBlockKey()));
	}

	@Override
	public byte[] getLeafBlock(SnapshotPaths paths, BlockKey blockKey){
		return getBlock(paths.leafBlock(blockKey.toCacheBlockKey()));
	}

	@Override
	public byte[] getValueBlock(SnapshotPaths paths, BlockKey blockKey){
		return getBlock(paths.valueBlock(blockKey.toCacheBlockKey()));
	}

	private byte[] getBlock(String path){
		return blobStorage.read(PathbeanKey.of(path));
	}

	/*-------------- delete ---------------*/

	@Override
	public void deleteRootBlock(){
		delete(SnapshotPaths.rootFile());
	}

	@Override
	public void deleteBranchBlock(SnapshotPaths paths, CacheBlockKey cacheBlockKey){
		delete(paths.branchBlock(cacheBlockKey));
	}

	@Override
	public void deleteLeafBlock(SnapshotPaths paths, CacheBlockKey cacheBlockKey){
		delete(paths.leafBlock(cacheBlockKey));
	}

	@Override
	public void deleteValueBlock(SnapshotPaths paths, CacheBlockKey cacheBlockKey){
		delete(paths.valueBlock(cacheBlockKey));
	}

	private void delete(String path){
		blobStorage.delete(PathbeanKey.of(path));
	}

}
