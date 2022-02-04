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
package io.datarouter.batchsizeoptimizer.storage.optimizedbatch;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.batchsizeoptimizer.storage.optimizedbatch.OpOptimizedBatchSize.OpOptimizedBatchSizeFielder;
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
public class DatarouterOpOptimizedBatchSizeDao extends BaseDao{

	public static class DatarouterOpOptimizedBatchSizeDaoParams extends BaseRedundantDaoParams{

		public DatarouterOpOptimizedBatchSizeDaoParams(List<ClientId> clientId){
			super(clientId);
		}

	}

	private final SortedMapStorageNode<OpOptimizedBatchSizeKey,OpOptimizedBatchSize,OpOptimizedBatchSizeFielder> node;

	@Inject
	public DatarouterOpOptimizedBatchSizeDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterOpOptimizedBatchSizeDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<OpOptimizedBatchSizeKey,OpOptimizedBatchSize,OpOptimizedBatchSizeFielder> node
							= nodeFactory.create(clientId, OpOptimizedBatchSize::new, OpOptimizedBatchSizeFielder::new)
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);

	}

	public Optional<OpOptimizedBatchSize> find(OpOptimizedBatchSizeKey key){
		return node.find(key);
	}

	public void putMulti(Collection<OpOptimizedBatchSize> databeans){
		node.putMulti(databeans);
	}

	public Scanner<OpOptimizedBatchSize> scan(){
		return node.scan();
	}

}
