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
package io.datarouter.job.storage.stopjobrequest;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.job.storage.stopjobrequest.StopJobRequest.StopJobRequestFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.util.DatabeanVacuum;
import io.datarouter.storage.util.DatabeanVacuum.DatabeanVacuumBuilder;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;

@Singleton
public class StopJobRequestDao extends BaseDao{

	public static class StopJobRequestDaoParams extends BaseRedundantDaoParams{

		public StopJobRequestDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final SortedMapStorageNode<StopJobRequestKey,StopJobRequest,StopJobRequestFielder> node;

	@Inject
	public StopJobRequestDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			StopJobRequestDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<StopJobRequestKey,StopJobRequest,StopJobRequestFielder> node =
							nodeFactory.create(clientId, StopJobRequest::new, StopJobRequestFielder::new)
							.withIsSystemTable(true)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::new);
		datarouter.register(node);
	}

	//scans requests for the server where the expiration date is not in the past
	public Scanner<StopJobRequest> scanUnexpiredRequestsForServer(String serverName){
		Instant now = Instant.now();
		return node.scanWithPrefix(new StopJobRequestKey(serverName, null))
				.include(databean -> databean.getRequestExpiration().isAfter(now));
	}

	public void putMulti(Collection<StopJobRequest> databeans){
		node.putMulti(databeans);
	}

	public void delete(StopJobRequestKey key){
		node.delete(key);
	}

	public DatabeanVacuum<StopJobRequestKey,StopJobRequest> makeVacuum(){
		var now = Instant.now();
		return new DatabeanVacuumBuilder<>(
				node.scan(),
				databean -> databean.getRequestExpiration().isBefore(now),
				node::deleteMulti)
				.build();
	}

}
