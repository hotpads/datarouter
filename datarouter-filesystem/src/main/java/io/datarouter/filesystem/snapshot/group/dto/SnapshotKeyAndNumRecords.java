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
package io.datarouter.filesystem.snapshot.group.dto;

import java.util.Comparator;
import java.util.List;

import io.datarouter.filesystem.snapshot.key.SnapshotKey;

public class SnapshotKeyAndNumRecords{

	public static final Comparator<SnapshotKeyAndNumRecords> BY_NUM_RECORDS = Comparator.comparing(
			input -> input.numRecords);

	public final SnapshotKey key;
	public final long numRecords;

	public SnapshotKeyAndNumRecords(SnapshotKey key, long numRecords){
		this.key = key;
		this.numRecords = numRecords;
	}

	public SnapshotKeyAndNumRecords(SnapshotKeyAndRoot from){
		this(from.key, from.root.numItems());
	}

	public static final long totalRecords(List<SnapshotKeyAndNumRecords> inputs){
		return inputs.stream()
				.mapToLong(input -> input.numRecords)
				.sum();
	}

}
