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
package io.datarouter.auth.service;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.p;
import static j2html.TagCreator.text;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequest;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.auth.storage.user.DatarouterUserDao;
import io.datarouter.auth.storage.userhistory.DatarouterUserHistory;
import io.datarouter.auth.storage.userhistory.DatarouterUserHistory.DatarouterUserChangeType;
import io.datarouter.auth.storage.userhistory.DatarouterUserHistoryDao;
import io.datarouter.auth.storage.userhistory.DatarouterUserHistoryKey;
import io.datarouter.util.collection.MapTool;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.user.databean.DatarouterUser;
import j2html.tags.ContainerTag;

@Singleton
public class DatarouterUserHistoryService{

	@Inject
	private DatarouterUserDao baseDatarouterUserDao;
	@Inject
	private DatarouterUserHistoryDao baseDatarouterUserHistoryDao;
	@Inject
	private DatarouterPermissionRequestDao permissionRequestDao;
	@Inject
	private DatarouterUserService datarouterUserService;
	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private DatarouterUserEditService userEditService;

	//don't call this with unresolved DatarouterPermissionRequests
	public Map<DatarouterPermissionRequest,String> getResolvedRequestToHistoryChangesMap(
			List<DatarouterPermissionRequest> requests){
		List<DatarouterUserHistoryKey> historyKeys = requests.stream()
				.map(DatarouterPermissionRequest::toUserHistoryKey)
				.collect(Collectors.toList());
		Map<DatarouterUserHistoryKey, String> historyMap = baseDatarouterUserHistoryDao.getMulti(historyKeys).stream()
				.collect(Collectors.toMap(DatarouterUserHistory::getKey, DatarouterUserHistory::getChanges));

		//requests get closed when they are SUPERCEDED by other requests or when they are edited (and have a history)
		return MapTool.getBy(requests, Function.identity(), req -> historyMap.getOrDefault(req.toUserHistoryKey(),
				req.getResolution().getPersistentString()));
	}

	public void recordCreate(DatarouterUser user, Long editorId, String description){
		baseDatarouterUserDao.put(user);
		baseDatarouterUserHistoryDao.put(new DatarouterUserHistory(user.getId(), user.getCreated(), editorId,
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
		baseDatarouterUserDao.put(user);
		baseDatarouterUserHistoryDao.put(history);
		permissionRequestDao.putMulti(permissionRequestDao
				.scanOpenPermissionRequestsForUser(history.getKey().getUserId())
				.map(history::resolvePermissionRequest)
				.list());
	}

	private void sendPasswordChangeEmail(DatarouterUser user, DatarouterUserHistory history, String signInUrl){
		String from = user.getUsername();
		String to = user.getUsername();
		DatarouterUser editor = datarouterUserService.getUserById(history.getEditor());
		var p1 = p(String.format("Your user (%s) password has been changed by user %s (%s).",
				user.getUsername(),
				editor.getId(),
				editor.getUsername()));
		var p2 = p("Changes: " + history.getChanges());
		var content = div(p1, p2, makeSignInParagraph(signInUrl));
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject(userEditService.getPermissionRequestEmailSubject(user))
				.withTitle("Password Changed")
				.withTitleHref(signInUrl)
				.withContent(content);
		htmlEmailService.trySendJ2Html(from, to, emailBuilder);
	}

	private void sendRoleEditEmail(DatarouterUser user, DatarouterUserHistory history, String signInUrl){
		DatarouterUser editor = datarouterUserService.getUserById(history.getEditor());
		String from = user.getUsername();
		String to = userEditService.getUserEditEmailRecipients(user, editor);
		var p1 = p(String.format("%s permissions have been edited by %s", user.getUsername(), editor.getUsername()));
		var p2 = p("Changes: " + history.getChanges());
		var content = div(p1, p2, makeSignInParagraph(signInUrl));
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject(userEditService.getPermissionRequestEmailSubject(user))
				.withTitle("Permissions Changed")
				.withTitleHref(signInUrl)
				.withContent(content);
		htmlEmailService.trySendJ2Html(from, to, emailBuilder);
	}

	private static ContainerTag makeSignInParagraph(String signInUrl){
		return p(text("Please sign in again to refresh your session: "), a("sign in").withHref(signInUrl));
	}

}
