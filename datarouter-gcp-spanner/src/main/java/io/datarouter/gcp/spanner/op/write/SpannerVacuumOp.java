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
package io.datarouter.gcp.spanner.op.write;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Options.TransactionOption;
import com.google.cloud.spanner.Options.UpdateOption;
import com.google.cloud.spanner.Statement;

import io.datarouter.gcp.spanner.op.SpannerBaseOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.TallyKey;
import io.datarouter.util.string.StringTool;

public class SpannerVacuumOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SpannerBaseOp<Void>{

	private static final String ID = TallyKey.FieldKeys.id.getColumnName();

	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final DatabaseClient client;
	private final String startKey;
	private final String endKey;
	private final Long nowMs;

	public SpannerVacuumOp(DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			String startKey,
			String endKey,
			Long nowMs){
		super("SpannerVacuum: " + fieldInfo.getTableName());
		this.client = client;
		this.fieldInfo = fieldInfo;
		this.startKey = startKey;
		this.endKey = endKey;
		this.nowMs = nowMs;
	}

	@Override
	public Void wrappedCall(){
		UpdateOption[] update = {};
		TransactionOption[] transactionOptions = {};
		String deleteQuery = buildDeleteSql(fieldInfo.getTableName(), this.nowMs, this.startKey, this.endKey);
		client.readWriteTransaction(transactionOptions)
				.run(transaction -> transaction.executeUpdate(Statement.of(deleteQuery), update));
		return null;
	}

	private String buildDeleteSql(String tableName, Long nowMs, String startId, String endId){
		var builder = new StringBuilder();
		builder.append("delete from ");
		builder.append(tableName);
		builder.append(" where ");
		if(startId != null){
			builder
				.append(ID)
				.append(" >= ")
				.append(StringTool.escapeString(startId))
				.append(" and ");
		}
		if(endId != null){
			builder
				.append(ID)
				.append(" < ")
				.append(StringTool.escapeString(endId))
				.append(" and ");
		}
		builder
				.append(Tally.FieldKeys.expirationMs.getColumnName())
				.append(" < ")
				.append("" + nowMs);
		return builder.toString();
	}
}
