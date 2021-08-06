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
package io.datarouter.instrumentation.task;

import java.time.Instant;

public class TaskTrackerDto{

	public final TaskTrackerKeyDto key;
	public final String longRunningTaskType;
	public final Instant startTime;
	public final Instant finishTime;
	public final Instant heartbeatTime;
	public final String jobExecutionStatus;
	public final String triggeredBy;
	public final Long numItemsProcessed;
	public final String lastItemProcessed;
	public final String exceptionRecordId;

	public TaskTrackerDto(
			TaskTrackerKeyDto key,
			String longRunningTaskType,
			Instant startTime,
			Instant finishTime,
			Instant heartbeatTime,
			String jobExecutionStatus,
			String triggeredBy,
			Long numItemsProcessed,
			String lastItemProcessed,
			String exceptionRecordId){
		this.key = key;
		this.longRunningTaskType = longRunningTaskType;
		this.startTime = startTime;
		this.finishTime = finishTime;
		this.heartbeatTime = heartbeatTime;
		this.jobExecutionStatus = jobExecutionStatus;
		this.triggeredBy = triggeredBy;
		this.numItemsProcessed = numItemsProcessed;
		this.lastItemProcessed = lastItemProcessed;
		this.exceptionRecordId = exceptionRecordId;
	}

}
