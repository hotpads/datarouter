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
package io.datarouter.filesystem.snapshot.combine;

import java.util.List;

import io.datarouter.filesystem.snapshot.group.dto.SnapshotKeyAndNumRecords;
import io.datarouter.scanner.Scanner;

public class SnapshotCombineTool{

	public static Scanner<List<SnapshotKeyAndNumRecords>> scanSmallestGroups(
			List<SnapshotKeyAndNumRecords> inputs,
			int targetNumSnapshots,
			int maxToMergeAtOnce){
		//TODO make this smarter
		return Scanner.of(inputs)
				.sort(SnapshotKeyAndNumRecords.BY_NUM_RECORDS)
				.limit(inputs.size() - targetNumSnapshots + 1)
				.batch(maxToMergeAtOnce)
				.advanceWhile(batch -> batch.size() > 1);
	}

}
