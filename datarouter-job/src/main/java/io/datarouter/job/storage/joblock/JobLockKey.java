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
package io.datarouter.job.storage.joblock;

import java.util.List;

import io.datarouter.job.storage.triggerlock.TriggerLockKey;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public class JobLockKey extends BaseRegularPrimaryKey<JobLockKey>{

	private String jobName;

	public JobLockKey(){
	}

	public JobLockKey(String jobName){
		this.jobName = jobName;
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(new StringField(TriggerLockKey.FieldKeys.jobName, jobName));
	}

	public String getJobName(){
		return jobName;
	}

}
