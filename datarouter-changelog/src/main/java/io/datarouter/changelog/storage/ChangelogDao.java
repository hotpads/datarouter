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
package io.datarouter.changelog.storage;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.changelog.storage.Changelog.ChangelogFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.util.tuple.Range;

@Singleton
public class ChangelogDao extends BaseDao{

	public static class ChangelogDaoParams extends BaseDaoParams{

		public ChangelogDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final SortedMapStorage<ChangelogKey,Changelog> node;

	@Inject
	public ChangelogDao(Datarouter datarouter, ChangelogDaoParams params, NodeFactory nodeFactory){
		super(datarouter);
		node = nodeFactory.create(params.clientId, Changelog::new, ChangelogFielder::new)
				.withIsSystemTable(true)
				.buildAndRegister();
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

}
