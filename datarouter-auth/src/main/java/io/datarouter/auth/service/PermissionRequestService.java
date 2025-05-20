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

import static j2html.TagCreator.p;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import io.datarouter.auth.link.EditUserLink;
import io.datarouter.auth.model.dto.PermissionRequestDto;
import io.datarouter.auth.role.Role;
import io.datarouter.auth.role.RoleManager;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.auth.storage.user.permissionrequest.PermissionRequest;
import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.type.DatarouterEmailTypes.PermissionRequestEmailType;
import io.datarouter.httpclient.endpoint.link.DatarouterLinkClient;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.types.MilliTime;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class PermissionRequestService{

	private static final String EMAIL_TITLE = "Permission Request";

	@Inject
	private DatarouterPermissionRequestDao datarouterPermissionRequestDao;
	@Inject
	private DatarouterUserService datarouterUserService;
	@Inject
	private DatarouterUserHistoryService datarouterUserHistoryService;
	@Inject
	private DatarouterHtmlEmailService htmlEmailService;
	@Inject
	private PermissionRequestEmailType permissionRequestEmailType;
	@Inject
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private DatarouterLinkClient linkClient;
	@Inject
	private DatarouterPermissionRequestEmailService permissionRequestEmailService;
	@Inject
	private RoleManager roleManager;

	public Optional<PermissionRequest> findOpenPermissionRequest(DatarouterUser user){
		return datarouterPermissionRequestDao
				.scanOpenPermissionRequestsForUser(user.getId())
				.findMax(Comparator.comparing(request -> request.getKey().getRequestTime()));
	}

	public List<PermissionRequestDto> getReverseChronologicalPermissionRequestDtos(
			DatarouterUser user,
			ZoneId timezoneForRequestTimeFormat){
		return datarouterPermissionRequestDao.scanPermissionRequestsForUser(user.getId())
				.listTo(requests -> Scanner.of(datarouterUserHistoryService.getResolvedRequestToHistoryChangesMap(
						requests).entrySet()))
				.sort(Entry.comparingByKey(PermissionRequest.REVERSE_CHRONOLOGICAL_COMPARATOR))
				.map(entry -> new PermissionRequestDto(
						entry.getKey(),
						timezoneForRequestTimeFormat,
						entry.getValue()))
				.list();
	}

	public record DeclinePermissionRequestDto(
			boolean success,
			String message){
	}

	public void createPermissionRequest(DatarouterUser user,
			Set<Role> requestedRoles,
			String reason,
			Optional<String> deniedUrl,
			Optional<String> allowedRoles){
		String specifics = "Request Reason: \"%s\"\nRequested Roles: %s.".formatted(reason, requestedRoles)
				+ deniedUrl.map(url -> "\nAttempted request to: " + url + ".").orElse("")
				+ allowedRoles.map(roles -> "\nAllowed Roles: " + roles + ".").orElse("");

		boolean requestIsDuplicate = findOpenPermissionRequest(user)
				.filter(request -> request.getRequestText().equals(specifics))
				.isPresent();
		// don't supersede the request and send another email if the request is the same
		if(requestIsDuplicate){
			return;
		}
		datarouterPermissionRequestDao.createPermissionRequest(new PermissionRequest(
				user.getId(),
				MilliTime.now(),
				specifics,
				null,
				null));
		Set<String> additionalRecipients = roleManager.getAdditionalPermissionRequestEmailRecipients(user,
				requestedRoles);
		permissionRequestEmailService.sendRequestEmail(user, reason, specifics, additionalRecipients);
	}

	public DeclinePermissionRequestDto declinePermissionRequests(
			DatarouterUser editedUser,
			DatarouterUser editor){
		//only allow DATAROUTER_ADMIN and self to decline requests
		if(!Objects.equals(editor.getId(), editedUser.getId()) && !datarouterUserService.isDatarouterAdmin(editor)){
			return new DeclinePermissionRequestDto(false, "You do not have permission to decline this request.");
		}
		MilliTime now = MilliTime.now();
		datarouterPermissionRequestDao.declineAll(editedUser.getId(), now);
		String changeString = String.format("Permission requests for %s have been declined by %s",
				editedUser.getUsername(),
				editor.getUsername());
		datarouterUserHistoryService.recordPermissionRequestDecline(editedUser, editor, changeString, now);
		sendDeclineEmail(editedUser, changeString);
		return new DeclinePermissionRequestDto(true, null);
	}

	private void sendDeclineEmail(DatarouterUser editedUser, String message){
		String titleHref = linkClient.toInternalUrl(new EditUserLink().withUserId(editedUser.getId()));
		var content = p(message);
		var emailBuilder = htmlEmailService.startEmailBuilder()
				.withSubject(datarouterUserHistoryService.getPermissionRequestEmailSubject(editedUser))
				.withTitle(EMAIL_TITLE)
				.withTitleHref(titleHref)
				.withContent(content)
				.from(editedUser.getUsername())
				.to(editedUser.getUsername())
				.to(permissionRequestEmailType, serverTypeDetector.mightBeProduction())
				.toSubscribers(serverTypeDetector.mightBeProduction())
				.toAdmin(serverTypeDetector.mightBeDevelopment());
		htmlEmailService.trySendJ2Html(emailBuilder);
	}

}
