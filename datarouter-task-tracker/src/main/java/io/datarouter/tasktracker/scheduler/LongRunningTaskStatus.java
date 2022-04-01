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

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.datarouter.enums.StringEnum;
import io.datarouter.instrumentation.task.TaskStatus;

public enum LongRunningTaskStatus implements StringEnum<LongRunningTaskStatus>{
	RUNNING(TaskStatus.RUNNING, "running", true, false),
	SUCCESS(TaskStatus.SUCCESS, "success", false, false),
	ERRORED(TaskStatus.ERRORED, "errored", false, true),
	STOP_REQUESTED(TaskStatus.STOP_REQUESTED, "stopRequested", false, false),
	MAX_DURATION_REACHED(TaskStatus.MAX_DURATION_REACHED, "maxDurationReached", false, false),
	TIMED_OUT(TaskStatus.TIMED_OUT, "timedOut", false, true),
	INTERRUPTED(TaskStatus.INTERRUPTED, "interrupted", false, true),
	;

	private static final Map<TaskStatus,LongRunningTaskStatus> BY_TASK_STATUS = Arrays.stream(values())
			.collect(Collectors.toMap(LongRunningTaskStatus::getStatus, Function.identity()));

	private final TaskStatus status;
	private final String persistentString;
	private final boolean isRunning;
	public final boolean isBadState;

	LongRunningTaskStatus(TaskStatus status, String persistentString, boolean isRunning, boolean isBadState){
		this.status = status;
		this.persistentString = persistentString;
		this.isRunning = isRunning;
		this.isBadState = isBadState;
	}

	public static LongRunningTaskStatus fromTaskStatus(TaskStatus status){
		return BY_TASK_STATUS.get(status);
	}

	public static LongRunningTaskStatus fromPersistentStringStatic(String str){
		return StringEnum.getEnumFromString(values(), str, null);
	}

	@Override
	public LongRunningTaskStatus fromPersistentString(String str){
		return fromPersistentStringStatic(str);
	}

	public TaskStatus getStatus(){
		return status;
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

	public boolean isRunning(){
		return isRunning;
	}

}
