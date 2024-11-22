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

import io.datarouter.model.field.Field;
import io.datarouter.model.field.codec.MilliTimeToLongFieldCodec;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.LongEncodedField;
import io.datarouter.model.field.imp.comparable.LongEncodedFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.types.MilliTime;

public class TriggerLockKey extends BaseRegularPrimaryKey<TriggerLockKey>{

	private String jobName;
	private MilliTime triggerTime;

	public static class FieldKeys{
		public static final StringFieldKey jobName = new StringFieldKey("jobName");
		public static final LongEncodedFieldKey<MilliTime> triggerTime = new LongEncodedFieldKey<>(
				"triggerTime",
				new MilliTimeToLongFieldCodec());
	}

	public TriggerLockKey(){
	}

	public TriggerLockKey(String jobName, MilliTime triggerTime){
		this.jobName = jobName;
		this.triggerTime = triggerTime;
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(FieldKeys.jobName, jobName),
				new LongEncodedField<>(FieldKeys.triggerTime, triggerTime));
	}

	public String getJobName(){
		return jobName;
	}

	public MilliTime getTriggerTime(){
		return triggerTime;
	}

}
