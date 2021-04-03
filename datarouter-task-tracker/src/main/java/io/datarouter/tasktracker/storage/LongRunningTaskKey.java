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
package io.datarouter.tasktracker.storage;

import java.util.Date;
import java.util.List;

import io.datarouter.instrumentation.task.TaskTrackerKeyDto;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.custom.LongDateField;
import io.datarouter.model.field.imp.custom.LongDateFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;

public class LongRunningTaskKey extends BaseRegularPrimaryKey<LongRunningTaskKey>{

	private String name;
	private Date triggerTime;
	private String serverName;

	public static class FieldKeys{
		public static final StringFieldKey name = new StringFieldKey("name").withColumnName("jobClass");
		public static final LongDateFieldKey triggerTime = new LongDateFieldKey("triggerTime");
		public static final StringFieldKey serverName = new StringFieldKey("serverName");
	}

	public LongRunningTaskKey(){
	}

	public LongRunningTaskKey(String name, Date triggerTime, String serverName){
		this.name = name;
		this.triggerTime = triggerTime;
		this.serverName = serverName;
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new StringField(FieldKeys.name, name),
				new LongDateField(FieldKeys.triggerTime, triggerTime),
				new StringField(FieldKeys.serverName, serverName));
	}

	public String getName(){
		return name;
	}

	public Date getTriggerTime(){
		return triggerTime;
	}

	public String getServerName(){
		return serverName;
	}

	public TaskTrackerKeyDto toDto(){
		return new TaskTrackerKeyDto(name, triggerTime.toInstant(), serverName);
	}

}
