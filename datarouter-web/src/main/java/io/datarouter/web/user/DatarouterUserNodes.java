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

import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage;
import io.datarouter.web.user.databean.DatarouterPermissionRequest;
import io.datarouter.web.user.databean.DatarouterPermissionRequestKey;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUserHistory;
import io.datarouter.web.user.databean.DatarouterUserHistoryKey;
import io.datarouter.web.user.databean.DatarouterUserKey;
import io.datarouter.web.user.session.DatarouterSession;
import io.datarouter.web.user.session.DatarouterSessionKey;

public interface DatarouterUserNodes{

	IndexedSortedMapStorage<DatarouterUserKey,DatarouterUser> getUserNode();
	IndexedSortedMapStorage<DatarouterUserHistoryKey,DatarouterUserHistory> getUserHistoryNode();
	IndexedSortedMapStorage<DatarouterPermissionRequestKey,DatarouterPermissionRequest> getPermissionRequestNode();
	IndexedSortedMapStorage<DatarouterSessionKey,DatarouterSession> getSessionNode();

}
