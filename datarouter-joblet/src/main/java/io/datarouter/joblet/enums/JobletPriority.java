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
import java.util.stream.Stream;

import io.datarouter.util.string.StringTool;

public enum JobletPriority{
	//IMPORTANT: maintain order
	HIGH(10),
	DEFAULT(100),
	LOW(1000);

	private Integer executionOrder;
	private String comparableName;

	private JobletPriority(Integer executionOrder){
		this.executionOrder = executionOrder;
		this.comparableName = StringTool.pad(Integer.toString(executionOrder), '0', 4);
	}

	public static JobletPriority fromExecutionOrder(Integer executionOrder){
		if(executionOrder == null){
			return JobletPriority.LOW;
		}
		for(JobletPriority priority : JobletPriority.values()){
			if(priority.getExecutionOrder() >= executionOrder){
				return priority;
			}
		}
		return JobletPriority.LOW;
	}

	public static Stream<JobletPriority> stream(){
		return Arrays.stream(values());
	}

	/*
	 * return true if this has higher priority than the other
	 */
	public boolean isHigher(JobletPriority other){
		return executionOrder < other.executionOrder;
	}

	public Integer getExecutionOrder(){
		return this.executionOrder;
	}

	public String getComparableName(){
		return comparableName;
	}

	public static JobletPriority getHighestPriority(){
		return JobletPriority.values()[0];
	}

	public static JobletPriority getLowestPriority(){
		return JobletPriority.values()[JobletPriority.values().length - 1];
	}

}
