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
package io.datarouter.client.mysql.op.read;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;

import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.node.MysqlReaderNode;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;

public class MysqlGetKeysOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseMysqlOp<List<PK>>{

	private final MysqlFieldCodecFactory fieldCodecFactory;
	private final MysqlGetOpExecutor mysqlGetOpExecutor;
	private final MysqlReaderNode<PK,D,F> node;
	private final String opName;
	private final Collection<PK> keys;
	private final Config config;

	public MysqlGetKeysOp(Datarouter datarouter, MysqlFieldCodecFactory fieldCodecFactory,
			MysqlGetOpExecutor mysqlGetOpExecutor, MysqlReaderNode<PK,D,F> node, String opName, Collection<PK> keys,
			Config config){
		super(datarouter, node.getClientNames(), Isolation.DEFAULT, true);
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.opName = opName;
		this.keys = keys;
		this.config = config;
		this.mysqlGetOpExecutor = mysqlGetOpExecutor;
	}

	@Override
	public List<PK> runOnce(){
		return mysqlGetOpExecutor.execute(node, opName, keys, config, node.getFieldInfo().getPrimaryKeyFields(),
				this::select, getConnection(node.getFieldInfo().getClientId().getName()));
	}

	private List<PK> select(PreparedStatement ps){
		return MysqlTool.selectPrimaryKeys(fieldCodecFactory, node.getFieldInfo(), ps);
	}

}
