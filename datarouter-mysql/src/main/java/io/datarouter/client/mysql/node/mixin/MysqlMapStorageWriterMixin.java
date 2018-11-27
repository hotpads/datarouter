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
package io.datarouter.client.mysql.node.mixin;

import java.util.Collection;

import io.datarouter.client.mysql.execution.MysqlOpRetryTool;
import io.datarouter.client.mysql.execution.SessionExecutor;
import io.datarouter.client.mysql.op.write.MysqlDeleteAllOp;
import io.datarouter.client.mysql.op.write.MysqlDeleteOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.write.MapStorageWriter;
import io.datarouter.storage.node.op.raw.write.MapStorageWriter.PhysicalMapStorageWriterNode;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;

public interface MysqlMapStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends PhysicalMapStorageWriterNode<PK,D,F>, MysqlStorageMixin{

	@Override
	public default void deleteAll(Config config){
		String opName = MapStorageWriter.OP_deleteAll;
		MysqlDeleteAllOp<PK,D,F> op = new MysqlDeleteAllOp<>(getDatarouter(), this, config);
		MysqlOpRetryTool.tryNTimes(new SessionExecutor<>(getDatarouter().getClientPool(), op, getTraceName(opName)),
				config);
	}

	@Override
	public default void delete(PK key, Config config){
		String opName = MapStorageWriter.OP_delete;
		MysqlDeleteOp<PK,D,F> op = new MysqlDeleteOp<>(getDatarouter(), this, getMysqlPreparedStatementBuilder(),
				ListTool.wrap(key), config);
		MysqlOpRetryTool.tryNTimes(new SessionExecutor<>(getDatarouter().getClientPool(), op, getTraceName(opName)),
				config);
	}

	@Override
	public default void deleteMulti(Collection<PK> keys, Config config){
		String opName = MapStorageWriter.OP_deleteMulti;
		if(CollectionTool.isEmpty(keys)){
			return;// avoid starting txn
		}
		MysqlDeleteOp<PK,D,F> op = new MysqlDeleteOp<>(getDatarouter(), this, getMysqlPreparedStatementBuilder(), keys,
				config);
		MysqlOpRetryTool.tryNTimes(new SessionExecutor<>(getDatarouter().getClientPool(), op, getTraceName(opName)),
				config);
	}

}
