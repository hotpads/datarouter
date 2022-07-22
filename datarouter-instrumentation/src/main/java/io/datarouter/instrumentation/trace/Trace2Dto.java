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
package io.datarouter.instrumentation.trace;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Trace2Dto{

	public final Traceparent traceparent;
	public final String initialParentId;
	public final String context;
	public final String type;
	public final String params;
	public final Long created;
	public final Long ended;
	public final String serviceName;
	public final Integer discardedThreadCount;
	public final Integer totalThreadCount;
	public final Long cpuTimeCreatedNs;
	public final Long cpuTimeEndedNs;
	public final Long memoryAllocatedBytesBegin;
	public final Long memoryAllocatedBytesEnded;
	public final List<TraceSaveReasonType> saveReasons;

	public Trace2Dto(
			Traceparent traceparent,
			String initialParentId,
			String context,
			String type,
			String params,
			Long created,
			Long ended,
			String serviceName,
			Integer discardedThreadCount,
			Integer totalThreadCount,
			Long cpuTimeCreatedNs,
			Long cpuTimeEndedNs,
			Long memoryAllocatedBytesBegin,
			Long memoryAllocatedBytesEnded,
			List<TraceSaveReasonType> saveReasons){
		this.traceparent = traceparent;
		this.created = created;
		this.initialParentId = initialParentId;
		this.context = context;
		this.type = type;
		this.params = params;
		this.ended = ended;
		this.serviceName = serviceName;
		this.discardedThreadCount = discardedThreadCount;
		this.totalThreadCount = totalThreadCount;
		this.cpuTimeCreatedNs = cpuTimeCreatedNs;
		this.cpuTimeEndedNs = cpuTimeEndedNs;
		this.memoryAllocatedBytesBegin = memoryAllocatedBytesBegin;
		this.memoryAllocatedBytesEnded = memoryAllocatedBytesEnded;
		this.saveReasons = saveReasons;
	}

	public long getDurationInNs(){
		return ended - created;
	}

	public long getDurationInMs(){
		return TimeUnit.NANOSECONDS.toMillis(getDurationInNs());
	}

	public static long getCurrentTimeInNs(){
		Instant now = Instant.now();
		return now.getEpochSecond() * 1_000_000_000 + now.getNano();
	}

	/*
	 * TODO remove this method when all data in the Trace table is using nanosecond precision
	 * Note: we need this method to support the old data (using millisecond precision) displayed in the UI
	 */
	public static long convertToMsFromNsIfNecessary(long timeInMsOrNs, long createdTimeInMsOrNs){
		boolean isTimePrecisionInMillis = createdTimeInMsOrNs < 1_000_000_000_000_000L;
		return isTimePrecisionInMillis ? timeInMsOrNs : TimeUnit.NANOSECONDS.toMillis(timeInMsOrNs);
	}

}
