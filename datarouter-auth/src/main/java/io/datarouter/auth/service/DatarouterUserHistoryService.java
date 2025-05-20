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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import io.datarouter.auth.model.dto.HistoryChange;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUserDao;
import io.datarouter.auth.storage.user.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.auth.storage.user.permissionrequest.PermissionRequest;
import io.datarouter.auth.storage.user.userhistory.DatarouterUserHistoryDao;
import io.datarouter.auth.storage.user.userhistory.DatarouterUserHistoryLog;
import io.datarouter.auth.storage.user.userhistory.DatarouterUserHistoryLog.DatarouterUserChangeType;
import io.datarouter.auth.storage.user.userhistory.DatarouterUserHistoryLogKey;
import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.type.DatarouterEmailTypes.PermissionRequestEmailType;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDto;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.instrumentation.relay.dto.RelayAddToThreadRequestDto;
import io.datarouter.instrumentation.relay.dto.RelayStartThreadRequestDto;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.instrumentation.relay.rml.RmlCollectors;
import io.datarouter.instrumentation.relay.rml.RmlDoc;
import io.datarouter.instrumentation.relay.rml.RmlStyle;
import io.datarouter.relay.DatarouterRelayFinder;
import io.datarouter.relay.DatarouterRelaySender;
import io.datarouter.relay.DatarouterRelayTopics;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.AdminEmail;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.types.MilliTime;
import j2html.tags.specialized.PTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterUserHistoryService{

	public static final String CHANGELOG_TYPE = "DatarouterUserHistory";

	@Inject
	private AdminEmail adminEmail;
	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private DatarouterUserDao userDao;
	@Inject
	private DatarouterUserHistoryDao userHistoryDao;
	@Inject
	private DatarouterPermissionRequestDao permissionRequestDao;
	@Inject
	private DatarouterUserService datarouterUserService;
	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private PermissionRequestEmailType permissionRequestEmailType;
	@Inject
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private ServiceName serviceName;
	@Inject
	private EnvironmentName environmentName;
	@Inject
	private DatarouterRelayFinder relayFinder;
	@Inject
	private DatarouterRelaySender relaySender;
	@Inject
	private DatarouterRelayTopics relayTopics;

	public Map<PermissionRequest,Optional<HistoryChange>> getResolvedRequestToHistoryChangesMap(
			List<PermissionRequest> requests){
		// The DatarouterUserHistory which resolved a permission request will match
		// DatarouterUserHistoryLogKey.time == PermissionRequest.resolutionTime
		Map<DatarouterUserHistoryLogKey,HistoryChange> historyMap = Scanner.of(requests)
				.map(PermissionRequest::toUserHistoryKey)
				.map(key -> key.orElse(null))
				.include(Objects::nonNull)
				.batch(100)
				.map(userHistoryDao::getMulti)
				.concat(Scanner::of)
				.toMap(DatarouterUserHistoryLog::getKey, history -> new HistoryChange(
						history.getChanges(),
						datarouterUserService.findUserById(history.getEditor(), false)));

		return Scanner.of(requests)
				.deduplicateConsecutive()
				.toMap(Function.identity(), request -> request.toUserHistoryKey()
						.map(historyKey -> historyMap.getOrDefault(
								historyKey,
								new HistoryChange(request.getResolution().persistentString, Optional.empty()))));
	}

	public void putAndRecordCreate(DatarouterUser user, Long editorId, String editorUsername, String description){
		userDao.put(user);
		userHistoryDao.put(new DatarouterUserHistoryLog(
				user.getId(),
				user.getCreated(),
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
		var history = new DatarouterUserHistoryLog(
				user.getId(),
				MilliTime.now(),
				editor.getId(),
				DatarouterUserChangeType.RESET,
				"password");
		doPutAndRecordEdit(user, history, false);
		sendPasswordChangeEmail(user, history, signinUrl);
		DatarouterChangelogDto dto = new DatarouterChangelogDtoBuilder(
				CHANGELOG_TYPE,
				user.getUsername(),
				DatarouterUserChangeType.RESET.persistentString,
				editor.getUsername())
				.build();
		changelogRecorder.record(dto);
	}

	public void putAndRecordTimezoneUpdate(
			DatarouterUser user,
			DatarouterUser editor,
			String changes){
		var history = new DatarouterUserHistoryLog(
				user.getId(),
				MilliTime.now(),
				editor.getId(),
				DatarouterUserChangeType.EDIT,
				changes);
		doPutAndRecordEdit(user, history, false);
		var dto = new DatarouterChangelogDtoBuilder(
				CHANGELOG_TYPE,
				user.getUsername(),
				DatarouterUserChangeType.EDIT.persistentString,
				editor.getUsername())
				.withComment(changes)
				.build();
		changelogRecorder.record(dto);
	}

	public void putAndRecordPermissionChange(
			DatarouterUser user,
			DatarouterUser editor,
			String changes,
			String signinUrl){
		putAndRecordPermissionChange(user, editor.getId(), editor.getUsername(), changes, signinUrl);
	}

	public void putAndRecordPermissionChange(
			DatarouterUser user,
			long editorId,
			String editorEmail,
			String changes,
			String signinUrl){
		var history = new DatarouterUserHistoryLog(
				user.getId(),
				MilliTime.now(),
				editorId,
				DatarouterUserChangeType.EDIT,
				changes);
		doPutAndRecordEdit(user, history, true);
		sendPermissionChangeEmail(user, history, signinUrl);
		DatarouterChangelogDto dto = new DatarouterChangelogDtoBuilder(
				CHANGELOG_TYPE,
				user.getUsername(),
				DatarouterUserChangeType.EDIT.persistentString,
				editorEmail)
				.withComment(changes)
				.build();
		changelogRecorder.record(dto);
	}

	public void recordRoleReset(DatarouterUser user, String changes){
		var history = new DatarouterUserHistoryLog(
				user.getId(),
				MilliTime.now(),
				DatarouterUserCreationService.ADMIN_ID,
				DatarouterUserChangeType.RESET,
				changes);
		userHistoryDao.put(history);
		DatarouterChangelogDto dto = new DatarouterChangelogDtoBuilder(
				CHANGELOG_TYPE,
				user.getUsername(),
				DatarouterUserChangeType.RESET.persistentString,
				adminEmail.get())
				.withComment(changes)
				.build();
		changelogRecorder.record(dto);
	}

	public void recordPermissionRequestDecline(
			DatarouterUser user,
			DatarouterUser editor,
			String changes,
			MilliTime declineTime){
		var history = new DatarouterUserHistoryLog(
				user.getId(),
				declineTime,
				editor.getId(),
				DatarouterUserChangeType.INFO,
				changes);
		userHistoryDao.put(history);
		DatarouterChangelogDto dto = new DatarouterChangelogDtoBuilder(
				CHANGELOG_TYPE,
				user.getUsername(),
				DatarouterUserChangeType.INFO.persistentString,
				editor.getUsername())
				.withComment(changes)
				.build();
		changelogRecorder.record(dto);
	}

	public void recordSamlSignOnChanges(DatarouterUser user, String changes){
		var history = new DatarouterUserHistoryLog(
				user.getId(),
				MilliTime.now(),
				DatarouterUserCreationService.ADMIN_ID,
				DatarouterUserChangeType.SAML,
				changes);
		userHistoryDao.put(history);
		DatarouterChangelogDto dto = new DatarouterChangelogDtoBuilder(
				CHANGELOG_TYPE,
				user.getUsername(),
				DatarouterUserChangeType.SAML.persistentString,
				adminEmail.get())
				.withComment(changes)
				.build();
		changelogRecorder.record(dto);
	}

	public void recordDeprovisions(List<DatarouterUser> users){
		MilliTime time = MilliTime.now();
		Scanner.of(users)
				.map(user -> new DatarouterUserHistoryLog(
						user.getId(),
						time,
						DatarouterUserCreationService.ADMIN_ID,
						DatarouterUserChangeType.DEPROVISION,
						"deprovisioned"))
				.flush(userHistoryDao::putMulti)
				.toMap(history -> history.getKey().getUserId());
		Scanner.of(users)
				.map(DatarouterUser::getId)
				.listTo(permissionRequestDao::scanOpenPermissionRequestsForUsers)
				.map(request -> request.decline(time.toInstant()))
				.flush(permissionRequestDao::putMulti);
		recordProvisioningChangelogs(users, DatarouterUserChangeType.DEPROVISION);
	}

	public void recordRestore(DatarouterUser user){
		MilliTime time = MilliTime.now();
		userHistoryDao.put(new DatarouterUserHistoryLog(
				user.getId(),
				time,
				DatarouterUserCreationService.ADMIN_ID,
				DatarouterUserChangeType.RESTORE,
				"restored"));
		recordProvisioningChangelogs(List.of(user), DatarouterUserChangeType.RESTORE);
	}

	public List<DatarouterUserHistoryLog> getHistoryForUser(Long userId){
		return userHistoryDao.scanWithPrefix(new DatarouterUserHistoryLogKey(userId, null))
				.list();
	}

	// similar to standard datarouter-email subject, but required for proper email threading
	public String getPermissionRequestEmailSubject(DatarouterUser user){
		return getEmailSubject("Permission Request", user);
	}

	private void recordProvisioningChangelogs(
			List<DatarouterUser> users,
			DatarouterUserChangeType action){
		Scanner.of(users)
				.map(DatarouterUser::getUsername)
				.map(username -> new DatarouterChangelogDtoBuilder(
						CHANGELOG_TYPE,
						username,
						action.persistentString,
						adminEmail.get()))
				.map(DatarouterChangelogDtoBuilder::build)
				.forEach(changelogRecorder::record);
	}

	private void doPutAndRecordEdit(DatarouterUser user, DatarouterUserHistoryLog history, boolean permissionsChanged){
		userDao.put(user);
		userHistoryDao.put(history);
		if(permissionsChanged){
			permissionRequestDao.scanOpenPermissionRequestsForUser(history.getKey().getUserId())
					.map(history::resolvePermissionRequest)
					.flush(permissionRequestDao::putMulti);
		}
	}

	private void sendPasswordChangeEmail(DatarouterUser user, DatarouterUserHistoryLog history, String signInUrl){
		DatarouterUser editor = datarouterUserService.getUserById(history.getEditor(), false);
		var p1 = p(String.format("Your user (%s) password has been changed by user %s (%s).",
				user.getUsername(),
				editor.getId(),
				editor.getUsername()));
		var p2 = p("Changes: " + history.getChanges());
		var content = div(p1, p2, makeSignInParagraph(signInUrl));
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject(getPasswordChangedEmailSubject(user))
				.withTitle("Password Changed")
				.withTitleHref(signInUrl)
				.withContent(content)
				.from(user.getUsername())
				.to(user.getUsername());
		htmlEmailService.trySendJ2Html(emailBuilder);
	}

	private void sendPermissionChangeEmail(DatarouterUser user, DatarouterUserHistoryLog history, String signInUrl){
		DatarouterUser editor = datarouterUserService.getUserById(history.getEditor(), false);
		String p1Text = "%s permissions have been edited by %s".formatted(user.getUsername(), editor.getUsername());
		var p1 = p(p1Text);
		var p2 = p("Changes: " + history.getChanges()).withStyle("white-space: pre-wrap");
		var content = div(p1, p2, makeSignInParagraph(signInUrl));
		String subject = getPermissionRequestEmailSubject(user);
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject(subject)
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

		List<String> topics = relayTopics.permissionChanged();
		RmlDoc doc = Rml.doc(
				Rml.heading(1, Rml.text("Permissions Changed").link(signInUrl)),
				Rml.paragraph(Rml.text("Changes: "))
						.with(history.getChanges().lines()
								.map(Rml::text)
								.collect(RmlCollectors.joining(Rml.hardBreak()))),
				makeSignInParagraphBlock(signInUrl))
				.withPadding(RmlStyle.padding(1));
		// We could record the threadId in DB so we don't have to try and look it up
		relayFinder.findRecentThread(relayTopics.permissionRequest().getFirst(), subject, Duration.ofDays(7))
				.ifPresentOrElse(
						threadId -> relaySender.addToThread(new RelayAddToThreadRequestDto(threadId, doc)),
						() -> relaySender.startThread(new RelayStartThreadRequestDto(
								topics,
								DatarouterPermissionRequestEmailService.FROM_NAME,
								subject,
								doc)));
	}

	private String getPasswordChangedEmailSubject(DatarouterUser user){
		return getEmailSubject("Password Changed", user);
	}

	private String getEmailSubject(String prefix, DatarouterUser user){
		return String.format("%s %s - %s - %s",
				prefix,
				user.getUsername(),
				environmentName.get(),
				serviceName.get());
	}

	private static PTag makeSignInParagraph(String signInUrl){
		return p(text("Please sign in again to refresh your session: "), a("sign in").withHref(signInUrl));
	}

	private static RmlBlock makeSignInParagraphBlock(String signInUrl){
		return Rml.paragraph(
				Rml.text("Please sign in again to refresh your session: "),
				Rml.text("sign in").link(signInUrl));
	}

}
