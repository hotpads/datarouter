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
package io.datarouter.filesystem.snapshot.group.vacuum;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.key.SnapshotKeyDecoder;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.UlidTool;

public class SnapshotVacuumPlanner{

	private final SnapshotVacuumConfig config;
	private final SnapshotKeyDecoder keyDecoder;
	private final List<SnapshotKey> ascKeys;

	public SnapshotVacuumPlanner(SnapshotVacuumConfig config, SnapshotKeyDecoder keyDecoder, List<SnapshotKey> ascKeys){
		this.config = config;
		this.keyDecoder = keyDecoder;
		this.ascKeys = ascKeys;
	}

	public static class SnapshotVacuumPlan{

		public String id = UlidTool.nextUlid();
		public List<SnapshotVacuumPlanItem> items = new ArrayList<>();

		void add(SnapshotVacuumPlanItem item){
			items.add(item);
		}

	}

	public static class SnapshotVacuumPlanItem{

		public final SnapshotKey snapshotKey;
		public final String reason;

		public SnapshotVacuumPlanItem(SnapshotKey snapshotKey, String reason){
			this.snapshotKey = snapshotKey;
			this.reason = reason;
		}

	}

	public SnapshotVacuumPlan plan(){
		var plan = new SnapshotVacuumPlan();

		//candidates are in reverse-chronological order after retaining minVersions
		LinkedHashSet<SnapshotKey> remainingCandidatesDesc = Scanner.of(ascKeys)
				.reverse()
				.skip(config.getMinVersions())
				.collect(LinkedHashSet::new);

		config.optTtl().ifPresent(ttl -> {
			List<SnapshotKey> keysToVacuumForTtl = Scanner.of(remainingCandidatesDesc)
					.include(snapshotKey -> keyDecoder.isOlderThan(snapshotKey, ttl))
					.list();
			Scanner.of(keysToVacuumForTtl)
					.each(remainingCandidatesDesc::remove)
					.map(snapshotKey -> new SnapshotVacuumPlanItem(
							snapshotKey,
							String.format("age=%s exceeds ttl=%s", keyDecoder.getAge(snapshotKey), ttl)))
					.forEach(plan::add);
		});

		config.optMaxVersions().ifPresent(maxVersions -> {
			int remainingKeep = maxVersions - config.getMinVersions();
			List<SnapshotKey> keysToVacuumForMaxVersions = Scanner.of(remainingCandidatesDesc)
					.skip(remainingKeep)
					.list();
			Scanner.of(keysToVacuumForMaxVersions)
					.each(remainingCandidatesDesc::remove)
					.map(snapshotKey -> new SnapshotVacuumPlanItem(
							snapshotKey,
							String.format("exceeds maxVersions=%s", maxVersions)))
					.forEach(plan::add);
		});

		return plan;
	}

}
