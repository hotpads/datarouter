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
package io.datarouter.exception.storage.httprecord;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.exception.storage.httprecord.HttpRequestRecord.HttpRequestRecordByExceptionRecord;
import io.datarouter.exception.storage.httprecord.HttpRequestRecord.HttpRequestRecordFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage;
import io.datarouter.storage.util.DatabeanVacuum;
import io.datarouter.storage.util.DatabeanVacuum.DatabeanVacuumBuilder;
import io.datarouter.virtualnode.writebehind.WriteBehindIndexedSortedMapStorageNode;

@Singleton
public class DatarouterHttpRequestRecordDao extends BaseDao{

	public static class DatarouterHttpRequestRecordDaoParams extends BaseDaoParams{

		public DatarouterHttpRequestRecordDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final WriteBehindIndexedSortedMapStorageNode<HttpRequestRecordKey,HttpRequestRecord,
			IndexedSortedMapStorage<HttpRequestRecordKey,HttpRequestRecord>> node;

	@Inject
	public DatarouterHttpRequestRecordDao(Datarouter datarouter, NodeFactory nodeFactory,
			DatarouterHttpRequestRecordDaoParams params){
		super(datarouter);
		IndexedSortedMapStorage<HttpRequestRecordKey,HttpRequestRecord> backingNode = nodeFactory
				.create(params.clientId, HttpRequestRecord::new, HttpRequestRecordFielder::new)
				.buildAndRegister();
		node = new WriteBehindIndexedSortedMapStorageNode<>(datarouter, backingNode);
	}

	public HttpRequestRecord lookupUnique(HttpRequestRecordByExceptionRecord key){
		return node.lookupUnique(key);
	}

	public void put(HttpRequestRecord databean){
		node.put(databean);
	}

	public void putMulti(Collection<HttpRequestRecord> databeans){
		node.putMulti(databeans);
	}

	public HttpRequestRecord get(HttpRequestRecordKey key){
		return node.get(key);
	}

	public List<HttpRequestRecord> getMulti(Collection<HttpRequestRecordKey> keys){
		return node.getMulti(keys);
	}

	public void deleteAll(){
		node.deleteAll();
	}

	public void delete(HttpRequestRecordKey key){
		node.delete(key);
	}

	public void deleteMulti(Collection<HttpRequestRecordKey> keys){
		node.deleteMulti(keys);
	}

	public DatabeanVacuum<HttpRequestRecordKey,HttpRequestRecord> makeVacuum(){
		var lifespan = Duration.ofDays(14);
		return new DatabeanVacuumBuilder<>(
				node.scan(),
				databean -> System.currentTimeMillis() - databean.getCreated().getTime() > lifespan.toMillis(),
				node::deleteMulti)
				.build();
	}

}
