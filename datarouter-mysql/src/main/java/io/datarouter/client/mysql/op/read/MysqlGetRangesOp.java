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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;

import io.datarouter.client.mysql.ddl.domain.MysqlTableOptions;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.node.MysqlReaderNode;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.util.MysqlPreparedStatementBuilder;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.client.mysql.util.SqlBuilder;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.Client;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader;
import io.datarouter.storage.util.DatarouterCounters;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.tuple.Range;

public class MysqlGetRangesOp<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseMysqlOp<List<D>>{

	private final MysqlReaderNode<PK,D,F> node;
	private final MysqlFieldCodecFactory fieldCodecFactory;
	private final MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder;
	private final Collection<Range<PK>> ranges;
	private final Config config;

	public MysqlGetRangesOp(Datarouter datarouter, MysqlReaderNode<PK,D,F> node,
			MysqlFieldCodecFactory fieldCodecFactory, MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder,
			Collection<Range<PK>> ranges, Config config){
		super(datarouter, node.getClientNames(), config.getOption(Isolation.KEY).orElse(Isolation.DEFAULT), true);
		this.node = node;
		this.fieldCodecFactory = fieldCodecFactory;
		this.mysqlPreparedStatementBuilder = mysqlPreparedStatementBuilder;
		this.ranges = ranges;
		this.config = config;
	}

	@Override
	public List<D> runOnce(){
		Client client = node.getClient();
		String opName = SortedStorageReader.OP_getRange;
		Connection connection = getConnection(node.getFieldInfo().getClientId().getName());
		PreparedStatement statement = mysqlPreparedStatementBuilder.getInRanges(config, node.getFieldInfo()
				.getTableName(), node.getFieldInfo().getFields(), ranges, node.getFieldInfo().getPrimaryKeyFields(),
				SqlBuilder.PRIMARY_KEY_INDEX_NAME, MysqlTableOptions.make(node.getFieldInfo()))
				.toPreparedStatement(connection);
		List<D> result = MysqlTool.selectDatabeans(fieldCodecFactory, node.getFieldInfo(), statement);
		DatarouterCounters.incClientNodeCustom(client.getType(), opName + " selects", client.getName(), node.getName(),
				1L);
		DatarouterCounters.incClientNodeCustom(client.getType(), opName + " rows", client.getName(), node.getName(),
				CollectionTool.size(result));
		return result;
	}

}
