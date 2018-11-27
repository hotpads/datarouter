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
package io.datarouter.client.mysql.node;

import java.util.Collection;

import io.datarouter.client.mysql.execution.MysqlOpRetryTool;
import io.datarouter.client.mysql.execution.SessionExecutor;
import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.node.mixin.MysqlIndexedStorageWriterMixin;
import io.datarouter.client.mysql.node.mixin.MysqlMapStorageWriterMixin;
import io.datarouter.client.mysql.node.mixin.MysqlSortedStorageWriterMixin;
import io.datarouter.client.mysql.op.read.MysqlGetOpExecutor;
import io.datarouter.client.mysql.op.write.MysqlPutOp;
import io.datarouter.client.mysql.util.MysqlPreparedStatementBuilder;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import io.datarouter.storage.node.op.raw.write.MapStorageWriter;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;

public class MysqlNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends MysqlReaderNode<PK,D,F>
implements PhysicalIndexedSortedMapStorageNode<PK,D,F>,
		MysqlIndexedStorageWriterMixin<PK,D,F>,
		MysqlSortedStorageWriterMixin<PK,D,F>,
		MysqlMapStorageWriterMixin<PK,D,F>{

	private final Datarouter datarouter;
	private final MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder;

	public MysqlNode(NodeParams<PK,D,F> params, MysqlFieldCodecFactory fieldCodecFactory, Datarouter datarouter,
			MysqlGetOpExecutor mysqlGetOpExecutor, DatarouterClients datarouterClients,
			DatarouterNodes datarouterNodes, MysqlPreparedStatementBuilder mysqlPreparedStatementBuilder){
		super(params, fieldCodecFactory, datarouter, datarouterClients, datarouterNodes, mysqlGetOpExecutor,
				mysqlPreparedStatementBuilder);
		this.datarouter = datarouter;
		this.mysqlPreparedStatementBuilder = mysqlPreparedStatementBuilder;
	}

	@Override
	public Node<PK,D,F> getMaster(){
		return this;
	}

	@Override
	public MysqlPreparedStatementBuilder getMysqlPreparedStatementBuilder(){
		return mysqlPreparedStatementBuilder;
	}

	/*------------------------- MapStorageWriter methods --------------------*/

	@Override
	public void put(D databean, Config config){
		String opName = MapStorageWriter.OP_put;
		MysqlPutOp<PK,D,F> op = new MysqlPutOp<>(datarouter, this, mysqlPreparedStatementBuilder, ListTool.wrap(
				databean), config);
		MysqlOpRetryTool.tryNTimes(new SessionExecutor<>(datarouter.getClientPool(), op, getTraceName(opName)), config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		String opName = MapStorageWriter.OP_putMulti;
		if(CollectionTool.isEmpty(databeans)){
			return;// avoid starting txn
		}
		TracerTool.appendToSpanInfo(TracerThreadLocal.get(), String.valueOf(CollectionTool.size(databeans)));
		MysqlPutOp<PK,D,F> op = new MysqlPutOp<>(datarouter, this, mysqlPreparedStatementBuilder, databeans, config);
		MysqlOpRetryTool.tryNTimes(new SessionExecutor<>(datarouter.getClientPool(), op, getTraceName(opName)), config);
	}

	@Override
	public Datarouter getDatarouter(){
		return datarouter;
	}

}
