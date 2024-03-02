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
package io.datarouter.joblet.dto;

import io.datarouter.joblet.execute.JobletProcessor;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.util.todo.ConvertToRecord;

@ConvertToRecord
public class JobletTypeSummary{

	private final JobletType<?> jobletTypeEnum;
	private final int numThreads;
	private final int numRunning;

	public JobletTypeSummary(JobletProcessor processor){
		this.jobletTypeEnum = processor.getJobletType();
		this.numThreads = processor.getThreadCount();
		this.numRunning = processor.getNumRunningJoblets();
	}

	public String getJobletType(){
		return jobletTypeEnum.getPersistentString();
	}

	public int getNumThreads(){
		return numThreads;
	}

	public int getNumRunning(){
		return numRunning;
	}

}
