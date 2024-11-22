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
package io.datarouter.auth.web.service;

import static j2html.TagCreator.p;

import java.time.ZoneId;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.model.dto.PermissionRequestDto;
import io.datarouter.auth.service.DatarouterUserHistoryService;
import io.datarouter.auth.service.DatarouterUserService;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.auth.storage.user.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.auth.storage.user.permissionrequest.PermissionRequest;
import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.type.DatarouterEmailTypes.PermissionRequestEmailType;
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
	private DatarouterAuthPaths paths;
	@Inject
	private PermissionRequestEmailType permissionRequestEmailType;
	@Inject
	private ServerTypeDetector serverTypeDetector;

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
		String titleHref = htmlEmailService.startLinkBuilder()
				.withLocalPath(paths.admin.editUser.toSlashedString())
				.withParam("userId", editedUser.getId() + "")
				.build();
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
