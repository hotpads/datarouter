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
package io.datarouter.filesystem.snapshot.storage.file;

import io.datarouter.filesystem.snapshot.compress.CompressedBlocks;
import io.datarouter.filesystem.snapshot.encode.EncodedBlock;
import io.datarouter.filesystem.snapshot.path.SnapshotPaths;
import io.datarouter.filesystem.snapshot.storage.block.SnapshotBlockStorageReader;

public interface SnapshotFileStorage
extends SnapshotBlockStorageReader{

	void addRootFile(EncodedBlock encodedBlock);
	void addBranchFile(SnapshotPaths paths, FileKey fileKey, CompressedBlocks compressedBlocks);
	void addLeafFile(SnapshotPaths paths, FileKey fileKey, CompressedBlocks compressedBlocks);
	void addValueFile(SnapshotPaths paths, FileKey fileKey, CompressedBlocks compressedBlocks);

	void deleteRootFile();
	void deleteBranchFile(SnapshotPaths paths, FileKey fileKey);
	void deleteLeafFile(SnapshotPaths paths, FileKey fileKey);
	void deleteValueFile(SnapshotPaths paths, FileKey fileKey);

	void deleteAll();

	String toStringDebug();

}
