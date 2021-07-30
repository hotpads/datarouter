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
package io.datarouter.job.storage.clustertriggerlock;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import io.datarouter.job.storage.clusterjoblock.ClusterJobLock;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.DateFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class ClusterTriggerLock extends BaseDatabean<ClusterTriggerLockKey,ClusterTriggerLock>{

	private Date expirationTime;
	private String serverName;

	public static class FieldKeys{
		@SuppressWarnings("deprecation")
		public static final DateFieldKey expirationTime = new DateFieldKey("expirationTime");
		public static final StringFieldKey serverName = new StringFieldKey("serverName");
	}

	public static class ClusterTriggerLockFielder extends BaseDatabeanFielder<ClusterTriggerLockKey,ClusterTriggerLock>{

		public ClusterTriggerLockFielder(){
			super(ClusterTriggerLockKey::new);
		}

		@SuppressWarnings("deprecation")
		@Override
		public List<Field<?>> getNonKeyFields(ClusterTriggerLock databean){
			return List.of(
					new DateField(FieldKeys.expirationTime, databean.expirationTime),
					new StringField(FieldKeys.serverName, databean.serverName));
		}
	}

	public ClusterTriggerLock(){
		super(new ClusterTriggerLockKey());
	}

	public ClusterTriggerLock(String jobName, Date triggerTime, Date expirationTime, String serverName){
		super(new ClusterTriggerLockKey(jobName, triggerTime));
		this.expirationTime = expirationTime;
		this.serverName = serverName;
	}

	@Override
	public Supplier<ClusterTriggerLockKey> getKeySupplier(){
		return ClusterTriggerLockKey::new;
	}

	public ClusterJobLock toClusterJobLock(){
		return new ClusterJobLock(getKey().getJobName(), getKey().getTriggerTime(), expirationTime, serverName);
	}

	public String getServerName(){
		return serverName;
	}

	public Date getExpirationTime(){
		return expirationTime;
	}

}
