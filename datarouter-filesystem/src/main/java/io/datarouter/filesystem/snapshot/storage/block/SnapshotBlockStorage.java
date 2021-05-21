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

import io.datarouter.filesystem.snapshot.compress.CompressedBlock;
import io.datarouter.filesystem.snapshot.encode.EncodedBlock;
import io.datarouter.filesystem.snapshot.path.SnapshotPaths;

public interface SnapshotBlockStorage
extends SnapshotBlockStorageReader{

	void addRootBlock(EncodedBlock encodedBlock);
	void addBranchBlock(SnapshotPaths paths, CacheBlockKey cacheBlockKey, CompressedBlock compressedBlock);
	void addLeafBlock(SnapshotPaths paths, CacheBlockKey cacheBlockKey, CompressedBlock compressedBlock);
	void addValueBlock(SnapshotPaths paths, CacheBlockKey cacheBlockKey, CompressedBlock compressedBlock);

	void deleteRootBlock();
	void deleteBranchBlock(SnapshotPaths paths, CacheBlockKey cacheBlockKey);
	void deleteLeafBlock(SnapshotPaths paths, CacheBlockKey cacheBlockKey);
	void deleteValueBlock(SnapshotPaths paths, CacheBlockKey cacheBlockKey);

}
