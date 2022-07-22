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
package io.datarouter.joblet.enums;

import io.datarouter.enums.StringMappedEnum;
import io.datarouter.scanner.Scanner;

public enum JobletStatus{

	CREATED("created", false),
	RUNNING("running", true),
	COMPLETE("complete", false),
	INTERRUPTED("interrupted", false),
	FAILED("failed", false),
	TIMED_OUT("timedOut", false);

	public static final StringMappedEnum<JobletStatus> BY_PERSISTENT_STRING
			= new StringMappedEnum<>(values(), value -> value.persistentString);

	public final String persistentString;
	public final boolean isRunning;

	JobletStatus(String persistentString, boolean isRunning){
		this.persistentString = persistentString;
		this.isRunning = isRunning;
	}

	public static Scanner<JobletStatus> scan(){
		return Scanner.of(values());
	}

}
