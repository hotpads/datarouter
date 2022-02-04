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
package io.datarouter.changelog.storage;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.changelog.storage.Changelog.ChangelogFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.tuple.Range;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;

@Singleton
public class ChangelogDao extends BaseDao{

	public static class ChangelogDaoParams extends BaseRedundantDaoParams{

		public ChangelogDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final SortedMapStorageNode<ChangelogKey,Changelog,ChangelogFielder> node;

	@Inject
	public ChangelogDao(Datarouter datarouter, ChangelogDaoParams params, NodeFactory nodeFactory){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<ChangelogKey,Changelog,ChangelogFielder> node =
							nodeFactory.create(clientId, Changelog::new, ChangelogFielder::new)
						.withTag(Tag.DATAROUTER)
						.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public Optional<Changelog> find(ChangelogKey key){
		return node.find(key);
	}

	public void put(Changelog databean){
		node.put(databean);
	}

	public Scanner<Changelog> scan(){
		return node.scan();
	}

	public Scanner<Changelog> scan(Range<ChangelogKey> range){
		return node.scan(range);
	}

	public Changelog get(ChangelogKey key){
		return node.get(key);
	}

}
