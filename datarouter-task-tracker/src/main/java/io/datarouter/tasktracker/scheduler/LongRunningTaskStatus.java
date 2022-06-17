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
package io.datarouter.tasktracker.scheduler;

import io.datarouter.enums.MappedEnum;
import io.datarouter.instrumentation.task.TaskStatus;

public enum LongRunningTaskStatus{
	RUNNING(TaskStatus.RUNNING, "running", true, false),
	SUCCESS(TaskStatus.SUCCESS, "success", false, false),
	ERRORED(TaskStatus.ERRORED, "errored", false, true),
	STOP_REQUESTED(TaskStatus.STOP_REQUESTED, "stopRequested", false, false),
	MAX_DURATION_REACHED(TaskStatus.MAX_DURATION_REACHED, "maxDurationReached", false, false),
	TIMED_OUT(TaskStatus.TIMED_OUT, "timedOut", false, true),
	INTERRUPTED(TaskStatus.INTERRUPTED, "interrupted", false, true);

	public static final MappedEnum<LongRunningTaskStatus,String> BY_PERSISTENT_STRING
			= new MappedEnum<>(values(), value -> value.persistentString);
	public static final MappedEnum<LongRunningTaskStatus,TaskStatus> BY_TASK_STATUS
			= new MappedEnum<>(values(), value -> value.status);

	public final TaskStatus status;
	public final String persistentString;
	public final boolean isRunning;
	public final boolean isBadState;

	LongRunningTaskStatus(TaskStatus status, String persistentString, boolean isRunning, boolean isBadState){
		this.status = status;
		this.persistentString = persistentString;
		this.isRunning = isRunning;
		this.isBadState = isBadState;
	}

}
