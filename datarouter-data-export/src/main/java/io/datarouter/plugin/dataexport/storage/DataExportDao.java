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
package io.datarouter.plugin.dataexport.storage;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.plugin.dataexport.storage.DataExportItem.DataExportItemFielder;
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
public class DataExportDao extends BaseDao{

	public static class DatarouterDataExportDaoParams extends BaseRedundantDaoParams{

		public DatarouterDataExportDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final SortedMapStorageNode<DataExportItemKey,DataExportItem,DataExportItemFielder> node;

	@Inject
	public DataExportDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterDataExportDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.<SortedMapStorageNode<DataExportItemKey,DataExportItem,DataExportItemFielder>>map(clientId ->
						nodeFactory.create(clientId, DataExportItem::new, DataExportItemFielder::new)
								.withTag(Tag.DATAROUTER)
								.build())
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public void putMulti(Collection<DataExportItem> databeans){
		node.putMulti(databeans);
	}

}
