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
package io.datarouter.auth.storage.permissionrequest;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequest.DatarouterPermissionRequestFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;

//TODO DATAROUTER-2759 rename to PermissionRequestDao and consider switching key to username or userToken
@Singleton
public class DatarouterPermissionRequestDao extends BaseDao{

	public static class DatarouterPermissionRequestDaoParams extends BaseRedundantDaoParams{

		public DatarouterPermissionRequestDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final SortedMapStorageNode<DatarouterPermissionRequestKey,DatarouterPermissionRequest,
			DatarouterPermissionRequestFielder> node;

	@Inject
	public DatarouterPermissionRequestDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterPermissionRequestDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<DatarouterPermissionRequestKey,DatarouterPermissionRequest,
					DatarouterPermissionRequestFielder> node =
							nodeFactory.create(clientId, DatarouterPermissionRequest::new,
									DatarouterPermissionRequestFielder::new)
						.withIsSystemTable(true)
						.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::new);
		datarouter.register(node);
	}

	public void putMulti(Collection<DatarouterPermissionRequest> databeans){
		node.putMulti(databeans);
	}

	public Scanner<DatarouterPermissionRequest> scan(){
		return node.scan();
	}

	public Scanner<DatarouterPermissionRequest> scanWithPrefix(DatarouterPermissionRequestKey prefix){
		return node.scanWithPrefix(prefix);
	}

	public Scanner<DatarouterPermissionRequest> scanOpenPermissionRequests(){
		return scan()
				.include(request -> request.getResolution() == null);
	}

	public Scanner<DatarouterPermissionRequest> scanOpenPermissionRequestsForUser(Long userId){
		Objects.requireNonNull(userId);
		return scanOpenPermissionRequestsForUsers(List.of(userId));
	}

	public Scanner<DatarouterPermissionRequest> scanOpenPermissionRequestsForUsers(List<Long> userIds){
		return Scanner.of(userIds)
				.map(userId -> new DatarouterPermissionRequestKey(userId, null))
				.listTo(node::scanWithPrefixes)
				.include(request -> request.getResolution() == null);
	}

	public Scanner<DatarouterPermissionRequest> scanPermissionRequestsForUser(Long userId){
		return scanWithPrefix(new DatarouterPermissionRequestKey(userId, null));
	}

	public void createPermissionRequest(DatarouterPermissionRequest request){
		//supercede existing requests to leave only the new request unresolved
		scanOpenPermissionRequestsForUser(request.getKey().getUserId())
				.map(DatarouterPermissionRequest::supercede)
				.append(request)
				.flush(this::putMulti);
	}

	public void declineAll(Long userId){
		scanOpenPermissionRequestsForUser(userId)
				.map(DatarouterPermissionRequest::decline)
				.flush(this::putMulti);
	}

	public Set<Long> getUserIdsWithPermissionRequests(){
		return scanOpenPermissionRequests()
				.map(DatarouterPermissionRequest::getKey)
				.map(DatarouterPermissionRequestKey::getUserId)
				.collect(HashSet::new);
	}

}
