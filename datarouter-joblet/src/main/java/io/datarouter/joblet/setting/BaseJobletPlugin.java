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
package io.datarouter.joblet.setting;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.job.config.BaseJobPlugin;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletTypeGroup;

public abstract class BaseJobletPlugin extends BaseJobPlugin{

	private final List<JobletType<?>> jobletTypes = new ArrayList<>();

	protected void addJobletType(JobletType<?> jobletType){
		jobletTypes.add(jobletType);
	}

	protected void addJobletTypeGroup(JobletTypeGroup jobletTypeGroup){
		jobletTypes.addAll(jobletTypeGroup.getAll());
	}

	public List<JobletType<?>> getJobletTypes(){
		return jobletTypes;
	}

}
