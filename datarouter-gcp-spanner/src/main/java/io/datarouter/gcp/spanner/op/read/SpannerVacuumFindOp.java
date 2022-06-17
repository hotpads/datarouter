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
package io.datarouter.gcp.spanner.op.read;

import java.util.Optional;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Options.QueryOption;
import com.google.cloud.spanner.ReadOnlyTransaction;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;

import io.datarouter.gcp.spanner.op.SpannerBaseOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.tally.TallyKey;
import io.datarouter.util.string.StringTool;

public class SpannerVacuumFindOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SpannerBaseOp<Optional<String>>{

	private static final String ID = TallyKey.FieldKeys.id.getColumnName();

	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final DatabaseClient client;
	private final String startKey;
	private final Boolean isFirst;
	private final Config config;

	public SpannerVacuumFindOp(DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			String startKey,
			Boolean isFirst,
			Config config){
		super("SpannerFindVacuum: " + fieldInfo.getTableName());
		this.client = client;
		this.fieldInfo = fieldInfo;
		this.startKey = startKey;
		this.isFirst = isFirst;
		this.config = config;
	}

	@Override
	public Optional<String> wrappedCall(){
		String selectKeySql = buildSelectSql(
				fieldInfo.getTableName(),
				this.startKey,
				config.findResponseBatchSize().orElse(10_000),
				this.isFirst);
		if(selectKeySql == null){
			return Optional.empty();
		}
		QueryOption[] queryOptions = {};
		try(ReadOnlyTransaction transaction = client.readOnlyTransaction()){
			ResultSet resultSet = transaction.executeQuery(Statement.of(selectKeySql), queryOptions);
			String endId = resultSet.next() ? resultSet.getString(ID) : null;
			return Optional.ofNullable(endId);
		}
	}

	private String buildSelectSql(String tableName, String startId, int batchSize, boolean first){
		var builder = new StringBuilder();
		builder.append("select " + ID + " from " + tableName);
		if(!first && startId == null){
			return null;
		}
		if(!first){
			builder.append(" where " + ID + " >= " + StringTool.escapeString(startId));
		}
		builder.append(" limit 1 offset " + batchSize);
		return builder.toString();
	}

}
