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
package io.datarouter.filesystem.snapshot.encode;

import java.util.Comparator;

import io.datarouter.filesystem.snapshot.entry.SnapshotEntry;
import io.datarouter.filesystem.snapshot.writer.BlockQueue.FileIdsAndEndings;

public interface LeafBlockEncoder extends DataBlockEncoder{

	final Comparator<LeafBlockEncoder> BLOCK_ID_COMPARATOR = Comparator.comparingInt(LeafBlockEncoder::blockId);

	void add(int blockId, long keyId, SnapshotEntry entry, int[] valueBlockIds, int[] valueIndexes);

	byte[] firstKey();

	void assertKeysSorted();

	int blockId();

	int firstValueBlockId(int column);

	int numValueBlocks(int column);

	EncodedBlock encode(FileIdsAndEndings[] fileIdsAndEndings);

}
