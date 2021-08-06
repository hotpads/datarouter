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
package io.datarouter.storage.op.scan.stride;

import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.tuple.Range;

public class StrideSample<PK extends PrimaryKey<PK>>{

	public final String strategy;
	public final Range<PK> range;
	public final PK lastSeenKey;
	public final long numRpcs;
	public final long numKeysTransferred;
	public final long sampleCount;
	public final boolean interrupted;
	public final long totalCount;
	public final boolean isLast;

	public StrideSample(
			String strategy,
			Range<PK> range,
			PK lastSeenKey,
			long numRpcs,
			long numKeysTransferred,
			long sampleCount,
			boolean interrupted,
			long totalCount,
			boolean isLast){
		this.strategy = strategy;
		this.range = range;
		this.lastSeenKey = lastSeenKey;
		this.numRpcs = numRpcs;
		this.numKeysTransferred = numKeysTransferred;
		this.sampleCount = sampleCount;
		this.interrupted = interrupted;
		this.totalCount = totalCount;
		this.isLast = isLast;
	}

	@Override
	public String toString(){
		return String.format("%s counted=%s total=%s interrupted=%s range=%s",
				strategy,
				NumberFormatter.addCommas(sampleCount),
				NumberFormatter.addCommas(totalCount),
				interrupted,
				range);
	}

}