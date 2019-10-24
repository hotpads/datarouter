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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.user.databean.DatarouterPermissionRequest;
import io.datarouter.web.user.databean.DatarouterPermissionRequestKey;
import io.datarouter.web.user.databean.DatarouterUserKey;

public interface BaseDatarouterPermissionRequestDao{

	void putMulti(Collection<DatarouterPermissionRequest> databeans);
	Scanner<DatarouterPermissionRequest> scan();
	Scanner<DatarouterPermissionRequest> scanWithPrefix(DatarouterPermissionRequestKey prefix);

	default Scanner<DatarouterPermissionRequest> scanOpenPermissionRequests(){
		return scan()
				.include(request -> request.getResolution() == null);
	}

	default Scanner<DatarouterPermissionRequest> scanOpenPermissionRequestsForUser(Long userId){
		Objects.requireNonNull(userId);
		return scanPermissionRequestsForUser(userId)
				.include(request -> request.getResolution() == null);
	}

	default Scanner<DatarouterPermissionRequest> scanPermissionRequestsForUser(Long userId){
		return scanWithPrefix(new DatarouterPermissionRequestKey(userId, null));
	}

	default void createPermissionRequest(DatarouterPermissionRequest request){
		//supercede existing requests to leave only the new request unresolved
		List<DatarouterPermissionRequest> requestsToPut = scanOpenPermissionRequestsForUser(request.getKey()
				.getUserId())
				.map(DatarouterPermissionRequest::supercede)
				.list();
		requestsToPut.add(request);
		putMulti(requestsToPut);
	}

	default void declineAll(Long userId){
		List<DatarouterPermissionRequest> requestsToPut = scanOpenPermissionRequestsForUser(userId)
				.map(DatarouterPermissionRequest::decline)
				.list();
		putMulti(requestsToPut);
	}

	default Set<DatarouterUserKey> getUserKeysWithPermissionRequests(){
		return scanOpenPermissionRequests()
				.map(DatarouterPermissionRequest::getKey)
				.map(DatarouterPermissionRequestKey::getUserId)
				.map(DatarouterUserKey::new)
				.collect(Collectors.toSet());
	}

	static class NoOpDatarouterPermissionRequestDao implements BaseDatarouterPermissionRequestDao{

		@Override
		public void putMulti(Collection<DatarouterPermissionRequest> databeans){
		}

		@Override
		public Scanner<DatarouterPermissionRequest> scan(){
			return Scanner.empty();
		}

		@Override
		public Scanner<DatarouterPermissionRequest> scanWithPrefix(DatarouterPermissionRequestKey prefix){
			return Scanner.empty();
		}

	}

}
