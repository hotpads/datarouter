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
package io.datarouter.storage.op.scan.stride.internal;

import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.util.tuple.Range;

public class OffsettingStrideSample<PK extends PrimaryKey<PK>>{

	public final PK lastSeenKey;
	public final long sampleCount;

	public OffsettingStrideSample(PK lastSeenKey, long sampleCount){
		this.lastSeenKey = lastSeenKey;
		this.sampleCount = sampleCount;
	}

	public InternalStrideSample<PK> toStrideSample(Range<PK> range, boolean interrupted){
		return new InternalStrideSample<>(
				"stride",
				range,
				lastSeenKey,
				1,
				1,
				sampleCount,
				interrupted);
	}

}