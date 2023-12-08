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
package io.datarouter.nodewatch.storage.tablesample;

import java.util.Collection;
import java.util.List;

import io.datarouter.nodewatch.storage.tablesample.TableSample.TableSampleFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.node.tableconfig.ClientTableEntityPrefixNameWrapper;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.tuple.Range;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterTableSampleDao extends BaseDao{

	public record DatarouterTableSampleDaoParams(List<ClientId> clientIds){
	}

	private final SortedMapStorageNode<TableSampleKey,TableSample,TableSampleFielder> node;

	@Inject
	public DatarouterTableSampleDao(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterTableSampleDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<TableSampleKey,TableSample,TableSampleFielder> node =
							nodeFactory.create(clientId, TableSample::new, TableSampleFielder::new)
							.withTag(Tag.DATAROUTER)
							.disableNodewatch()
							.withTableName("TableRowSample") // Some datastores list 'TableSample' as a reserved word
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public Scanner<TableSample> streamForNode(ClientTableEntityPrefixNameWrapper nodeNames){
		var prefix = TableSampleKey.createSubEntityPrefix(nodeNames);
		var range = new Range<>(prefix, true, prefix, true);
		return node.scan(range);
	}

	public void deleteWithPrefix(TableSampleKey prefix){
		node.deleteWithPrefix(prefix);
	}

	public void put(TableSample databean){
		node.put(databean);
	}

	public void putMulti(Collection<TableSample> databeans){
		node.putMulti(databeans);
	}

	public void delete(TableSampleKey key){
		node.delete(key);
	}

	public TableSample get(TableSampleKey key){
		return node.get(key);
	}

	public Scanner<TableSample> scanWithPrefix(TableSampleKey prefix){
		return node.scanWithPrefix(prefix);
	}

	public Scanner<TableSampleKey> scanKeysWithPrefix(TableSampleKey prefix){
		return node.scanKeysWithPrefix(prefix);
	}

}
