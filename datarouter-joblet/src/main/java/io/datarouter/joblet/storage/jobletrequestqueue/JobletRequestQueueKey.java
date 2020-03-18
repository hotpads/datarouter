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
package io.datarouter.joblet.storage.jobletrequestqueue;

import java.util.Objects;

import io.datarouter.joblet.enums.JobletPriority;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.lang.ClassTool;

public class JobletRequestQueueKey implements Comparable<JobletRequestQueueKey>{

	public final JobletType<?> type;
	public final JobletPriority priority;

	public JobletRequestQueueKey(JobletType<?> type, JobletPriority priority){
		this.type = type;
		this.priority = priority;
	}

	public String getQueueName(){
		return type.getShortQueueName() + "-" + priority.getComparableName();
	}

	/*---------------- Object ------------------*/

	@Override
	public boolean equals(Object obj){
		if(ClassTool.differentClass(this, obj)){
			return false;
		}
		JobletRequestQueueKey other = (JobletRequestQueueKey)obj;
		return Objects.equals(type, other.type)
				&& Objects.equals(priority, other.priority);
	}

	@Override
	public int hashCode(){
		return Objects.hash(type, priority);
	}

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

	/*-------------- get/set ------------------------*/

	public JobletType<?> getType(){
		return type;
	}

	public JobletPriority getPriority(){
		return priority;
	}

}
