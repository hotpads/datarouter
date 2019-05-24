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

import io.datarouter.client.mysql.MysqlClientType;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.key.unique.UniqueKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;

public class MysqlNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends MysqlReaderNode<PK,D,F>
implements PhysicalIndexedSortedMapStorageNode<PK,D,F>{

	private final MysqlNodeManager mysqlNodeManager;

	public MysqlNode(NodeParams<PK,D,F> params, MysqlClientType mysqlClientType, MysqlNodeManager mysqlNodeManager){
		super(params, mysqlClientType, mysqlNodeManager);
		this.mysqlNodeManager = mysqlNodeManager;
	}

	/*------------------------- MapStorageWriter methods --------------------*/

	@Override
	public void put(D databean, Config config){
		mysqlNodeManager.put(getFieldInfo(), databean, config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		mysqlNodeManager.putMulti(getFieldInfo(), databeans, config);
	}

	@Override
	public void delete(PK key, Config config){
		mysqlNodeManager.delete(getFieldInfo(), key, config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		mysqlNodeManager.deleteMulti(getFieldInfo(), keys, config);
	}

	@Override
	public void deleteAll(Config config){
		mysqlNodeManager.deleteAll(getFieldInfo(), config);
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		mysqlNodeManager.deleteUnique(getFieldInfo(), uniqueKey, config);
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		mysqlNodeManager.deleteMultiUnique(getFieldInfo(), uniqueKeys, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		mysqlNodeManager.deleteByIndex(getFieldInfo(), keys, config);
	}

}
