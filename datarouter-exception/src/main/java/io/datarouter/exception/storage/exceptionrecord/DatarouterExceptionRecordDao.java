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
package io.datarouter.exception.storage.exceptionrecord;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.exception.storage.exceptionrecord.ExceptionRecord.ExceptionRecordFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.storage.util.DatabeanVacuum;
import io.datarouter.storage.util.DatabeanVacuum.DatabeanVacuumBuilder;
import io.datarouter.util.tuple.Range;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;

@Singleton
public class DatarouterExceptionRecordDao extends BaseDao{

	public static class DatarouterExceptionRecordDaoParams extends BaseRedundantDaoParams{

		public DatarouterExceptionRecordDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final SortedMapStorageNode<ExceptionRecordKey,ExceptionRecord,ExceptionRecordFielder> node;

	@Inject
	public DatarouterExceptionRecordDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterExceptionRecordDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<ExceptionRecordKey,ExceptionRecord,ExceptionRecordFielder> node =
							nodeFactory.create(clientId, ExceptionRecord::new, ExceptionRecordFielder::new)
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public ExceptionRecord get(ExceptionRecordKey key){
		return node.get(key);
	}

	public void put(ExceptionRecord databean){
		node.put(databean);
	}

	public void putMulti(Collection<ExceptionRecord> databeans){
		node.putMulti(databeans);
	}

	public Scanner<ExceptionRecord> scan(Range<ExceptionRecordKey> range, Config config){
		return node.scan(range, config);
	}

	public DatabeanVacuum<ExceptionRecordKey,ExceptionRecord> makeVacuum(){
		var lifespan = Duration.ofDays(14);
		return new DatabeanVacuumBuilder<>(
				node.scan(),
				databean -> System.currentTimeMillis() - databean.getCreated().getTime() > lifespan.toMillis(),
				node::deleteMulti)
				.build();
	}

}
