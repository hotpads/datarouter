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
package io.datarouter.storage.config.storage.clusterschemaupdatelock;

import java.util.List;

import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.key.primary.base.BaseRegularPrimaryKey;
import io.datarouter.util.HashMethods;

public class ClusterSchemaUpdateLockKey extends BaseRegularPrimaryKey<ClusterSchemaUpdateLockKey>{

	private Integer buildId;
	private String statementHash;

	public static class FieldKeys{
		public static final IntegerFieldKey buildId = new IntegerFieldKey("buildId");
		public static final StringFieldKey statementHash = new StringFieldKey("statementHash");
	}

	public ClusterSchemaUpdateLockKey(){
	}

	public ClusterSchemaUpdateLockKey(Integer buildId, String statementHash){
		this.buildId = buildId;
		this.statementHash = statementHash;
	}

	@Override
	public List<Field<?>> getFields(){
		return List.of(
				new IntegerField(FieldKeys.buildId, buildId),
				new StringField(FieldKeys.statementHash, statementHash));
	}

	public Integer getBuildId(){
		return buildId;
	}

	public String getStatementHash(){
		return statementHash;
	}

	public static ClusterSchemaUpdateLockKey createKeyWithHashedSqlStatement(Integer buildId, String sqlStatement){
		return new ClusterSchemaUpdateLockKey(buildId, HashMethods.md5Hash(sqlStatement));
	}

}
