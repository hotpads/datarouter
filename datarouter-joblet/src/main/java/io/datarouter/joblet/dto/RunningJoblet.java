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

import java.time.Instant;
import java.util.Optional;

import io.datarouter.joblet.model.JobletPackage;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.util.DateTool;

public class RunningJoblet{

	private String name;
	private String id;
	private Instant startedAt;
	private String queueId;
	private String jobletData;

	public RunningJoblet(JobletType<?> jobletType, long id, Instant startedAt, Optional<JobletPackage> jobletPackage){
		this.name = jobletType.getPersistentString();
		this.id = Long.toString(id);
		this.startedAt = startedAt;
		if(jobletPackage.isPresent()){
			this.queueId = jobletPackage.get().getJobletRequest().getQueueId();
			this.jobletData = jobletPackage.get().getJobletData().getData();
		}
	}

	private RunningJoblet(String name, String id, Instant startedAt, String queueId, String jobletData){
		this.name = name;
		this.id = id;
		this.startedAt = startedAt;
		this.queueId = queueId;
		this.jobletData = jobletData;
	}

	public RunningJoblet withoutData(){
		return new RunningJoblet(name, id, startedAt, queueId, null);
	}

	public boolean hasPayload(){
		return jobletData != null;
	}

	public String getName(){
		return name;
	}

	public String getId(){
		return id;
	}

	public String getRunningTimeString(){
		return DateTool.getAgoString(startedAt);
	}

	public String getQueueId(){
		return queueId;
	}

	public String getJobletData(){
		return jobletData;
	}

	public Instant getStartedAt(){
		return startedAt;
	}

}
