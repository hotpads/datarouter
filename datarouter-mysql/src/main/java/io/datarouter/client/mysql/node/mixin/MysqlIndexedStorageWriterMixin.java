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
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.write.MysqlDeleteByIndexOp;
import io.datarouter.client.mysql.op.write.MysqlUniqueIndexDeleteOp;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.write.IndexedStorageWriter;
import io.datarouter.storage.node.op.raw.write.IndexedStorageWriter.PhysicalIndexedStorageWriterNode;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;

public interface MysqlIndexedStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends PhysicalIndexedStorageWriterNode<PK,D,F>, MysqlStorageMixin{

	@Override
	public default void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		String opName = IndexedStorageWriter.OP_deleteUnique;
		MysqlUniqueIndexDeleteOp<PK,D,F> op = new MysqlUniqueIndexDeleteOp<>(getDatarouter(),
				getMysqlPreparedStatementBuilder(), this, ListTool.wrap(uniqueKey), config);
		MysqlOpRetryTool.tryNTimes(new SessionExecutor<>(getDatarouter().getClientPool(), op, getTraceName(opName)),
				config);
	}

	@Override
	public default void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		String opName = IndexedStorageWriter.OP_deleteMultiUnique;
		if(CollectionTool.isEmpty(uniqueKeys)){
			return;// avoid starting txn
		}
		MysqlUniqueIndexDeleteOp<PK,D,F> op = new MysqlUniqueIndexDeleteOp<>(getDatarouter(),
				getMysqlPreparedStatementBuilder(), this, uniqueKeys, config);
		MysqlOpRetryTool.tryNTimes(new SessionExecutor<>(getDatarouter().getClientPool(), op, getTraceName(opName)),
				config);
	}

	@Override
	public default <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		BaseMysqlOp<Long> op = new MysqlDeleteByIndexOp<>(getDatarouter(), this, getMysqlPreparedStatementBuilder(),
				keys, config);
		MysqlOpRetryTool.tryNTimes(new SessionExecutor<>(getDatarouter().getClientPool(), op,
				IndexedStorageWriter.OP_deleteMultiUnique), config);
	}

}
