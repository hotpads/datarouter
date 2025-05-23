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
package io.datarouter.auth.storage.account;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.datarouter.auth.storage.account.DatarouterAccount.DatarouterAccountFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.Require;
import io.datarouter.util.string.StringTool;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAccountDao extends BaseDao{

	public static class DatarouterAccountDaoParams{

		public final List<ClientId> clientIds;
		public final Optional<String> tableName;

		public DatarouterAccountDaoParams(List<ClientId> clientIds){
			this.clientIds = clientIds;
			tableName = Optional.empty();
		}

		public DatarouterAccountDaoParams(List<ClientId> clientIds, String tableName){
			this.clientIds = clientIds;
			Require.isTrue(StringTool.notNullNorEmpty(tableName));
			this.tableName = Optional.of(tableName);
		}

	}

	private final SortedMapStorageNode<DatarouterAccountKey,DatarouterAccount,DatarouterAccountFielder> node;

	@Inject
	public DatarouterAccountDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterAccountDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.<SortedMapStorageNode<DatarouterAccountKey,DatarouterAccount,DatarouterAccountFielder>>map(
						clientId -> {
							var builder = nodeFactory.create(
									clientId,
									DatarouterAccount::new,
									DatarouterAccountFielder::new)
									.withTag(Tag.DATAROUTER);
							params.tableName.ifPresent(builder::withTableName);
							return builder
									.build();
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public void put(DatarouterAccount databean){
		node.put(databean);
	}

	public void putMulti(Collection<DatarouterAccount> databeans){
		node.putMulti(databeans);
	}

	public DatarouterAccount get(DatarouterAccountKey key){
		return node.get(key);
	}

	public Scanner<DatarouterAccount> scanMulti(Collection<DatarouterAccountKey> keys){
		return node.scanMulti(keys);
	}

	public Scanner<DatarouterAccount> scan(){
		return node.scan();
	}

	public Scanner<DatarouterAccountKey> scanKeys(){
		return node.scanKeys();
	}

	public boolean exists(DatarouterAccountKey key){
		return node.exists(key);
	}

	public void delete(DatarouterAccountKey key){
		node.delete(key);
	}

	public Optional<DatarouterAccount> find(DatarouterAccountKey key){
		return node.find(key);
	}

}
