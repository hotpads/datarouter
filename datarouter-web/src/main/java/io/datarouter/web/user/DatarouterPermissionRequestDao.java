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
package io.datarouter.web.user;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.user.databean.DatarouterPermissionRequest;
import io.datarouter.web.user.databean.DatarouterPermissionRequestKey;
import io.datarouter.web.user.databean.DatarouterUserKey;

@Singleton
public class DatarouterPermissionRequestDao{

	@Inject
	private DatarouterUserNodes userNodes;


	public Scanner<DatarouterPermissionRequest> scanOpenPermissionRequests(){
		return userNodes.getPermissionRequestNode().scan()
				.include(request -> request.getResolution() == null);
	}

	public Scanner<DatarouterPermissionRequest> streamOpenPermissionRequestsForUser(Long userId){
		Objects.requireNonNull(userId);
		return scanPermissionRequestsForUser(userId)
				.include(request -> request.getResolution() == null);
	}

	public Scanner<DatarouterPermissionRequest> scanPermissionRequestsForUser(Long userId){
		return userNodes.getPermissionRequestNode().scanWithPrefix(new DatarouterPermissionRequestKey(userId, null));
	}

	public void createPermissionRequest(DatarouterPermissionRequest request){
		//supercede existing requests to leave only the new request unresolved
		List<DatarouterPermissionRequest> requestsToPut = streamOpenPermissionRequestsForUser(request.getKey()
				.getUserId())
				.map(DatarouterPermissionRequest::supercede)
				.list();
		requestsToPut.add(request);

		userNodes.getPermissionRequestNode().putMulti(requestsToPut);
	}

	public void declineAll(Long userId){
		List<DatarouterPermissionRequest> requestsToPut = streamOpenPermissionRequestsForUser(userId)
				.map(DatarouterPermissionRequest::decline)
				.list();
		userNodes.getPermissionRequestNode().putMulti(requestsToPut);
	}

	public Set<DatarouterUserKey> getUserKeysWithPermissionRequests(){
		return scanOpenPermissionRequests()
				.map(DatarouterPermissionRequest::getKey)
				.map(DatarouterPermissionRequestKey::getUserId)
				.map(DatarouterUserKey::new)
				.collect(Collectors.toSet());
	}

}
