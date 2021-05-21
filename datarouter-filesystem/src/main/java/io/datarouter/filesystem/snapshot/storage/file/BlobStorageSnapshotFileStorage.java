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

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.compress.CompressedBlock;
import io.datarouter.filesystem.snapshot.compress.CompressedBlocks;
import io.datarouter.filesystem.snapshot.encode.EncodedBlock;
import io.datarouter.filesystem.snapshot.path.SnapshotPaths;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.MultiByteArrayInputStream;
import io.datarouter.storage.util.Subpath;

public class BlobStorageSnapshotFileStorage implements SnapshotFileStorage{

	private static final boolean USE_CHUNK_WRITER = true;

	private final Directory directory;

	public BlobStorageSnapshotFileStorage(Directory directory){
		this.directory = directory;
	}

	/*-------------- add ----------------*/

	@Override
	public void addRootFile(EncodedBlock encodedBlock){
		add(SnapshotPaths.rootFile(), new CompressedBlocks(List.of(new CompressedBlock(encodedBlock.chunks))));
	}

	@Override
	public void addBranchFile(SnapshotPaths paths, FileKey fileKey, CompressedBlocks compressedBlocks){
		add(paths.branchFile(fileKey), compressedBlocks);
	}

	@Override
	public void addLeafFile(SnapshotPaths paths, FileKey fileKey, CompressedBlocks compressedBlocks){
		add(paths.leafFile(fileKey), compressedBlocks);
	}

	@Override
	public void addValueFile(SnapshotPaths paths, FileKey fileKey, CompressedBlocks compressedBlocks){
		add(paths.valueFile(fileKey), compressedBlocks);
	}

	private void add(String path, CompressedBlocks compressedBlocks){
		Iterator<byte[]> chunks = compressedBlocks.chunkIterator();
		if(USE_CHUNK_WRITER){
			var inputStream = new MultiByteArrayInputStream(chunks);
			directory.write(PathbeanKey.of(path), inputStream);
		}else{
			directory.write(PathbeanKey.of(path), chunks);
		}
	}

	/*---------------- get ---------------*/

	@Override
	public byte[] getRootBlock(){
		return directory.read(PathbeanKey.of(SnapshotPaths.rootFile()));
	}

	@Override
	public byte[] getBranchBlock(SnapshotPaths paths, BlockKey blockKey){
		return getBlock(paths.branchFile(blockKey.toFileKey()), blockKey.offset, blockKey.length);
	}

	@Override
	public byte[] getLeafBlock(SnapshotPaths paths, BlockKey blockKey){
		return getBlock(paths.leafFile(blockKey.toFileKey()), blockKey.offset, blockKey.length);
	}

	@Override
	public byte[] getValueBlock(SnapshotPaths paths, BlockKey blockKey){
		return getBlock(paths.valueFile(blockKey.toFileKey()), blockKey.offset, blockKey.length);
	}

	private byte[] getBlock(String path, int offset, int length){
		return directory.read(PathbeanKey.of(path), offset, length);
	}

	/*-------------- delete ---------------*/

	@Override
	public void deleteRootFile(){
		delete(SnapshotPaths.rootFile());
	}

	@Override
	public void deleteBranchFile(SnapshotPaths paths, FileKey fileKey){
		delete(paths.branchFile(fileKey));
	}

	@Override
	public void deleteLeafFile(SnapshotPaths paths, FileKey fileKey){
		delete(paths.leafFile(fileKey));
	}

	@Override
	public void deleteValueFile(SnapshotPaths paths, FileKey fileKey){
		delete(paths.valueFile(fileKey));
	}

	private void delete(String path){
		directory.delete(PathbeanKey.of(path));
	}

	/*------------ deleteAll -----------------*/

	@Override
	public void deleteAll(){
		directory.deleteAll(Subpath.empty());
	}

	/*-------------- debug ------------------*/

	@Override
	public String toStringDebug(){
		return "\n" + directory.scanKeys(Subpath.empty())
				.map(Object::toString)
				.sort()
				.collect(Collectors.joining("\n"));
	}

}
