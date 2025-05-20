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

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.detail.DatarouterUserExternalDetailService;
import io.datarouter.auth.detail.DatarouterUserProfileLink;
import io.datarouter.auth.link.EditUserLink;
import io.datarouter.auth.service.UserInfo.UserInfoSupplier;
import io.datarouter.auth.session.SessionBasedUser;
import io.datarouter.auth.storage.user.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.auth.storage.user.permissionrequest.PermissionRequest;
import io.datarouter.auth.storage.user.permissionrequest.PermissionRequestKey;
import io.datarouter.email.link.DatarouterEmailLinkClient;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.types.MilliTime;
import io.datarouter.util.time.ZonedDateFormatterTool;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestRmlService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class PermissionRequestDailyDigest implements DailyDigest{

	@Inject
	private DatarouterPermissionRequestDao permissionRequestDao;
	@Inject
	private DatarouterAuthPaths paths;
	@Inject
	private DatarouterUserExternalDetailService detailsService;
	@Inject
	private UserInfoSupplier userInfo;
	@Inject
	private DailyDigestRmlService digestService;
	@Inject
	private DatarouterEmailLinkClient linkClient;

	@Override
	public String getTitle(){
		return "Permission Requests";
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.ACTIONABLE;
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.HIGH;
	}

	@Override
	public Optional<RmlBlock> getRelayContent(ZoneId zoneId){
		List<PermissionRequestDto> openRequests = getOpenRequests();
		if(openRequests.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(
				Rml.paragraph(
						digestService.makeHeading("Open Permission Requests", paths.admin.viewUsers),
						Rml.table(
								Rml.tableRow(
										Rml.tableHeader(Rml.text("Username")),
										Rml.tableHeader(Rml.text("Profile")),
										Rml.tableHeader(Rml.text("Date Requested")),
										Rml.tableHeader(Rml.text("Details"))))
								.with(openRequests.stream()
										.map(req -> {
											String username = req.user().getUsername();
											DatarouterUserProfileLink detailsLink = detailsService.getUserProfileLink(
													username).get();
											String editUrl = linkClient.toUrl(new EditUserLink()
													.withUsername(username));

											return Rml.tableRow(
													Rml.tableCell(Rml.text(username)),
													Rml.tableCell(Rml.text(detailsLink.name()).link(detailsLink.url())),
													Rml.tableCell(Rml.text(req.getInstantRequested(zoneId))),
													Rml.tableCell(Rml.text("Edit User Page").link(editUrl)));
										}))));
	}

	@Override
	public List<DailyDigestPlatformTask> getTasks(ZoneId zoneId){
		return List.of();
	}

	private List<PermissionRequestDto> getOpenRequests(){
		return permissionRequestDao.scanOpenPermissionRequests()
				.map(PermissionRequest::getKey)
				.concatOpt(key -> Optional.of(key)
						.map(PermissionRequestKey::getUserId)
						.map(id -> userInfo.get().findUserById(id, true))
						.filter(Optional::isPresent)
						.map(Optional::get)
						.map(user -> new PermissionRequestDto(user, key.getRequestTime())))
				.sort(Comparator.comparing((PermissionRequestDto dto) -> dto.user.getUsername()))
				.list();
	}

	private record PermissionRequestDto(
			SessionBasedUser user,
			MilliTime requested){

		public String getInstantRequested(ZoneId zoneId){
			return ZonedDateFormatterTool.formatInstantWithZone(requested.toInstant(), zoneId);
		}

	}

}
