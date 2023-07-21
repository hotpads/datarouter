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
package io.datarouter.filesystem.snapshot.reader.block;

import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.compress.BlockDecompressor;
import io.datarouter.filesystem.snapshot.compress.BlockDecompressorFactory;
import io.datarouter.filesystem.snapshot.encode.BlockDecoder;
import io.datarouter.filesystem.snapshot.encode.BlockDecoderFactory;
import io.datarouter.filesystem.snapshot.path.SnapshotPaths;
import io.datarouter.filesystem.snapshot.path.SnapshotPathsRegistry;
import io.datarouter.filesystem.snapshot.storage.block.SnapshotBlockStorageReader;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DecodingBlockLoaderFactory{

	@Inject
	private SnapshotPathsRegistry pathsRegistry;
	@Inject
	private BlockDecoderFactory blockDecoderFactory;
	@Inject
	private BlockDecompressorFactory blockDecompressorFactory;

	public DecodingBlockLoader create(RootBlock rootBlock, SnapshotBlockStorageReader snapshotBlockStorageReader){
		BlockDecoder blockDecoder = blockDecoderFactory.create(rootBlock);
		SnapshotPaths paths = pathsRegistry.getPaths(rootBlock.pathFormat());
		BlockDecompressor blockDecompressor = blockDecompressorFactory.create(rootBlock);
		return new DecodingBlockLoader(snapshotBlockStorageReader, paths, blockDecompressor, blockDecoder);
	}

}
