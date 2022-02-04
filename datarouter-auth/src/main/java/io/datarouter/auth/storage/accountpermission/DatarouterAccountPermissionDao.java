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
package io.datarouter.auth.storage.accountpermission;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.accountpermission.DatarouterAccountPermission.DatarouterAccountPermissionFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.Require;
import io.datarouter.util.string.StringTool;
import io.datarouter.virtualnode.redundant.RedundantIndexedSortedMapStorageNode;

@Singleton
public class DatarouterAccountPermissionDao extends BaseDao implements BaseDatarouterAccountPermissionDao{

	public static class DatarouterAccountPermissionDaoParams extends BaseRedundantDaoParams{

		public final Optional<String> tableName;

		public DatarouterAccountPermissionDaoParams(List<ClientId> clientIds){
			super(clientIds);
			tableName = Optional.empty();
		}

		public DatarouterAccountPermissionDaoParams(List<ClientId> clientIds, String tableName){
			super(clientIds);
			Require.isTrue(StringTool.notNullNorEmpty(tableName));
			this.tableName = Optional.of(tableName);
		}

	}

	private final SortedMapStorageNode<
			DatarouterAccountPermissionKey,
			DatarouterAccountPermission,
			DatarouterAccountPermissionFielder> node;

	@Inject
	public DatarouterAccountPermissionDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterAccountPermissionDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					var builder = nodeFactory.create(
							clientId,
							DatarouterAccountPermission::new,
							DatarouterAccountPermissionFielder::new)
							.withTag(Tag.DATAROUTER)
							.withTableName(params.tableName);

					IndexedSortedMapStorageNode<
							DatarouterAccountPermissionKey,
							DatarouterAccountPermission,
							DatarouterAccountPermissionFielder> node = builder.build();
					return node;
				})
				.listTo(RedundantIndexedSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	@Override
	public void put(DatarouterAccountPermission databean){
		node.put(databean);
	}

	@Override
	public void deleteWithPrefix(DatarouterAccountPermissionKey prefix){
		node.deleteWithPrefix(prefix);
	}

	@Override
	public void delete(DatarouterAccountPermissionKey key){
		node.delete(key);
	}

	@Override
	public Scanner<DatarouterAccountPermissionKey> scanKeys(){
		return node.scanKeys();
	}

	@Override
	public Scanner<DatarouterAccountPermissionKey> scanKeysWithPrefixes(
			Collection<DatarouterAccountPermissionKey> prefixes){
		return node.scanKeysWithPrefixes(prefixes);
	}

}
