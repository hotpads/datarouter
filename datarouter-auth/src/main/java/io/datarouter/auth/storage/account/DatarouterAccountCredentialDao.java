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
package io.datarouter.auth.storage.account;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.account.DatarouterAccountCredential.DatarouterAccountCredentialFielder;
import io.datarouter.model.databean.FieldlessIndexEntry;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.PutMethod;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.IndexingNodeFactory;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import io.datarouter.storage.node.op.index.UniqueIndexReader;
import io.datarouter.util.Require;
import io.datarouter.util.string.StringTool;
import io.datarouter.virtualnode.redundant.RedundantIndexedSortedMapStorageNode;

@Singleton
public class DatarouterAccountCredentialDao extends BaseDao implements BaseDatarouterAccountCredentialDao{

	public static class DatarouterAccountCredentialDaoParams extends BaseRedundantDaoParams{

		public final Optional<String> tableName;

		public DatarouterAccountCredentialDaoParams(List<ClientId> clientIds){
			super(clientIds);
			tableName = Optional.empty();
		}

		public DatarouterAccountCredentialDaoParams(List<ClientId> clientIds, String tableName){
			super(clientIds);
			Require.isTrue(StringTool.notNullNorEmpty(tableName));
			this.tableName = Optional.of(tableName);
		}

	}

	private final IndexedSortedMapStorageNode<DatarouterAccountCredentialKey,DatarouterAccountCredential,
			DatarouterAccountCredentialFielder> node;
	private final UniqueIndexReader<DatarouterAccountCredentialKey,DatarouterAccountCredential,
			DatarouterAccountCredentialByAccountNameKey, FieldlessIndexEntry<
			DatarouterAccountCredentialByAccountNameKey,DatarouterAccountCredentialKey, DatarouterAccountCredential>>
			byAccountName;

	@Inject
	public DatarouterAccountCredentialDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			IndexingNodeFactory indexingNodeFactory,
			DatarouterAccountCredentialDaoParams params){
		super(datarouter);

		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					var builder = nodeFactory.create(clientId, DatarouterAccountCredential
							::new, DatarouterAccountCredentialFielder::new)
							.withIsSystemTable(true);
					params.tableName.ifPresent(builder::withTableName);

					IndexedSortedMapStorageNode<DatarouterAccountCredentialKey,DatarouterAccountCredential,
							DatarouterAccountCredentialFielder> node = builder.build();
					return node;
				})
				.listTo(RedundantIndexedSortedMapStorageNode::new);
		byAccountName = indexingNodeFactory.createKeyOnlyManagedIndex(DatarouterAccountCredentialByAccountNameKey::new,
				node).build();
		datarouter.register(node);
	}

	@Override
	public void put(DatarouterAccountCredential databean){
		node.put(databean);
	}

	@Override
	public void insertOrBust(DatarouterAccountCredential databean){
		node.put(databean, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
	}

	@Override
	public void updateIgnore(DatarouterAccountCredential databean){
		node.put(databean, new Config().setPutMethod(PutMethod.UPDATE_IGNORE));
	}

	@Override
	public void updateMultiIgnore(Collection<DatarouterAccountCredential> databeans){
		node.putMulti(databeans, new Config().setPutMethod(PutMethod.UPDATE_IGNORE));
	}

	@Override
	public DatarouterAccountCredential get(DatarouterAccountCredentialKey key){
		return node.get(key);
	}

	@Override
	public Scanner<DatarouterAccountCredential> scan(){
		return node.scan();
	}

	@Override
	public Scanner<DatarouterAccountCredential> scanMulti(Collection<DatarouterAccountCredentialKey> keys){
		return node.scanMulti(keys);
	}

	@Override
	public Scanner<DatarouterAccountCredentialKey> scanKeys(){
		return node.scanKeys();
	}

	@Override
	public Scanner<DatarouterAccountCredential> scanByAccountName(Collection<String> accountNames){
		return Scanner.of(accountNames)
				.map(accountName -> new DatarouterAccountCredentialByAccountNameKey(accountName, null))
				.listTo(byAccountName::scanDatabeansWithPrefixes);
	}

	@Override
	public boolean exists(DatarouterAccountCredentialKey key){
		return node.exists(key);
	}

	@Override
	public void delete(DatarouterAccountCredentialKey key){
		node.delete(key);
	}

	@Override
	public void deleteByAccountName(String accountName){
		byAccountName.scanKeysWithPrefix(new DatarouterAccountCredentialByAccountNameKey(accountName, null))
				.map(DatarouterAccountCredentialByAccountNameKey::getTargetKey)
				.flush(node::deleteMulti);
	}

	@Override
	public Optional<DatarouterAccountCredential> find(DatarouterAccountCredentialKey key){
		return node.find(key);
	}

}
