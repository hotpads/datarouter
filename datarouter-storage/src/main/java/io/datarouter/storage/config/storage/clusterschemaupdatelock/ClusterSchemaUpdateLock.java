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
package io.datarouter.storage.config.storage.clusterschemaupdatelock;

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
import io.datarouter.model.util.CommonFieldSizes;

public class ClusterSchemaUpdateLock extends BaseDatabean<ClusterSchemaUpdateLockKey,ClusterSchemaUpdateLock>{

	private String statement;
	private String serverName;
	private Instant triggerTime;

	public static class FieldKeys{
		public static final StringFieldKey statement = new StringFieldKey("statement")
				.withSize(CommonFieldSizes.MAX_LENGTH_TEXT);
		public static final StringFieldKey serverName = new StringFieldKey("serverName");
		public static final InstantFieldKey triggerTime = new InstantFieldKey("triggerTime");
	}

	public static class ClusterSchemaUpdateLockFielder
	extends BaseDatabeanFielder<ClusterSchemaUpdateLockKey,ClusterSchemaUpdateLock>{

		public ClusterSchemaUpdateLockFielder(){
			super(ClusterSchemaUpdateLockKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(ClusterSchemaUpdateLock databean){
			return List.of(
					new StringField(FieldKeys.statement, databean.statement),
					new StringField(FieldKeys.serverName, databean.serverName),
					new InstantField(FieldKeys.triggerTime, databean.triggerTime));
		}

	}

	public ClusterSchemaUpdateLock(){
		super(new ClusterSchemaUpdateLockKey());
	}

	public ClusterSchemaUpdateLock(
			Integer buildId,
			String statement,
			String serverName,
			Instant triggerTime){
		super(ClusterSchemaUpdateLockKey.createKeyWithHashedSqlStatement(buildId, statement));
		this.statement = statement;
		this.serverName = serverName;
		this.triggerTime = triggerTime;
	}

	@Override
	public Supplier<ClusterSchemaUpdateLockKey> getKeySupplier(){
		return ClusterSchemaUpdateLockKey::new;
	}

	public String getStatement(){
		return statement;
	}

	public String getServerName(){
		return serverName;
	}

	public Instant getTriggerTime(){
		return triggerTime;
	}

}
