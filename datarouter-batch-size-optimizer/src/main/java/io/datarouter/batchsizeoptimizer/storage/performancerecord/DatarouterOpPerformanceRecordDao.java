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
package io.datarouter.batchsizeoptimizer.storage.performancerecord;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.batchsizeoptimizer.storage.performancerecord.OpPerformanceRecord.OpPerformanceRecordFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.virtualnode.writebehind.WriteBehindSortedMapStorageNode;

@Singleton
public class DatarouterOpPerformanceRecordDao extends BaseDao{

	public static class DatarouterOpPerformanceRecordDaoParams extends BaseDaoParams{

		public DatarouterOpPerformanceRecordDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final SortedMapStorage<OpPerformanceRecordKey,OpPerformanceRecord> node;

	@Inject
	public DatarouterOpPerformanceRecordDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterOpPerformanceRecordDaoParams params){
		super(datarouter);
		node = new WriteBehindSortedMapStorageNode<>(
				datarouter,
				nodeFactory.create(params.clientId,
				OpPerformanceRecord::new,
				OpPerformanceRecordFielder::new)
				.buildAndRegister());
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
