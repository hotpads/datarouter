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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.filesystem.snapshot.encode.RootBlockDecoder;
import io.datarouter.filesystem.snapshot.path.SnapshotPathsRegistry;
import io.datarouter.filesystem.snapshot.reader.block.DecodingBlockLoaderFactory;
import io.datarouter.storage.file.Directory;

@Singleton
public class SnapshotGroupFactory{

	@Inject
	private SnapshotPathsRegistry pathsRegistry;
	@Inject
	private RootBlockDecoder rootBlockDecoder;
	@Inject
	private DecodingBlockLoaderFactory decodingBlockLoaderFactory;

	public SnapshotGroupBuilder startBuilder(String groupId, Directory directory){
		return new SnapshotGroupBuilder(
				groupId,
				pathsRegistry,
				rootBlockDecoder,
				decodingBlockLoaderFactory,
				directory);
	}

}
