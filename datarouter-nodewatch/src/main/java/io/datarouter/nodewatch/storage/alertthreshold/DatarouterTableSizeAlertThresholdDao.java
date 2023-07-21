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
package io.datarouter.nodewatch.storage.alertthreshold;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.datarouter.nodewatch.storage.alertthreshold.TableSizeAlertThreshold.TableSizeAlertThresholdFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterTableSizeAlertThresholdDao extends BaseDao{

	public static class DatarouterTableSizeAlertThresholdDaoParams extends BaseRedundantDaoParams{

		public DatarouterTableSizeAlertThresholdDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final SortedMapStorageNode<
			TableSizeAlertThresholdKey,
			TableSizeAlertThreshold,
			TableSizeAlertThresholdFielder> node;

	@Inject
	public DatarouterTableSizeAlertThresholdDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterTableSizeAlertThresholdDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<
							TableSizeAlertThresholdKey,
							TableSizeAlertThreshold,
							TableSizeAlertThresholdFielder> node = nodeFactory.create(
									clientId,
									TableSizeAlertThreshold::new,
									TableSizeAlertThresholdFielder::new)
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public Optional<TableSizeAlertThreshold> find(TableSizeAlertThresholdKey key){
		return node.find(key);
	}

	public void put(TableSizeAlertThreshold databean){
		node.put(databean);
	}

	public void putMulti(Collection<TableSizeAlertThreshold> databeans){
		node.putMulti(databeans);
	}

	public void delete(TableSizeAlertThresholdKey key){
		node.delete(key);
	}

}
