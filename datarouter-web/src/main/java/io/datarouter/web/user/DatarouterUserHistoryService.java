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

import io.datarouter.util.collection.MapTool;
import io.datarouter.web.app.WebappName;
import io.datarouter.web.email.DatarouterEmailService;
import io.datarouter.web.user.databean.DatarouterPermissionRequest;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.databean.DatarouterUserHistory;
import io.datarouter.web.user.databean.DatarouterUserHistory.DatarouterUserChangeType;
import io.datarouter.web.user.databean.DatarouterUserHistoryKey;

@Singleton
public class DatarouterUserHistoryService{

	@Inject
	private DatarouterUserNodes userNodes;
	@Inject
	private DatarouterPermissionRequestDao permissionRequestDao;
	@Inject
	private DatarouterUserDao userDao;
	@Inject
	private DatarouterEmailService datarouterEmailService;
	@Inject
	private WebappName webappName;
	@Inject
	private DatarouterUserEditService userEditService;

	//don't call this with unresolved DatarouterPermissionRequests
	public Map<DatarouterPermissionRequest,String> getResolvedRequestToHistoryChangesMap(
			List<DatarouterPermissionRequest> requests){
		List<DatarouterUserHistoryKey> historyKeys = requests.stream()
				.map(DatarouterPermissionRequest::toUserHistoryKey)
				.collect(Collectors.toList());
		Map<DatarouterUserHistoryKey, String> historyMap = userNodes.getUserHistoryNode().getMulti(historyKeys).stream()
				.collect(Collectors.toMap(DatarouterUserHistory::getKey, DatarouterUserHistory::getChanges));

		//requests get closed when they are SUPERCEDED by other requests or when they are edited (and have a history)
		return MapTool.getBy(requests, Function.identity(), req -> historyMap.getOrDefault(req.toUserHistoryKey(),
				req.getResolution().getPersistentString()));
	}

	public void recordCreate(DatarouterUser user, Long editorId, String description){
		userNodes.getUserNode().put(user);
		userNodes.getUserHistoryNode().put(new DatarouterUserHistory(user.getId(), user.getCreated(), editorId,
				DatarouterUserChangeType.CREATE, description));
	}

	public void recordPasswordChange(DatarouterUser user, DatarouterUserHistory history, String signinUrl){
		recordEdit(user, history);
		sendPasswordChangeEmail(user, history, signinUrl);
	}

	public void recordRoleEdit(DatarouterUser user, DatarouterUserHistory history, String signinUrl){
		recordEdit(user, history);
		sendRoleEditEmail(user, history, signinUrl);
	}

	private void recordEdit(DatarouterUser user, DatarouterUserHistory history){
		userNodes.getUserNode().put(user);
		userNodes.getUserHistoryNode().put(history);
		userNodes.getPermissionRequestNode().putMulti(permissionRequestDao.streamOpenPermissionRequestsForUser(history
				.getKey().getUserId())
				.map(history::resolvePermissionRequest)
				.collect(Collectors.toList()));
	}

	private void sendPasswordChangeEmail(DatarouterUser user, DatarouterUserHistory history, String signinUrl){
		DatarouterUser editor = userDao.getUserById(history.getEditor());
		String recipients = user.getUsername();
		String subject = userEditService.getPermissionRequestEmailSubject(user, webappName.getName());
		StringBuilder body = new StringBuilder()
				.append("Your user (").append(user.getUsername()).append(") password has been changed by user ")
				.append(editor.getId()).append(" (").append(editor.getUsername()).append(").")
				.append("\n\nChanges: ").append(history.getChanges())
				.append("\n\nPlease sign in again to refresh your session: ").append(signinUrl);
		datarouterEmailService.trySendEmail(user.getUsername(), recipients, subject, body.toString());
	}

	private void sendRoleEditEmail(DatarouterUser user, DatarouterUserHistory history, String signinUrl){
		DatarouterUser editor = userDao.getUserById(history.getEditor());
		String recipients = userEditService.getUserEditEmailRecipients(user, editor);
		String subject = userEditService.getPermissionRequestEmailSubject(user, webappName.getName());
		StringBuilder body = new StringBuilder()
				.append(user.getUsername()).append(" permissions have been edited by ").append(editor.getUsername())
				.append("\n\nChanges: ").append(history.getChanges())
				.append("\n\nPlease sign in again to refresh your session: ").append(signinUrl);
		datarouterEmailService.trySendEmail(user.getUsername(), recipients, subject, body.toString());
	}

}
