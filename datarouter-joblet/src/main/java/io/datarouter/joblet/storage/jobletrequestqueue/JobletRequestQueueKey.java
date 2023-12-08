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
package io.datarouter.joblet.storage.jobletrequestqueue;

import io.datarouter.joblet.enums.JobletPriority;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.util.ComparableTool;

public record JobletRequestQueueKey(
		JobletType<?> type,
		JobletPriority priority)
implements Comparable<JobletRequestQueueKey>{

	public String getQueueName(){
		return type.getShortQueueName() + "-" + priority.getComparableName();
	}

	/*---------------- Object ------------------*/

	@Override
	public String toString(){
		return getQueueName();
	}

	/*------------- Comparable -------------------*/

	@Override
	public int compareTo(JobletRequestQueueKey other){
		int diff = ComparableTool.nullFirstCompareTo(type, other.type);
		if(diff != 0){
			return diff;
		}
		return ComparableTool.nullFirstCompareTo(priority, other.priority);
	}

}
