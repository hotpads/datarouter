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
package io.datarouter.webappinstance.storage.webappinstancelog;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.model.databean.FieldlessIndexEntry;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.IndexingNodeFactory;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import io.datarouter.storage.node.op.index.IndexReader;
import io.datarouter.storage.tag.Tag;
import io.datarouter.util.tuple.Range;
import io.datarouter.virtualnode.redundant.RedundantIndexedSortedMapStorageNode;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLog.WebappInstanceLogFielder;

@Singleton
public class DatarouterWebappInstanceLogDao extends BaseDao{

	public static class DatarouterWebappInstanceLogDaoParams extends BaseRedundantDaoParams{

		public DatarouterWebappInstanceLogDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final IndexedSortedMapStorageNode<WebappInstanceLogKey,WebappInstanceLog,WebappInstanceLogFielder> node;
	private final IndexReader<WebappInstanceLogKey,WebappInstanceLog,WebappInstanceLogByBuildInstantKey,
			FieldlessIndexEntry<WebappInstanceLogByBuildInstantKey,WebappInstanceLogKey,WebappInstanceLog>>
			byBuildInstant;

	@Inject
	public DatarouterWebappInstanceLogDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			IndexingNodeFactory indexingNodeFactory,
			DatarouterWebappInstanceLogDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					IndexedSortedMapStorageNode<WebappInstanceLogKey,WebappInstanceLog,WebappInstanceLogFielder> node =
							nodeFactory.create(clientId, WebappInstanceLog::new, WebappInstanceLogFielder::new)
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
					})
				.listTo(RedundantIndexedSortedMapStorageNode::makeIfMulti);
		byBuildInstant = indexingNodeFactory.createKeyOnlyManagedIndex(WebappInstanceLogByBuildInstantKey::new, node)
				.build();
		datarouter.register(node);
	}

	public void put(WebappInstanceLog log){
		node.put(log);
	}

	public Scanner<WebappInstanceLog> scan(){
		return node.scan();
	}

	public Scanner<WebappInstanceLog> scanWithPrefix(WebappInstanceLogKey key){
		return node.scanWithPrefix(key);
	}

	public Scanner<WebappInstanceLog> scanDatabeans(Range<WebappInstanceLogByBuildInstantKey> range){
		return byBuildInstant.scanDatabeans(range);
	}

}
