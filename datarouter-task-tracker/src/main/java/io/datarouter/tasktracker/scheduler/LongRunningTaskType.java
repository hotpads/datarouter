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

public enum LongRunningTaskType{
	JOB("job"),
	JOBLET("joblet"),
	REQUEST("request"),
	MIGRATION("migration"),
	TEST("test"),
	NOOP("noop");

	public static final MappedEnum<LongRunningTaskType,String> BY_PERSISTENT_STRING
			= new MappedEnum<>(values(), value -> value.persistentString);

	public final String persistentString;

	LongRunningTaskType(String persistentString){
		this.persistentString = persistentString;
	}

}
