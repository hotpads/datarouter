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
package io.datarouter.auth.storage.account.credential.secret;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.datarouter.auth.storage.account.credential.secret.DatarouterAccountSecretCredential.DatarouterAccountSecretCredentialFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.PutMethod;
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
public class DatarouterAccountSecretCredentialDao extends BaseDao{

	public static class DatarouterAccountSecretCredentialDaoParams{

		public final List<ClientId> clientIds;
		public final Optional<String> tableName;

		public DatarouterAccountSecretCredentialDaoParams(List<ClientId> clientIds){
			this.clientIds = clientIds;
			tableName = Optional.empty();
		}

		public DatarouterAccountSecretCredentialDaoParams(List<ClientId> clientIds, String tableName){
			this.clientIds = clientIds;
			Require.isTrue(StringTool.notNullNorEmpty(tableName));
			this.tableName = Optional.of(tableName);
		}

	}

	private final SortedMapStorageNode<
			DatarouterAccountSecretCredentialKey,
			DatarouterAccountSecretCredential,
			DatarouterAccountSecretCredentialFielder> node;

	@Inject
	public DatarouterAccountSecretCredentialDao(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterAccountSecretCredentialDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					var builder = nodeFactory.create(clientId, DatarouterAccountSecretCredential::new,
							DatarouterAccountSecretCredentialFielder::new)
							.withTag(Tag.DATAROUTER)
							.withTableName(params.tableName);
					SortedMapStorageNode<DatarouterAccountSecretCredentialKey,DatarouterAccountSecretCredential,
							DatarouterAccountSecretCredentialFielder> node = builder.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public void insertOrBust(DatarouterAccountSecretCredential databean){
		node.put(databean, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
	}

	public void updateIgnore(DatarouterAccountSecretCredential databean){
		node.put(databean, new Config().setPutMethod(PutMethod.UPDATE_IGNORE));
	}

	public void updateMultiIgnore(Collection<DatarouterAccountSecretCredential> databeans){
		node.putMulti(databeans, new Config().setPutMethod(PutMethod.UPDATE_IGNORE));
	}

	public DatarouterAccountSecretCredential get(DatarouterAccountSecretCredentialKey key){
		return node.get(key);
	}

	public Scanner<DatarouterAccountSecretCredential> scan(){
		return node.scan();
	}

	public Scanner<DatarouterAccountSecretCredential> scanMulti(Collection<DatarouterAccountSecretCredentialKey> keys){
		return node.scanMulti(keys);
	}

	public void delete(DatarouterAccountSecretCredentialKey key){
		node.delete(key);
	}

}
