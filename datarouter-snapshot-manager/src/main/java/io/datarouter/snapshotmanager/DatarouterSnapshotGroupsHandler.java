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
package io.datarouter.snapshotmanager;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.table;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.filesystem.snapshot.group.SnapshotGroups;
import io.datarouter.scanner.Threads;
import io.datarouter.snapshotmanager.DatarouterSnapshotExecutors.DatarouterSnapshotWebExecutor;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.TagCreator;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class DatarouterSnapshotGroupsHandler extends BaseHandler{

	private static final String P_groupId = "groupId";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterSnapshotPaths snapshotPaths;
	@Inject
	private SnapshotGroups groups;
	@Inject
	private DatarouterSnapshotWebExecutor exec;

	@Handler
	public Mav listGroups(){
		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Filesystem - Snapshot Groups")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(buildGroupList())
				.buildMav();
	}

	private DivTag buildGroupList(){
		var thead = thead(tr(th("ID"), th("numSnapshots")));
		var table = table()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.with(thead);
		groups.scanIds()
				.sort()
				.parallelOrdered(new Threads(exec, exec.getMaximumPoolSize()))
				.map(id -> {
					String href = new URIBuilder()
							.setPath(request.getContextPath() + snapshotPaths.datarouter.snapshot.group.listSnapshots
									.toSlashedString())
							.addParameter(P_groupId, id)
							.toString();
					var anchor = a(id).withHref(href);
					String numSnapshots = groups.getGroup(id).keyReadOps(false).scanSnapshotKeys().count() + "";
					return tr(TagCreator.td(anchor), TagCreator.td(numSnapshots));
				})
				.forEach(table::with);
		var header = h4("Snapshot Groups");
		return div(header, table)
				.withClass("container-fluid my-4")
				.withStyle("padding-left: 0px");
	}

}
