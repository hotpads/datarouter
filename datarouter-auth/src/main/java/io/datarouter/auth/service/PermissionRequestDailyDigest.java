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
import static j2html.TagCreator.h3;
import static j2html.TagCreator.i;
import static j2html.TagCreator.td;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.auth.config.DatarouterAuthPaths;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequest;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequestDao;
import io.datarouter.auth.storage.permissionrequest.DatarouterPermissionRequestKey;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.digest.DailyDigest;
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

	@Override
	public Optional<ContainerTag> getContent(){
		List<? extends SessionBasedUser> openRequests = permissionRequestDao.scanOpenPermissionRequests()
				.map(DatarouterPermissionRequest::getKey)
				.map(DatarouterPermissionRequestKey::getUserId)
				.map(id -> userInfo.getUserById(id, true))
				.include(Optional::isPresent)
				.map(Optional::get)
				.list();
		if(openRequests.size() == 0){
			return Optional.empty();
		}
		var header = h3("Open Permission Requests");
		var table = buildTable(openRequests);
		return Optional.of(div(header, table));
	}

	private <T extends SessionBasedUser> ContainerTag buildTable(List<T> rows){
		return new J2HtmlTable<T>()
				.withClasses("table table-sm table-striped my-4 border")
				.withColumn("Username", row -> row.getUsername())
				.withHtmlColumn("Profile", row -> {
					String detailsLink = detailsService.getUserProfileUrl(row.getUsername()).get();
					return td(a(detailsLink)
							.withHref(detailsLink));
				})
				.withHtmlColumn("Details", row -> {
					String link = servletContextSupplier.get().getContextPath() + paths.admin.editUser.toSlashedString()
							+ "?username=" + row.getUsername();
					return td(a(i().withClass("fa fa-link"))
							.withClass("btn btn-link w-100 py-0")
							.withHref(link));
				})
				.build(rows);
	}

}
