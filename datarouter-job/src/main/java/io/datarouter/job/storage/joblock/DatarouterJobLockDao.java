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
package io.datarouter.job.storage.joblock;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.datarouter.job.storage.joblock.JobLock.JobLockFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.PutMethod;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.storage.vacuum.DatabeanVacuum;
import io.datarouter.storage.vacuum.DatabeanVacuum.DatabeanVacuumBuilder;
import io.datarouter.types.MilliTime;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterJobLockDao extends BaseDao{

	public record DatarouterJobLockDaoParams(List<ClientId> clientIds){
	}

	private final SortedMapStorageNode<JobLockKey,JobLock,JobLockFielder> node;

	@Inject
	public DatarouterJobLockDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterJobLockDaoParams params){
		super(datarouter);

		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<JobLockKey,JobLock,JobLockFielder> node =
							nodeFactory.create(clientId, JobLock::new, JobLockFielder::new)
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public void putAndAcquire(JobLock databean){
		var config = new Config()
				.setPutMethod(PutMethod.INSERT_OR_BUST)
				.setIgnoreException(true);
		node.put(databean, config);
	}

	public void forcePut(JobLock databean){
		node.put(databean);
	}

	public JobLock get(JobLockKey key){
		return node.get(key);
	}

	public Optional<JobLock> find(JobLockKey key){
		return node.find(key);
	}

	public boolean exists(JobLockKey key){
		return node.exists(key);
	}

	public void delete(JobLockKey key){
		node.delete(key);
	}

	public void deleteMulti(Collection<JobLockKey> keys){
		node.deleteMulti(keys);
	}

	public Scanner<JobLock> scan(){
		return node.scan();
	}

	public DatabeanVacuum<JobLockKey,JobLock> makeVacuum2(){
		MilliTime now = MilliTime.now();
		return new DatabeanVacuumBuilder<>(
				node.scan(),
				databean -> now.isAfter(databean.getExpirationTime()),
				node::deleteMulti)
				.build();
	}

}
