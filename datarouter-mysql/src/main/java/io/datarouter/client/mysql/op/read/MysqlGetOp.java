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
package io.datarouter.client.mysql.op.read;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;

import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public class MysqlGetOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseMysqlOp<List<D>>{

	private final MysqlFieldCodecFactory fieldCodecFactory;
	private final MysqlGetOpExecutor mysqlGetOpExecutor;
	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;
	private final String opName;
	private final Collection<PK> keys;
	private final Config config;

	public MysqlGetOp(
			DatarouterClients datarouterClients,
			MysqlFieldCodecFactory fieldCodecFactory,
			MysqlGetOpExecutor mysqlGetOpExecutor,
			PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo,
			String opName,
			Collection<PK> keys,
			Config config){
		super(datarouterClients, fieldInfo.getClientId(), Isolation.DEFAULT, true);
		this.fieldCodecFactory = fieldCodecFactory;
		this.fieldInfo = fieldInfo;
		this.opName = opName;
		this.keys = keys;
		this.config = config;
		this.mysqlGetOpExecutor = mysqlGetOpExecutor;
	}

	@Override
	public List<D> runOnce(){
		String indexName = fieldInfo.getDisableForcePrimary() ? null : MysqlTool.PRIMARY_KEY_INDEX_NAME;
		return mysqlGetOpExecutor.execute(
				fieldInfo,
				opName,
				keys,
				config,
				fieldInfo.getFields(),
				this::select,
				getConnection(),
				indexName);
	}

	private List<D> select(PreparedStatement ps){
		return MysqlTool.selectDatabeans(fieldCodecFactory, fieldInfo.getDatabeanSupplier(), fieldInfo.getFields(), ps);
	}

}
