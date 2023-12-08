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
import java.util.function.Supplier;

import io.datarouter.job.storage.triggerlock.TriggerLock;
import io.datarouter.job.storage.triggerlock.TriggerLockKey;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.scanner.WarnOnModifyList;
import io.datarouter.types.MilliTime;

public class JobLock extends BaseDatabean<JobLockKey,JobLock>{

	private MilliTime triggerTime;
	private MilliTime expirationTime;
	private String serverName;

	public static class JobLockFielder extends BaseDatabeanFielder<JobLockKey,JobLock>{

		public JobLockFielder(){
			super(JobLockKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(JobLock databean){
			return List.of(
					new LongEncodedField<>(TriggerLockKey.FieldKeys.triggerTime, databean.triggerTime),
					new LongEncodedField<>(TriggerLock.FieldKeys.expirationTime, databean.expirationTime),
					new StringField(TriggerLock.FieldKeys.serverName, databean.serverName));
		}

	}

	public JobLock(String lockName, MilliTime triggerTime, MilliTime expirationTime, String serverName){
		super(new JobLockKey(lockName));
		this.triggerTime = triggerTime;
		this.expirationTime = expirationTime;
		this.serverName = serverName;
	}

	@Override
	public Supplier<JobLockKey> getKeySupplier(){
		return JobLockKey::new;
	}

	public JobLock(){
		super(new JobLockKey());
	}

	public static List<JobLock> getBefore(List<JobLock> keys, MilliTime before){
		return keys.stream()
				.filter(triggerLockKey -> triggerLockKey.getTriggerTime().isBefore(before))
				.collect(WarnOnModifyList.deprecatedCollector());
	}

	public String getServerName(){
		return serverName;
	}

	public MilliTime getTriggerTime(){
		return triggerTime;
	}

	public MilliTime getExpirationTime(){
		return expirationTime;
	}

}
