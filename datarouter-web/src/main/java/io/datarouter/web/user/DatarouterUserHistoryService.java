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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.util.DatarouterEmailTool;
import io.datarouter.util.collection.MapTool;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.web.app.WebAppName;
import io.datarouter.web.user.databean.DatarouterPermissionRequest;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUserHistory;
import io.datarouter.web.user.databean.DatarouterUserHistoryKey;
import io.datarouter.web.user.databean.DatarouterPermissionRequest.DatarouterPermissionRequestResolution;
import io.datarouter.web.user.databean.DatarouterUserHistory.DatarouterUserChangeType;

@Singleton
public class DatarouterUserHistoryService{

	private final DatarouterUserNodes userNodes;
	private final DatarouterPermissionRequestDao permissionRequestDao;
	private final DatarouterUserDao userDao;
	private final DatarouterProperties datarouterProperties;
	private final String webAppName;

	@Inject
	public DatarouterUserHistoryService(DatarouterUserNodes userNodes,
			DatarouterPermissionRequestDao permissionRequestDao, DatarouterUserDao userDao,
			DatarouterProperties datarouterProperties, WebAppName webAppName){
		this.userNodes = userNodes;
		this.permissionRequestDao = permissionRequestDao;
		this.userDao = userDao;
		this.datarouterProperties = datarouterProperties;
		this.webAppName = webAppName.getName();
	}

	//don't call this with unresolved DatarouterPermissionRequests
	public Map<DatarouterPermissionRequest, String> getResolvedRequestToHistoryChangesMap(
			List<DatarouterPermissionRequest> requests){
		List<DatarouterUserHistoryKey> historyKeys = IterableTool.map(requests, DatarouterPermissionRequest
				::toUserHistoryKey);
		Map<DatarouterUserHistoryKey, String> historyMap = userNodes.getUserHistoryNode().getMulti(historyKeys, null)
				.stream()
				.collect(Collectors.toMap(DatarouterUserHistory::getKey, DatarouterUserHistory::getChanges));

		//requests get closed when they are SUPERCEDED by other requests or when they are edited (and have a history)
		return MapTool.getBy(requests, Function.identity(), req -> historyMap.getOrDefault(req.toUserHistoryKey(),
				DatarouterPermissionRequestResolution.SUPERCEDED.getPersistentString()));
	}

	public void recordEdit(DatarouterUser user, DatarouterUserHistory history, String signinUrl){
		userNodes.getUserNode().put(user, null);
		userNodes.getUserHistoryNode().put(history, null);
		userNodes.getPermissionRequestNode().putMulti(permissionRequestDao.streamOpenPermissionRequestsForUser(history
				.getKey().getUserId())
				.map(history::resolvePermissionRequest)
				.collect(Collectors.toList()), null);
		sendEditEmail(user, history, signinUrl);
	}

	public void recordCreate(DatarouterUser user, Long editorId, String description){
		userNodes.getUserNode().put(user, null);
		userNodes.getUserHistoryNode().put(new DatarouterUserHistory(user.getId(), user.getCreated(), editorId,
				DatarouterUserChangeType.CREATE, description), null);
	}

	private void sendEditEmail(DatarouterUser user, DatarouterUserHistory history, String signinUrl){
		DatarouterUser editor = userDao.getUserById(history.getEditor());
		StringBuilder body = new StringBuilder()
				.append("Your user (").append(user.getUsername()).append(") permissions have been edited by user ")
				.append(editor.getId()).append(" (").append(editor.getUsername()).append(").")
				.append("\n\nChanges: ").append(history.getChanges())
				.append("\n\nPlease sign in again to refresh your session: ").append(signinUrl);

		String subject = "User permissions changed in web app " + webAppName;

		DatarouterEmailTool.trySendEmail(datarouterProperties.getAdministratorEmail(), user.getUsername(), subject,
				body.toString());
	}
}
