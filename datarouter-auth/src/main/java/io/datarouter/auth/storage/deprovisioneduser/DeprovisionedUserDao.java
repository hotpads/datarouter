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
package io.datarouter.auth.storage.deprovisioneduser;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.deprovisioneduser.DeprovisionedUser.DeprovisionedUserFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;

@Singleton
public class DeprovisionedUserDao extends BaseDao{

	public static class DeprovisionedUserDaoParams extends BaseRedundantDaoParams{

		public DeprovisionedUserDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final SortedMapStorageNode<DeprovisionedUserKey,DeprovisionedUser,DeprovisionedUserFielder> node;

	@Inject
	public DeprovisionedUserDao(Datarouter datarouter, NodeFactory nodeFactory, DeprovisionedUserDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<DeprovisionedUserKey,DeprovisionedUser,DeprovisionedUserFielder> node =
							nodeFactory.create(clientId, DeprovisionedUser::new, DeprovisionedUserFielder::new)
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public Optional<DeprovisionedUser> find(DeprovisionedUserKey key){
		return node.find(key);
	}

	public Scanner<DeprovisionedUser> scan(){
		return node.scan();
	}

	public Scanner<DeprovisionedUser> scanWithPrefixes(Collection<DeprovisionedUserKey> prefixes){
		return node.scanWithPrefixes(prefixes);
	}

	public void putMulti(Collection<DeprovisionedUser> databeans){
		node.putMulti(databeans);
	}

	public void deleteMulti(Collection<DeprovisionedUserKey> keys){
		node.deleteMulti(keys);
	}

	public void deleteMultiUsernames(Collection<String> usernames){
		Scanner.of(usernames)
				.map(DeprovisionedUserKey::new)
				.flush(node::deleteMulti);
	}

}
