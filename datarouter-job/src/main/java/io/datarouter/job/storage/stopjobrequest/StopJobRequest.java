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
package io.datarouter.job.storage.stopjobrequest;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.InstantField;
import io.datarouter.model.field.imp.comparable.InstantFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class StopJobRequest extends BaseDatabean<StopJobRequestKey,StopJobRequest>{

	private String username;
	private Instant requestExpiration;
	private Instant jobTriggerDeadline;//jobs triggered after this time should be ignored instead of stopped

	public static class FieldKeys{
		public static final StringFieldKey username = new StringFieldKey("username");
		public static final InstantFieldKey requestExpiration = new InstantFieldKey("requestExpiration");
		public static final InstantFieldKey jobTriggerDeadline = new InstantFieldKey("jobTriggerDeadline");
	}


	public StopJobRequest(){
		super(new StopJobRequestKey());
	}

	public StopJobRequest(
			String jobServerName,
			Instant requestExpiration,
			Instant jobTriggerDeadline,
			String jobClass,
			String username){
		super(new StopJobRequestKey(jobServerName, jobClass));
		this.username = username;
		this.requestExpiration = requestExpiration;
		this.jobTriggerDeadline = jobTriggerDeadline;
	}

	public static class StopJobRequestFielder extends BaseDatabeanFielder<StopJobRequestKey,StopJobRequest>{

		public StopJobRequestFielder(){
			super(StopJobRequestKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(StopJobRequest databean){
			return List.of(
					new StringField(FieldKeys.username, databean.username),
					new InstantField(FieldKeys.requestExpiration, databean.requestExpiration),
					new InstantField(FieldKeys.jobTriggerDeadline, databean.jobTriggerDeadline));
		}

	}

	@Override
	public Supplier<StopJobRequestKey> getKeySupplier(){
		return StopJobRequestKey::new;
	}

	public String getUsername(){
		return username;
	}

	public Instant getRequestExpiration(){
		return requestExpiration;
	}

	public Instant getJobTriggerDeadline(){
		return jobTriggerDeadline;
	}

}
