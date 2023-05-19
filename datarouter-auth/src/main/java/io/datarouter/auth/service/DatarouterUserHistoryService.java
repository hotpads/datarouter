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
package io.datarouter.auth.service;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.p;
import static j2html.TagCreator.text;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequest;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.auth.storage.user.DatarouterUserDao;
import io.datarouter.auth.storage.userhistory.DatarouterUserHistory;
import io.datarouter.auth.storage.userhistory.DatarouterUserHistory.DatarouterUserChangeType;
import io.datarouter.auth.storage.userhistory.DatarouterUserHistoryDao;
import io.datarouter.auth.storage.userhistory.DatarouterUserHistoryKey;
import io.datarouter.email.type.DatarouterEmailTypes.PermissionRequestEmailType;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDto;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.user.databean.DatarouterUser;
import j2html.tags.specialized.PTag;

@Singleton
public class DatarouterUserHistoryService{

	private static final String CHANGELOG_TYPE = "DatarouterUserHistory";

	@Inject
	private AdminEmail adminEmail;
	@Inject
	private ChangelogRecorder changelogRecorder;
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
	@Inject
	private PermissionRequestEmailType permissionRequestEmailType;
	@Inject
	private ServerTypeDetector serverTypeDetector;

	public Map<DatarouterPermissionRequest,Optional<HistoryChange>> getResolvedRequestToHistoryChangesMap(
			List<DatarouterPermissionRequest> requests){
		Map<DatarouterUserHistoryKey,HistoryChange> historyMap = Scanner.of(requests)
				.map(DatarouterPermissionRequest::toUserHistoryKey)
				.map(key -> key.orElse(null))
				.include(Objects::nonNull)
				.batch(100)
				.map(baseDatarouterUserHistoryDao::getMulti)
				.concat(Scanner::of)
				.toMap(DatarouterUserHistory::getKey, history -> new HistoryChange(history.getChanges(),
						Optional.ofNullable(datarouterUserService.getUserById(history.getEditor()))));

		return Scanner.of(requests)
				.deduplicateConsecutive()
				.toMap(Function.identity(), request -> request.toUserHistoryKey()
						.map(historyKey -> historyMap.getOrDefault(
								historyKey,
								new HistoryChange(request.getResolution().persistentString, Optional.empty()))));
	}

	public record HistoryChange(
			String changes,
			Optional<DatarouterUser> editor){
	}

	public void putAndRecordCreate(DatarouterUser user, Long editorId, String editorUsername, String description){
		baseDatarouterUserDao.put(user);
		baseDatarouterUserHistoryDao.put(new DatarouterUserHistory(
				user.getId(),
				user.getCreatedInstant(),
				editorId,
				DatarouterUserChangeType.CREATE,
				description));
		var dto = new DatarouterChangelogDtoBuilder(
				CHANGELOG_TYPE,
				user.getUsername(),
				DatarouterUserChangeType.CREATE.persistentString,
				editorUsername)
				.withComment(description)
				.build();
		changelogRecorder.record(dto);
	}

	public void putAndRecordPasswordChange(DatarouterUser user, DatarouterUser editor, String signinUrl){
		var history = new DatarouterUserHistory(
				user.getId(),
				Instant.now(),
				editor.getId(),
				DatarouterUserChangeType.RESET,
				"password");
		doPutAndRecordEdit(user, history);
		sendPasswordChangeEmail(user, history, signinUrl);
		DatarouterChangelogDto dto = new DatarouterChangelogDtoBuilder(
				CHANGELOG_TYPE,
				user.getUsername(),
				DatarouterUserChangeType.RESET.persistentString,
				editor.getUsername())
				.build();
		changelogRecorder.record(dto);
	}

	public void putAndRecordEdit(DatarouterUser user, DatarouterUser editor, String changes, String signinUrl){
		var history = new DatarouterUserHistory(
				user.getId(),
				Instant.now(),
				editor.getId(),
				DatarouterUserChangeType.EDIT,
				changes);
		doPutAndRecordEdit(user, history);
		sendRoleEditEmail(user, history, signinUrl);
		DatarouterChangelogDto dto = new DatarouterChangelogDtoBuilder(
				CHANGELOG_TYPE,
				user.getUsername(),
				DatarouterUserChangeType.EDIT.persistentString,
				editor.getUsername())
				.withComment(changes)
				.build();
		changelogRecorder.record(dto);
	}

	public void recordMessage(DatarouterUser user, DatarouterUser editor, String message){
		baseDatarouterUserHistoryDao.put(new DatarouterUserHistory(
				user.getId(),
				Instant.now(),
				editor.getId(),
				DatarouterUserChangeType.INFO,
				message));
	}

