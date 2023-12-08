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
package io.datarouter.auth.storage.user.userhistory;

import java.util.Collection;
import java.util.List;

import io.datarouter.auth.storage.user.userhistory.DatarouterUserHistoryLog.DatarouterUserHistoryLogFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterUserHistoryDao extends BaseDao{

	public record DatarouterUserHistoryDaoParams(List<ClientId> clientIds){
	}

	private final SortedMapStorageNode<
			DatarouterUserHistoryLogKey,
			DatarouterUserHistoryLog,
			DatarouterUserHistoryLogFielder> node;


	@Inject
	public DatarouterUserHistoryDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterUserHistoryDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<
							DatarouterUserHistoryLogKey,
							DatarouterUserHistoryLog,
							DatarouterUserHistoryLogFielder> node = nodeFactory.create(
									clientId,
									DatarouterUserHistoryLog::new,
									DatarouterUserHistoryLogFielder::new)
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public void put(DatarouterUserHistoryLog databean){
		node.put(databean);
	}

	public void putMulti(Collection<DatarouterUserHistoryLog> databeans){
		node.putMulti(databeans);
	}

	public List<DatarouterUserHistoryLog> getMulti(Collection<DatarouterUserHistoryLogKey> keys){
		return node.getMulti(keys);
	}

	public Scanner<DatarouterUserHistoryLog> scanWithPrefix(DatarouterUserHistoryLogKey key){
		return node.scanWithPrefix(key);
	}

}
