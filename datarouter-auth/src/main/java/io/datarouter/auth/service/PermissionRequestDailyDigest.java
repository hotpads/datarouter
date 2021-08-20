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
import static j2html.TagCreator.i;
import static j2html.TagCreator.td;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequest;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequestKey;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.scanner.OptionalScanner;
import io.datarouter.util.time.ZonedDateFormaterTool;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.user.detail.DatarouterUserExternalDetailService;
import io.datarouter.web.user.session.service.SessionBasedUser;
import j2html.tags.ContainerTag;

@Singleton
public class PermissionRequestDailyDigest implements DailyDigest{

	@Inject
	private DatarouterPermissionRequestDao permissionRequestDao;
	@Inject
	private ServletContextSupplier servletContextSupplier;
	@Inject
	private DatarouterAuthPaths paths;
	@Inject
	private DatarouterUserExternalDetailService detailsService;
	@Inject
	private UserInfo userInfo;
	@Inject
	private DailyDigestService digestService;
	@Inject
	private DatarouterService datarouterService;

	@Override
	public Optional<ContainerTag<?>> getPageContent(ZoneId zoneId){
		List<PermissionRequestDto> openRequests = getOpenRequests();
		if(openRequests.size() == 0){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Open Permission Requests", paths.admin.viewUsers);
		var table = buildPageTable(openRequests, zoneId);
		return Optional.of(div(header, table));
	}

	@Override
	public Optional<ContainerTag<?>> getEmailContent(){
		List<PermissionRequestDto> openRequests = getOpenRequests();
		if(openRequests.size() == 0){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Open Permission Requests", paths.admin.viewUsers);
		var table = buildEmailTable(openRequests, datarouterService.getZoneId());
		return Optional.of(div(header, table));
	}

	@Override
	public String getTitle(){
		return "Permission Requests";
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.HIGH;
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.ACTIONABLE;
	}

	private List<PermissionRequestDto> getOpenRequests(){
		return permissionRequestDao.scanOpenPermissionRequests()
				.map(DatarouterPermissionRequest::getKey)
				.map(key -> Optional.of(key)
						.map(DatarouterPermissionRequestKey::getUserId)
						.map(id -> userInfo.getUserById(id, true))
						.filter(Optional::isPresent)
						.map(Optional::get)
						.map(user -> new PermissionRequestDto(user, key.getRequestTime())))
				.concat(OptionalScanner::of)
				.list();
	}

	private ContainerTag<?> buildPageTable(List<PermissionRequestDto> rows, ZoneId zoneId){
		return new J2HtmlTable<PermissionRequestDto>()
				.withClasses("table table-sm table-striped my-4 border")
				.withColumn("Username", row -> row.user.getUsername())
				.withHtmlColumn("Profile", row -> {
					String detailsLink = detailsService.getUserProfileUrl(row.user.getUsername()).get();
					return td(a(detailsLink)
							.withHref(detailsLink));
				})
				.withColumn("Date Requested", row -> row.getInstantRequested(zoneId))
				.withHtmlColumn("Details", row -> {
					String link = servletContextSupplier.get().getContextPath() + paths.admin.editUser.toSlashedString()
							+ "?username=" + row.user.getUsername();
					return td(a(i().withClass("fa fa-link"))
							.withClass("btn btn-link w-100 py-0")
							.withHref(link));
				})
				.build(rows);
	}

	private ContainerTag<?> buildEmailTable(List<PermissionRequestDto> rows, ZoneId zoneId){
		return new J2HtmlEmailTable<PermissionRequestDto>()
				.withColumn("Username", row -> row.user.getUsername())
				.withColumn(new J2HtmlEmailTableColumn<>(
						"Profile",
						row -> {
							String detailsLink = detailsService.getUserProfileUrl(row.user.getUsername()).get();
							return a(detailsLink)
									.withHref(detailsLink);
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

		public PermissionRequestDto(SessionBasedUser user, Instant instantRequested){
			this.user = user;
			this.instantRequested = instantRequested;
		}

		public String getInstantRequested(ZoneId zoneId){
			return ZonedDateFormaterTool.formatInstantWithZone(instantRequested, zoneId);
		}

	}

}
