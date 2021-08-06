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

import io.datarouter.filesystem.snapshot.encode.RootBlockDecoder;
import io.datarouter.filesystem.snapshot.group.vacuum.SnapshotVacuumConfig;
import io.datarouter.filesystem.snapshot.key.SnapshotKeyDecoder;
import io.datarouter.filesystem.snapshot.key.UlidSnapshotKeyDecoder;
import io.datarouter.filesystem.snapshot.path.SnapshotPathsRegistry;
import io.datarouter.filesystem.snapshot.reader.block.DecodingBlockLoaderFactory;
import io.datarouter.filesystem.snapshot.web.SnapshotRecordStringDecoder;
import io.datarouter.storage.file.Directory;

public class SnapshotGroupBuilder{

	private static final Class<? extends SnapshotKeyDecoder> DEFAULT_SNAPSHOT_KEY_DECODER_CLASS
			= UlidSnapshotKeyDecoder.class;

	private final String groupId;

	private final SnapshotPathsRegistry pathsRegistry;
	private final RootBlockDecoder rootBlockDecoder;
	private final DecodingBlockLoaderFactory decodingBlockLoaderFactory;

	private final Directory groupDirectory;

	private Directory cacheDirectory;
	private Class<? extends SnapshotKeyDecoder> snapshotKeyDecoderClass;
	private Class<? extends SnapshotRecordStringDecoder> snapshotRecordStringDecoderClass;
	private SnapshotVacuumConfig vacuumConfig;

	public SnapshotGroupBuilder(
			String groupId,
			SnapshotPathsRegistry pathsRegistry,
			RootBlockDecoder rootBlockDecoder,
			DecodingBlockLoaderFactory decodingBlockLoaderFactory,
			Directory groupDirectory){
		this.groupId = groupId;
		this.pathsRegistry = pathsRegistry;
		this.rootBlockDecoder = rootBlockDecoder;
		this.decodingBlockLoaderFactory = decodingBlockLoaderFactory;
		this.groupDirectory = groupDirectory;
		this.snapshotKeyDecoderClass = DEFAULT_SNAPSHOT_KEY_DECODER_CLASS;
		this.vacuumConfig = SnapshotVacuumConfig.DEFAULT;
	}

	public SnapshotGroupBuilder setCacheStorage(Directory cacheDirectory){
		this.cacheDirectory = cacheDirectory;
		return this;
	}

	public SnapshotGroupBuilder setSnapshotKeyDecoderClass(
			Class<? extends SnapshotKeyDecoder> snapshotKeyDecoderClass){
		this.snapshotKeyDecoderClass = snapshotKeyDecoderClass;
		return this;
	}

	public SnapshotGroupBuilder setSnapshotRecordStringDecoderClass(
			Class<? extends SnapshotRecordStringDecoder> snapshotEntryDecoderClass){
		this.snapshotRecordStringDecoderClass = snapshotEntryDecoderClass;
		return this;
	}

	public SnapshotGroupBuilder setVacuumConfig(SnapshotVacuumConfig vacuumConfig){
		this.vacuumConfig = vacuumConfig;
		return this;
	}

	public SnapshotGroup build(){
		return new SnapshotGroup(
				groupId,
				pathsRegistry,
				rootBlockDecoder,
				decodingBlockLoaderFactory,
				groupDirectory,
				cacheDirectory,
				snapshotKeyDecoderClass,
				snapshotRecordStringDecoderClass,
				vacuumConfig);
	}

}
