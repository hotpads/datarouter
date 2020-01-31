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
package io.datarouter.joblet.enums;

import java.util.Arrays;

import io.datarouter.joblet.queue.JobletRequestSelector;
import io.datarouter.joblet.queue.selector.MysqlLockForUpdateJobletRequestSelector;
import io.datarouter.joblet.queue.selector.MysqlUpdateAndScanJobletRequestSelector;
import io.datarouter.joblet.queue.selector.SqsJobletRequestSelector;

public enum JobletQueueMechanism{
	JDBC_LOCK_FOR_UPDATE("jdbcLockForUpdate", MysqlLockForUpdateJobletRequestSelector.class),
	JDBC_UPDATE_AND_SCAN("jdbcUpdateAndScan", MysqlUpdateAndScanJobletRequestSelector.class),
	SQS("sqs", SqsJobletRequestSelector.class);

	private final String persistentString;
	private final Class<? extends JobletRequestSelector> selectorClass;

	private JobletQueueMechanism(String persistentString, Class<? extends JobletRequestSelector> selectorClass){
		this.persistentString = persistentString;
		this.selectorClass = selectorClass;
	}

	public static JobletQueueMechanism fromPersistentString(String from){
		return Arrays.stream(values())
				.filter(mechanism -> mechanism.getPersistentString().equals(from))
				.findAny()
				.get();
	}

	public String getPersistentString(){
		return persistentString;
	}

	public Class<? extends JobletRequestSelector> getSelectorClass(){
		return selectorClass;
	}

}
