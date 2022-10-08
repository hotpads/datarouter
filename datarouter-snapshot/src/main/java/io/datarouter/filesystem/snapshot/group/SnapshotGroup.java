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
package io.datarouter.filesystem.snapshot.group;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.datarouter.filesystem.snapshot.block.Block;
import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.block.leaf.LeafBlock;
import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.encode.RootBlockDecoder;
import io.datarouter.filesystem.snapshot.group.groupops.SnapshotGroupDeleteOps;
import io.datarouter.filesystem.snapshot.group.groupops.SnapshotGroupFileReadOps;
import io.datarouter.filesystem.snapshot.group.groupops.SnapshotGroupKeyReadOps;
import io.datarouter.filesystem.snapshot.group.groupops.SnapshotGroupVacuumOps;
import io.datarouter.filesystem.snapshot.group.groupops.SnapshotGroupWriteOps;
import io.datarouter.filesystem.snapshot.group.vacuum.SnapshotVacuumConfig;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.key.SnapshotKeyDecoder;
import io.datarouter.filesystem.snapshot.path.SnapshotPathsRegistry;
import io.datarouter.filesystem.snapshot.reader.block.BlockLoader;
import io.datarouter.filesystem.snapshot.reader.block.DecodingBlockLoader;
import io.datarouter.filesystem.snapshot.reader.block.DecodingBlockLoaderFactory;
import io.datarouter.filesystem.snapshot.reader.block.LeafBlockRangeLoader.LeafBlockRange;
import io.datarouter.filesystem.snapshot.storage.block.BlobStorageSnapshotBlockStorage;
import io.datarouter.filesystem.snapshot.storage.block.CachingBlockStorageReader;
import io.datarouter.filesystem.snapshot.storage.block.SnapshotBlockStorage;
import io.datarouter.filesystem.snapshot.storage.block.SnapshotBlockStorageReader;
import io.datarouter.filesystem.snapshot.storage.file.BlobStorageSnapshotFileStorage;
import io.datarouter.filesystem.snapshot.storage.file.SnapshotFileStorage;
import io.datarouter.filesystem.snapshot.web.SnapshotRecordStringDecoder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.Directory;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.lang.ReflectionTool;

public class SnapshotGroup implements BlockLoader{

	private static final Subpath ID = new Subpath("id");
	private static final Subpath FILE = new Subpath("file");

	private final String groupId;

	private final SnapshotPathsRegistry pathsRegistry;
	private final RootBlockDecoder rootBlockDecoder;
	private final DecodingBlockLoaderFactory decodingBlockLoaderFactory;

	private final Directory directory;
	private final Directory cacheDirectory;
	private final SnapshotKeyDecoder snapshotKeyDecoder;
	private final Class<? extends SnapshotRecordStringDecoder> snapshotRecordDecoderClass;
	private final SnapshotVacuumConfig vacuumConfig;

	private final Directory idDirectory;
	private final Directory fileDirectory;

	//TODO make this a cache with eviction
	private final Map<SnapshotKey,DecodingBlockLoader> decodingBlockLoaderBySnapshotKey;

	public SnapshotGroup(
			String groupId,
			SnapshotPathsRegistry pathsRegistry,
			RootBlockDecoder rootBlockDecoder,
			DecodingBlockLoaderFactory decodingBlockLoaderFactory,
			Directory directory,
			Directory cacheDirectory,
			Class<? extends SnapshotKeyDecoder> snapshotKeyDecoderClass,
			Class<? extends SnapshotRecordStringDecoder> snapshotRecordDecoderClass,
			SnapshotVacuumConfig vacuumConfig){
		this.groupId = groupId;
		this.pathsRegistry = pathsRegistry;
		this.rootBlockDecoder = rootBlockDecoder;
		this.decodingBlockLoaderFactory = decodingBlockLoaderFactory;
		this.directory = directory;
		this.cacheDirectory = cacheDirectory;
		this.snapshotKeyDecoder = ReflectionTool.create(snapshotKeyDecoderClass);
		this.snapshotRecordDecoderClass = snapshotRecordDecoderClass;
		this.vacuumConfig = vacuumConfig;
		idDirectory = directory.subdirectory(ID);
		fileDirectory = directory.subdirectory(FILE);
		//TODO create directories if they don't exist
		decodingBlockLoaderBySnapshotKey = new ConcurrentHashMap<>();
	}

