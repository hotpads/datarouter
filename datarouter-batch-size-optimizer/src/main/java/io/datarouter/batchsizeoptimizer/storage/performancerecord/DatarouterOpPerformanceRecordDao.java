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
package io.datarouter.batchsizeoptimizer.storage.performancerecord;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.batchsizeoptimizer.storage.performancerecord.OpPerformanceRecord.OpPerformanceRecordFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;

@Singleton
public class DatarouterOpPerformanceRecordDao extends BaseDao{

	public static class DatarouterOpPerformanceRecordDaoParams extends BaseRedundantDaoParams{

		public DatarouterOpPerformanceRecordDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final SortedMapStorageNode<OpPerformanceRecordKey,OpPerformanceRecord,OpPerformanceRecordFielder> node;


	@Inject
	public DatarouterOpPerformanceRecordDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterOpPerformanceRecordDaoParams params){
		super(datarouter);

		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<OpPerformanceRecordKey,OpPerformanceRecord,OpPerformanceRecordFielder> node =
							nodeFactory.create(clientId, OpPerformanceRecord::new, OpPerformanceRecordFielder::new)
							.withIsSystemTable(true)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);

	}

	public Scanner<OpPerformanceRecord> scan(){
		return node.scan();
	}

	public void deleteMulti(Collection<OpPerformanceRecordKey> keys){
		node.deleteMulti(keys);
	}

	public void put(OpPerformanceRecord databean){
		node.put(databean);
	}

	public void putMulti(Collection<OpPerformanceRecord> databeans){
		node.putMulti(databeans);
	}

}
