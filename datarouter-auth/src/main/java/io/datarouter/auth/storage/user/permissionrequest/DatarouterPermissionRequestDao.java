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
package io.datarouter.auth.storage.user.permissionrequest;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.datarouter.auth.storage.user.permissionrequest.PermissionRequest.PermissionRequestFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.types.MilliTime;
import io.datarouter.virtualnode.redundant.RedundantSortedMapStorageNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterPermissionRequestDao extends BaseDao{

	public record DatarouterPermissionRequestDaoParams(List<ClientId> clientIds){
	}

	private final SortedMapStorageNode<PermissionRequestKey,PermissionRequest,PermissionRequestFielder> node;

	@Inject
	public DatarouterPermissionRequestDao(
			Datarouter datarouter,
			NodeFactory nodeFactory,
			DatarouterPermissionRequestDaoParams params){
		super(datarouter);
		node = Scanner.of(params.clientIds)
				.map(clientId -> {
					SortedMapStorageNode<
							PermissionRequestKey,
							PermissionRequest,
							PermissionRequestFielder> node = nodeFactory.create(
									clientId,
									PermissionRequest::new,
									PermissionRequestFielder::new)
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
				})
				.listTo(RedundantSortedMapStorageNode::makeIfMulti);
		datarouter.register(node);
	}

	public void putMulti(Collection<PermissionRequest> databeans){
		node.putMulti(databeans);
	}

	public Scanner<PermissionRequest> scan(){
		return node.scan();
	}

	public Scanner<PermissionRequest> scanWithPrefix(PermissionRequestKey prefix){
		return node.scanWithPrefix(prefix);
	}

	public Scanner<PermissionRequest> scanOpenPermissionRequests(){
		return scan()
				.include(request -> request.getResolution() == null);
	}

	public Scanner<PermissionRequest> scanOpenPermissionRequestsForUser(Long userId){
		Objects.requireNonNull(userId);
		return scanOpenPermissionRequestsForUsers(List.of(userId));
	}

	public Scanner<PermissionRequest> scanOpenPermissionRequestsForUsers(List<Long> userIds){
		return Scanner.of(userIds)
				.map(userId -> new PermissionRequestKey(userId, null))
				.listTo(node::scanWithPrefixes)
				.include(request -> request.getResolution() == null);
	}

	public Scanner<PermissionRequest> scanPermissionRequestsForUser(Long userId){
		return scanWithPrefix(new PermissionRequestKey(userId, null));
	}

	public void createPermissionRequest(PermissionRequest request){
		//supercede existing requests to leave only the new request unresolved
		scanOpenPermissionRequestsForUser(request.getKey().getUserId())
				.map(PermissionRequest::supercede)
				.append(request)
				.flush(this::putMulti);
	}

	public void expireAll(Long userId){
		scanOpenPermissionRequestsForUser(userId)
				.map(PermissionRequest::expire)
				.flush(this::putMulti);
	}

	public void declineAll(Long userId, MilliTime declineTime){
		scanOpenPermissionRequestsForUser(userId)
				.map(request -> request.decline(declineTime.toInstant()))
				.flush(this::putMulti);
	}

	public Set<Long> getUserIdsWithPermissionRequests(){
		return scanOpenPermissionRequests()
				.map(PermissionRequest::getKey)
				.map(PermissionRequestKey::getUserId)
				.collect(HashSet::new);
	}

}
