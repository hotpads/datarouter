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
package io.datarouter.nodewatch.storage.tablecount;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.nodewatch.storage.tablecount.TableCount.TableCountFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;

@Singleton
public class DatarouterTableCountDao extends BaseDao{

	public static class DatarouterTableCountDaoParams extends BaseRedundantDaoParams{

		public DatarouterTableCountDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final SortedMapStorageNode<TableCountKey,TableCount,TableCountFielder> node;

	@Inject
	public DatarouterTableCountDao(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterTableCountDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<TableCountKey,TableCount,TableCountFielder> node =
							nodeFactory.create(clientId, TableCount::new, TableCountFielder::new)
							.withIsSystemTable(true)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	//currently not depending on table sort order
	public List<TableCount> getForTable(String clientName, String tableName){
		var prefix = TableCountKey.createClientTableKey(clientName, tableName);
		return node.scanWithPrefix(prefix)
				.list();
	}

	public Scanner<TableCount> scan(){
		return node.scan();
	}

	public void put(TableCount databean){
		node.put(databean);
	}

	public void putMulti(Collection<TableCount> databeans){
		node.putMulti(databeans);
	}

	public void deleteWithPrefix(TableCountKey prefix){
		node.deleteWithPrefix(prefix);
	}

	public Scanner<TableCount> scanWithPrefix(TableCountKey prefix){
		return node.scanWithPrefix(prefix);
	}

}