	public SnapshotGroupWriteOps writeOps(){
		return new SnapshotGroupWriteOps(
				this,
				groupId,
				decodingBlockLoaderFactory,
				cacheDirectory,
				idDirectory,
				decodingBlockLoaderBySnapshotKey);
	}

	public SnapshotGroupFileReadOps fileReadOps(){
		return new SnapshotGroupFileReadOps(
				groupId,
				idDirectory,
				fileDirectory);
	}

	public SnapshotGroupKeyReadOps keyReadOps(boolean cacheOk){
		return new SnapshotGroupKeyReadOps(
				this,
				fileReadOps(),
				snapshotKeyDecoder,
				cacheOk);
	}

	public SnapshotGroupDeleteOps deleteOps(){
		return new SnapshotGroupDeleteOps(
				this,
				pathsRegistry,
				directory,
				idDirectory,
				fileDirectory);
	}

	public SnapshotGroupVacuumOps vacuumOps(){
		return new SnapshotGroupVacuumOps(
				this,
				groupId,
				fileDirectory,
				snapshotKeyDecoder,
				vacuumConfig,
				fileReadOps(),
				keyReadOps(false));
	}

	/*---------------- BlockLoader ------------*/

	@Override
	public Block get(BlockKey blockKey){
		return blockLoader(blockKey.snapshotKey()).get(blockKey);
	}

	@Override
	public Scanner<LeafBlock> leafRange(LeafBlockRange range){
		return blockLoader(range.snapshotKey).leafRange(range);
	}

	private DecodingBlockLoader blockLoader(SnapshotKey snapshotKey){
		return decodingBlockLoaderBySnapshotKey.computeIfAbsent(
				snapshotKey,
				$ -> makeDecodingBlockLoader(snapshotKey));
	}

	/*---------------- other --------------*/

	public String getDirectoryLocation(){
		return String.format("%s:%s", directory.getBucket(), directory.getRootPath());
	}

	public String getGroupId(){
		return groupId;
	}

	public SnapshotKey getSnapshotKey(String snapshotId){
		return new SnapshotKey(groupId, snapshotId);
	}

	public Class<? extends SnapshotRecordStringDecoder> getSnapshotEntryDecoderClass(){
		return snapshotRecordDecoderClass;
	}

	private DecodingBlockLoader makeDecodingBlockLoader(SnapshotKey snapshotKey){
		SnapshotBlockStorageReader blockStorageReader = makeStorageReader(snapshotKey.snapshotId());
		byte[] rootBytes = blockStorageReader.getRootBlock();
		RootBlock rootBlock = rootBlockDecoder.decode(rootBytes);
		return decodingBlockLoaderFactory.create(rootBlock, blockStorageReader);
	}

	public SnapshotBlockStorageReader makeStorageReader(String snapshotId){
		SnapshotBlockStorageReader snapshotFileStorage = makeSnapshotFileStorage(snapshotId);
		return cacheDirectory == null
				? snapshotFileStorage
				: new CachingBlockStorageReader(snapshotFileStorage, makeCacheStorage(snapshotId));
	}

	public SnapshotFileStorage makeSnapshotFileStorage(String snapshotId){
		return new BlobStorageSnapshotFileStorage(
				fileDirectory.subdirectory(new Subpath(snapshotId)));
	}

	public SnapshotBlockStorage makeCacheStorage(String snapshotId){
		return new BlobStorageSnapshotBlockStorage(
				cacheDirectory.subdirectory(new Subpath(snapshotId)));
	}

}