	public void recordDeprovisions(List<DatarouterUser> users, Optional<DatarouterUser> editor){
		Instant time = Instant.now();
		Long editorId = editor.map(DatarouterUser::getId)
				.orElse(DatarouterUserCreationService.ADMIN_ID);
		Map<Long, DatarouterUserHistory> histories = Scanner.of(users)
				.map(user -> new DatarouterUserHistory(
						user.getId(),
						time,
						editorId,
						DatarouterUserChangeType.DEPROVISION,
						"deprovisioned"))
				.flush(baseDatarouterUserHistoryDao::putMulti)
				.toMap(history -> history.getKey().getUserId());
		Scanner.of(users)
				.map(DatarouterUser::getId)
				.listTo(permissionRequestDao::scanOpenPermissionRequestsForUsers)
				.map(request -> request.decline(time))
				.flush(permissionRequestDao::putMulti);
		editor.ifPresent(editorUser -> {
			users.forEach(user -> sendDeprovisioningEmail(user, histories.get(user.getId()), editorUser));
		});
		recordProvisioningChangelogs(users, editor, DatarouterUserChangeType.DEPROVISION);
	}

	public void recordRestores(List<DatarouterUser> users, Optional<DatarouterUser> editor){
		Instant time = Instant.now();
		Long editorId = editor.map(DatarouterUser::getId)
				.orElse(null);
		Scanner.of(users)
				.map(user -> new DatarouterUserHistory(
						user.getId(),
						time,
						editorId,
						DatarouterUserChangeType.RESTORE,
						"restored"))
				.flush(baseDatarouterUserHistoryDao::putMulti);
		recordProvisioningChangelogs(users, editor, DatarouterUserChangeType.RESTORE);
	}

	public List<DatarouterUserHistory> getHistoryForUser(Long userId){
		return baseDatarouterUserHistoryDao.scanWithPrefix(new DatarouterUserHistoryKey(userId, null))
				.list();
	}

	private void recordProvisioningChangelogs(
			List<DatarouterUser> users,
			Optional<DatarouterUser> editor,
			DatarouterUserChangeType action){
		Scanner.of(users)
				.map(DatarouterUser::getUsername)
				.map(username -> new DatarouterChangelogDtoBuilder(
						CHANGELOG_TYPE,
						username,
						action.persistentString,
						editor.map(DatarouterUser::getUsername).orElse(adminEmail.get())))
				.map(DatarouterChangelogDtoBuilder::build)
				.forEach(changelogRecorder::record);
	}

	private void doPutAndRecordEdit(DatarouterUser user, DatarouterUserHistory history){
		baseDatarouterUserDao.put(user);
		baseDatarouterUserHistoryDao.put(history);
		permissionRequestDao.scanOpenPermissionRequestsForUser(history.getKey().getUserId())
				.map(history::resolvePermissionRequest)
				.flush(permissionRequestDao::putMulti);
	}

	private void sendPasswordChangeEmail(DatarouterUser user, DatarouterUserHistory history, String signInUrl){
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
				.withContent(content)
				.from(user.getUsername())
				.to(user.getUsername());
		htmlEmailService.trySendJ2Html(emailBuilder);
	}

	private void sendRoleEditEmail(DatarouterUser user, DatarouterUserHistory history, String signInUrl){
		DatarouterUser editor = datarouterUserService.getUserById(history.getEditor());
		var p1 = p(String.format("%s permissions have been edited by %s", user.getUsername(), editor.getUsername()));
		var p2 = p("Changes: " + history.getChanges()).withStyle("white-space: pre-wrap");
		var content = div(p1, p2, makeSignInParagraph(signInUrl));
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject(userEditService.getPermissionRequestEmailSubject(user))
				.withTitle("Permissions Changed")
				.withTitleHref(signInUrl)
				.withContent(content)
				.from(user.getUsername())
				.to(user.getUsername())
				.to(editor.getUsername())
				.to(permissionRequestEmailType, serverTypeDetector.mightBeProduction())
				.toSubscribers(serverTypeDetector.mightBeProduction())
				.toAdmin(serverTypeDetector.mightBeDevelopment());
		htmlEmailService.trySendJ2Html(emailBuilder);
	}

	private void sendDeprovisioningEmail(DatarouterUser user, DatarouterUserHistory history, DatarouterUser editor){
		var content = div(p(String.format("Your user (%s) has been %s by user %s (%s).",
				user.getUsername(),
				history.getChanges(),
				editor.getId(),
				editor.getUsername())));
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject(userEditService.getPermissionRequestEmailSubject(user))
				.withTitle("Permissions Changed")
				.withContent(content)
				.from(user.getUsername())
				.to(user.getUsername());
		htmlEmailService.trySendJ2Html(emailBuilder);
	}

	private static PTag makeSignInParagraph(String signInUrl){
		return p(text("Please sign in again to refresh your session: "), a("sign in").withHref(signInUrl));
	}

}
