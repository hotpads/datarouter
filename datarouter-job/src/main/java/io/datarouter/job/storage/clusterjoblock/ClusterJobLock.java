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
package io.datarouter.job.storage.clusterjoblock;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.datarouter.job.storage.clustertriggerlock.ClusterTriggerLock;
import io.datarouter.job.storage.clustertriggerlock.ClusterTriggerLockKey;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class ClusterJobLock extends BaseDatabean<ClusterJobLockKey,ClusterJobLock>{

	private Date triggerTime;
	private Date expirationTime;
	private String serverName;

	public static class ClusterJobLockFielder extends BaseDatabeanFielder<ClusterJobLockKey,ClusterJobLock>{

		public ClusterJobLockFielder(){
			super(ClusterJobLockKey::new);
		}

		@SuppressWarnings("deprecation")
		@Override
		public List<Field<?>> getNonKeyFields(ClusterJobLock databean){
			return List.of(
					new DateField(ClusterTriggerLockKey.FieldKeys.triggerTime, databean.triggerTime),
					new DateField(ClusterTriggerLock.FieldKeys.expirationTime, databean.expirationTime),
					new StringField(ClusterTriggerLock.FieldKeys.serverName, databean.serverName));
		}

	}

	public ClusterJobLock(String lockName, Date triggerTime, Date expirationTime, String serverName){
		super(new ClusterJobLockKey(lockName));
		this.triggerTime = triggerTime;
		this.expirationTime = expirationTime;
		this.serverName = serverName;
	}

	@Override
	public Supplier<ClusterJobLockKey> getKeySupplier(){
		return ClusterJobLockKey::new;
	}

	public ClusterJobLock(){
		super(new ClusterJobLockKey());
	}

	public static List<ClusterJobLock> getBefore(List<ClusterJobLock> keys, Date before){
		return keys.stream()
				.filter(triggerLockKey -> triggerLockKey.getTriggerTime().before(before))
				.collect(Collectors.toList());
	}

	public String getServerName(){
		return serverName;
	}

	public Date getTriggerTime(){
		return triggerTime;
	}

	public Date getExpirationTime(){
		return expirationTime;
	}

}
