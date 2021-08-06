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
package io.datarouter.filesystem.snapshot.entry;

import java.util.Arrays;
import java.util.List;

import io.datarouter.util.array.PagedObjectArray;
import io.datarouter.util.bytes.ByteWriter;

public class SnapshotEntrySlabBuilder{

	private final ByteWriter keyWriter;
	private int keyEnding;
	private final PagedObjectArray<Integer> keyEndings;

	private final ByteWriter valueWriter;
	private int valueEnding;
	private final PagedObjectArray<Integer> valueEndings;

	private final PagedObjectArray<byte[][]> columnValuesList;

	public SnapshotEntrySlabBuilder(){
		keyWriter = new ByteWriter(64);
		keyEnding = 0;
		keyEndings = new PagedObjectArray<>(64);

		valueWriter = new ByteWriter(64);
		valueEnding = 0;
		valueEndings = new PagedObjectArray<>(64);

		columnValuesList = new PagedObjectArray<>(64);
	}

	public void append(byte[] key, byte[] value, byte[][] columnValues){
		keyWriter.bytes(key);
		keyEnding += key.length;
		keyEndings.add(keyEnding);

		valueWriter.bytes(value);
		valueEnding += value.length;
		valueEndings.add(valueEnding);

		columnValuesList.add(columnValues);
	}

	public List<SnapshotEntry> build(){
		int numEntries = keyEndings.size();

		byte[] keySlab = keyWriter.concat();
		byte[] valueSlab = valueWriter.concat();

		SnapshotEntry[] entries = new SnapshotEntry[numEntries];
		for(int i = 0; i < numEntries; ++i){
			int keyFrom = i == 0 ? 0 : keyEndings.get(i - 1);
			int keyTo = keyEndings.get(i);
			int valueFrom = i == 0 ? 0 : valueEndings.get(i - 1);
			int valueTo = valueEndings.get(i);
			entries[i] = new SnapshotEntry(
					keySlab, keyFrom, keyTo,
					valueSlab, valueFrom, valueTo,
					columnValuesList.get(i));
		}

		return Arrays.asList(entries);
	}

}
