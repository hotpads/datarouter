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
package io.datarouter.job.storage.triggerlock;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.job.storage.joblock.JobLock;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.MilliTimeToLongFieldCodec;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.types.MilliTime;

public class TriggerLock extends BaseDatabean<TriggerLockKey,TriggerLock>{

	private MilliTime expirationTime;
	private String serverName;

	public static class FieldKeys{
		public static final LongEncodedFieldKey<MilliTime> expirationTime = new LongEncodedFieldKey<>(
				"expirationTime",
				new MilliTimeToLongFieldCodec());
		public static final StringFieldKey serverName = new StringFieldKey("serverName");
	}

	public static class TriggerLockFielder extends BaseDatabeanFielder<TriggerLockKey,TriggerLock>{

		public TriggerLockFielder(){
			super(TriggerLockKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(TriggerLock databean){
			return List.of(
					new LongEncodedField<>(FieldKeys.expirationTime, databean.expirationTime),
					new StringField(FieldKeys.serverName, databean.serverName));
		}
	}

	public TriggerLock(){
		super(new TriggerLockKey());
	}

	public TriggerLock(String jobName, MilliTime triggerTime, MilliTime expirationTime, String serverName){
		super(new TriggerLockKey(jobName, triggerTime));
		this.expirationTime = expirationTime;
		this.serverName = serverName;
	}

	@Override
	public Supplier<TriggerLockKey> getKeySupplier(){
		return TriggerLockKey::new;
	}

	public JobLock toJobLock(){
		return new JobLock(
				getKey().getJobName(),
				getKey().getTriggerTime(),
				expirationTime,
				serverName);
	}

	public String getServerName(){
		return serverName;
	}

	public MilliTime getExpirationTime(){
		return expirationTime;
	}

}
