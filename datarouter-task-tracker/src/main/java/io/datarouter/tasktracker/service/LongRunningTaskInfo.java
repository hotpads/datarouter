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
package io.datarouter.tasktracker.service;

import java.util.concurrent.atomic.AtomicLong;

import io.datarouter.tasktracker.scheduler.LongRunningTaskStatus;
import io.datarouter.tasktracker.scheduler.LongRunningTaskType;

public class LongRunningTaskInfo{

	public final String serverName;
	public final String name;
	public final LongRunningTaskType type;
	public final String triggeredBy;
	public final AtomicLong numItemsProcessed;

	public Long triggerTimeMs;
	public String databeanName;
	public Long finishTimeMs;
	public LongRunningTaskStatus longRunningTaskStatus;
	public Long startTimeMs;
	public String lastItemProcessed;
	public Long heartbeatTimeMs;
	public String exceptionRecordId;

	public LongRunningTaskInfo(String name, String serverName, LongRunningTaskType type, String triggeredBy){
		this.name = name;
		this.serverName = serverName;
		this.type = type;
		this.triggeredBy = triggeredBy;
		this.numItemsProcessed = new AtomicLong();
	}

}
