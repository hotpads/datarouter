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
package io.datarouter.auth.storage.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.virtualnode.redundant.RedundantIndexedSortedMapStorageNode;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUser.DatarouterUserByUserTokenLookup;
import io.datarouter.web.user.databean.DatarouterUser.DatarouterUserByUsernameLookup;
import io.datarouter.web.user.databean.DatarouterUser.DatarouterUserFielder;
import io.datarouter.web.user.databean.DatarouterUserKey;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterUserDao extends BaseDao{

	public static class DatarouterUserDaoParams extends BaseRedundantDaoParams{

		public DatarouterUserDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final IndexedSortedMapStorageNode<DatarouterUserKey,DatarouterUser,DatarouterUserFielder> node;

	@Inject
	public DatarouterUserDao(Datarouter datarouter, NodeFactory nodeFactory, DatarouterUserDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.<IndexedSortedMapStorageNode<DatarouterUserKey,DatarouterUser,DatarouterUserFielder>>map(clientId ->
						nodeFactory.create(clientId, DatarouterUser::new, DatarouterUserFielder::new)
						.withTag(Tag.DATAROUTER)
						.build())
				.listTo(RedundantIndexedSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public DatarouterUser get(DatarouterUserKey key){
		return node.get(key);
	}

	public DatarouterUser getByUserToken(DatarouterUserByUserTokenLookup key){
		return node.lookupUnique(key);
	}

	public DatarouterUser getByUsername(DatarouterUserByUsernameLookup key){
		return node.lookupUnique(key);
	}

	public List<DatarouterUser> getMulti(Collection<DatarouterUserKey> keys){
		return node.getMulti(keys);
	}

	public List<DatarouterUser> getMultiByUserTokens(Collection<DatarouterUserByUserTokenLookup> keys){
		return node.lookupMultiUnique(keys);
	}

	public List<DatarouterUser> getMultiByUsername(Collection<DatarouterUserByUsernameLookup> keys){
		return node.lookupMultiUnique(keys);
	}

	public Optional<DatarouterUser> find(DatarouterUserKey key){
		return node.find(key);
	}

	public Optional<DatarouterUser> find(DatarouterUserByUserTokenLookup key){
		return Optional.ofNullable(node.lookupUnique(key));
	}

	public Optional<DatarouterUser> findByUsername(DatarouterUserByUsernameLookup key){
		return Optional.ofNullable(node.lookupUnique(key));
	}

	public Scanner<DatarouterUser> scan(){
		return node.scan();
	}

	public void put(DatarouterUser databean){
		node.put(databean);
	}

	public void putMulti(Collection<DatarouterUser> databeans){
		node.putMulti(databeans);
	}

	public void delete(DatarouterUserKey key){
		node.delete(key);
	}

	public void deleteMulti(Collection<DatarouterUserKey> keys){
		node.deleteMulti(keys);
	}

	public boolean hasAny(){
		return node.scanKeys(new Config().setLimit(1)).hasAny();
	}

	public boolean exists(DatarouterUserByUserTokenLookup key){
		return node.lookupUnique(key) != null;
	}

}
