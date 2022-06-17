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
package io.datarouter.gcp.spanner.op.read.index;

import java.util.Collection;
import java.util.List;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.KeySet;
import com.google.cloud.spanner.Options;
import com.google.cloud.spanner.Options.ReadOption;
import com.google.cloud.spanner.ReadOnlyTransaction;
import com.google.cloud.spanner.ResultSet;

import io.datarouter.gcp.spanner.field.SpannerFieldCodecs;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class SpannerGetByIndexOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		IK extends PrimaryKey<IK>>
extends SpannerBaseReadIndexOp<PK,D>{

	private final Collection<IK> keys;
	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final String indexName;

	public SpannerGetByIndexOp(
			DatabaseClient client,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			Collection<IK> keys,
			Config config,
			SpannerFieldCodecs fieldCodecs,
			String indexName){
		super(client, config, fieldCodecs, fieldInfo.getTableName());
		this.keys = keys;
		this.fieldInfo = fieldInfo;
		this.indexName = indexName;
	}

	@Override
	public KeySet buildKeySet(){
		return buildKeySet(keys);
	}

	@Override
	public List<D> wrappedCall(){
		try(ReadOnlyTransaction txn = client.readOnlyTransaction()){
			ReadOption[] readOptions = config.findLimit()
					.map(limit -> new ReadOption[]{Options.limit(limit)})
					.orElseGet(() -> new ReadOption[]{});
			try(ResultSet rs = txn.readUsingIndex(
						tableName,
						indexName,
						buildKeySet(),
						fieldInfo.getPrimaryKeyFieldColumnNames(),
						readOptions)){
				List<PK> keyList = createFromResultSet(
						rs,
						fieldInfo.getPrimaryKeySupplier(),
						fieldInfo.getPrimaryKeyFields());
				try(ResultSet databeanRs = txn.read(
							tableName,
							buildKeySet(keyList),
							fieldInfo.getFieldColumnNames(),
							readOptions)){
					return createFromResultSet(
							databeanRs,
							fieldInfo.getDatabeanSupplier(),
							fieldInfo.getFields());
				}
			}
		}
	}

}
