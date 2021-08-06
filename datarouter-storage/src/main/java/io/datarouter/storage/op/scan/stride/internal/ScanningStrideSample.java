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
package io.datarouter.storage.op.scan.stride.internal;

import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.util.tuple.Range;

public class ScanningStrideSample<PK extends PrimaryKey<PK>>{

	public final PK lastSeenKey;
	public final long numRpcs;
	public final long sampleCount;

	public ScanningStrideSample(PK lastSeenKey, long numRpcs, long sampleCount){
		this.lastSeenKey = lastSeenKey;
		this.numRpcs = numRpcs;
		this.sampleCount = sampleCount;
	}

	public InternalStrideSample<PK> toStrideSample(Range<PK> range, boolean interrupted){
		return new InternalStrideSample<>(
				"scanKeys",
				range,
				lastSeenKey,
				numRpcs,
				sampleCount,
				sampleCount,
				interrupted);
	}

}