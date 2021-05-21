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

import java.time.Duration;
import java.util.Optional;

public class SnapshotVacuumConfig{

	private static final int DEFAULT_MIN_VERSIONS = 1;

	public static final SnapshotVacuumConfig DEFAULT = new SnapshotVacuumConfig(DEFAULT_MIN_VERSIONS, null, null);

	/**
	 * The strongest setting.  Do not delete these most recent N snapshots.
	 */
	private final Integer minVersions;

	/**
	 * After retaining minVersions, delete snapshots older than the ttl
	 */
	private final Optional<Duration> ttl;

	/**
	 * After retaining minVersions and deleting snapshots older than the ttl, delete the oldest snapshots over
	 * maxVersions
	 */
	public final Optional<Integer> maxVersions;

	public SnapshotVacuumConfig(Integer minVersions, Duration ttl, Integer maxVersions){
		this.minVersions = Optional.of(minVersions).orElse(DEFAULT_MIN_VERSIONS);
		this.ttl = Optional.ofNullable(ttl);
		this.maxVersions = Optional.ofNullable(maxVersions);
	}

	public int getMinVersions(){
		return minVersions;
	}

	public Optional<Duration> optTtl(){
		return ttl;
	}

	public Optional<Integer> optMaxVersions(){
		return maxVersions;
	}

}
