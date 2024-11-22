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

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.detail.DatarouterUserExternalDetailService;
import io.datarouter.auth.detail.DatarouterUserProfileLink;
import io.datarouter.auth.service.UserInfo.UserInfoSupplier;
import io.datarouter.auth.session.SessionBasedUser;
import io.datarouter.auth.storage.user.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.auth.storage.user.permissionrequest.PermissionRequest;
import io.datarouter.auth.storage.user.permissionrequest.PermissionRequestKey;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.types.MilliTime;
import io.datarouter.util.time.ZonedDateFormatterTool;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TableTag;
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
	private DailyDigestService digestService;

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
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		List<PermissionRequestDto> openRequests = getOpenRequests();
		if(openRequests.isEmpty()){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Open Permission Requests", paths.admin.viewUsers);
		var table = buildEmailTable(openRequests, zoneId);
		return Optional.of(div(header, table));
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
											String username = req.user.getUsername();
											DatarouterUserProfileLink detailsLink = detailsService.getUserProfileLink(
													username).get();
											String editUrl = paths.admin.editUser.toSlashedString()
													+ "?username=" + username;

											return Rml.tableRow(
													Rml.tableCell(Rml.text(username)),
													Rml.tableCell(Rml.text(detailsLink.name()).link(detailsLink.url())),
													Rml.tableCell(Rml.text(req.getInstantRequested(zoneId))),
													Rml.tableCell(Rml.text("Edit User Page").link(editUrl)));
										}))));
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

	private TableTag buildEmailTable(List<PermissionRequestDto> rows, ZoneId zoneId){
		return new J2HtmlEmailTable<PermissionRequestDto>()
				.withColumn("Username", row -> row.user.getUsername())
				.withColumn(new J2HtmlEmailTableColumn<>(
						"Profile",
						row -> {
							String username = row.user.getUsername();
							DatarouterUserProfileLink detailsLink = detailsService.getUserProfileLink(username).get();
							return a(detailsLink.name()).withHref(detailsLink.url());
						}))
				.withColumn("Date Requested", row -> row.getInstantRequested(zoneId))
				.withColumn(new J2HtmlEmailTableColumn<>(
						"Details",
						row -> {
							String link = paths.admin.editUser.toSlashedString() + "?username="
									+ row.user.getUsername();
							return digestService.makeATagLink("Edit User Page", link);
						}))
				.build(rows);
	}

	private static class PermissionRequestDto{

		public final SessionBasedUser user;
		private final Instant instantRequested;

		public PermissionRequestDto(SessionBasedUser user, MilliTime requested){
			this.user = user;
			this.instantRequested = requested.toInstant();
		}

		public String getInstantRequested(ZoneId zoneId){
			return ZonedDateFormatterTool.formatInstantWithZone(instantRequested, zoneId);
		}

	}

}
