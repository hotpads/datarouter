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
package io.datarouter.filesystem.snapshot.reader.record;

import java.util.Arrays;
import java.util.Comparator;

import io.datarouter.filesystem.snapshot.entry.SnapshotEntry;
import io.datarouter.util.bytes.ByteTool;

public class SnapshotLeafRecord{

	public static final Comparator<SnapshotLeafRecord> KEY_COMPARATOR = Comparator.comparing(
			snapshotLeafRecord -> snapshotLeafRecord.key,
			(a, b) -> Arrays.compareUnsigned(a, b));

	public final long id;
	public final byte[] key;
	public final byte[] value;

	public SnapshotLeafRecord(long id, byte[] key, byte[] value){
		this.id = id;
		this.key = key;
		this.value = value;
	}

	public SnapshotEntry entry(){
		return new SnapshotEntry(key, value, ByteTool.EMPTY_ARRAY_2);
	}

}
