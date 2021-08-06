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
package io.datarouter.webappinstance.storage.onetimelogintoken;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;
import io.datarouter.webappinstance.storage.onetimelogintoken.OneTimeLoginToken.OneTimeLoginTokenFielder;

@Singleton
public class DatarouterOneTimeLoginTokenDao extends BaseDao{

	public static class DatarouterOneTimeLoginTokenDaoParams extends BaseRedundantDaoParams{

		public DatarouterOneTimeLoginTokenDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}
	}

	private final MapStorageNode<OneTimeLoginTokenKey,OneTimeLoginToken,OneTimeLoginTokenFielder> node;

	@Inject
	public DatarouterOneTimeLoginTokenDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterOneTimeLoginTokenDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<OneTimeLoginTokenKey,OneTimeLoginToken,OneTimeLoginTokenFielder> node =
							nodeFactory.create(clientId, OneTimeLoginToken::new, OneTimeLoginTokenFielder::new)
							.withIsSystemTable(true)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::new);
		datarouter.register(node);
	}

	public OneTimeLoginToken get(OneTimeLoginTokenKey key){
		return node.get(key);
	}

	public void put(OneTimeLoginToken oneTimeLoginToken){
		node.put(oneTimeLoginToken);
	}

	public void delete(OneTimeLoginTokenKey key){
		node.delete(key);
	}

	public void deleteAll(){
		node.deleteAll();
	}

}
