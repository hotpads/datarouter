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

import java.time.Duration;
import java.util.List;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Key;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Options.TransactionOption;
import com.google.cloud.spanner.Struct;

import io.datarouter.gcp.spanner.op.SpannerBaseOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.TallyKey;

public class SpannerIncrementOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends SpannerBaseOp<Long>{

	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final DatabaseClient client;
	private final String key;
	private final Long incrementAmount;
	private final Config config;

	public SpannerIncrementOp(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			String key,
			Long incrementAmount,
			Config config){
		super("SpannerIncrement: " + fieldInfo.getTableName());
		this.client = client;
		this.fieldInfo = fieldInfo;
		this.key = key;
		this.incrementAmount = incrementAmount;
		this.config = config;
	}

	@Override
	public Long wrappedCall(){
		TransactionOption[] transactionOptions = {};
		long nowMs = System.currentTimeMillis();
		Long expiresAt = config.findTtl()
				.map(Duration::toMillis)
				.map(ttlMs -> nowMs + ttlMs)
				.orElse(null);
		return client.readWriteTransaction(transactionOptions)
				.run(transaction -> {
						Struct row = transaction.readRow(
								fieldInfo.getTableName(),
								Key.of(key),
								List.of(Tally.FieldKeys.tally.getName(), Tally.FieldKeys.expirationMs.getName()));
						Long incrementBy = row == null ? incrementAmount : row.getLong(0) + incrementAmount;
						Long expiration = null;
						if(row == null || row.isNull(Tally.FieldKeys.expirationMs.getColumnName())
								|| row.getLong(Tally.FieldKeys.expirationMs.getColumnName()) < nowMs){
							expiration = expiresAt;
						}else{
							expiration = row.getLong(Tally.FieldKeys.expirationMs.getColumnName());
						}
						var insertOrUpdateTally = Mutation.newInsertOrUpdateBuilder(fieldInfo.getTableName())
								.set(TallyKey.FieldKeys.id.getColumnName())
								.to(key)
								.set(Tally.FieldKeys.tally.getColumnName())
								.to(incrementBy)
								.set(Tally.FieldKeys.expirationMs.getColumnName())
								.to(expiration)
								.build();
						transaction.buffer(insertOrUpdateTally);
						return incrementBy;
					});
	}

}
