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
package io.datarouter.filesystem.snapshot.web;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;

import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.filesystem.snapshot.group.SnapshotGroup;
import io.datarouter.filesystem.snapshot.group.SnapshotGroups;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.ContainerTag;

public class DatarouterSnapshotGroupHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterSnapshotGroupHandler.class);

	private static final String P_groupId = "groupId";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterSnapshotPaths snapshotPaths;
	@Inject
	private SnapshotGroups groups;

	@Handler
	public Mav listSnapshots(@Param(P_groupId) String groupId){
		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Filesystem - Snapshots in group")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(buildSnapshotList(groupId))
				.buildMav();
	}

	private ContainerTag<?> buildSnapshotList(String groupId){
		logger.warn("hello");
		SnapshotGroup group = groups.getGroup(groupId);
		var thead = thead(tr(th("id"), th("summary"), td("entries")));
		var table = table()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.with(thead);
		group.keyReadOps(false).scanSnapshotKeys()
				.map(snapshotKey -> {
					var idTd = td(snapshotKey.snapshotId);

					String summaryHref = new URIBuilder()
							.setPath(request.getContextPath() + snapshotPaths.datarouter.snapshot.individual.summary
									.toSlashedString())
							.addParameter(DatarouterSnapshotHandler.P_groupId, snapshotKey.groupId)
							.addParameter(DatarouterSnapshotHandler.P_snapshotId, snapshotKey.snapshotId)
							.toString();
					var summaryTd = td(a("summary").withHref(summaryHref));

					String entriesHref = new URIBuilder()
							.setPath(request.getContextPath() + snapshotPaths.datarouter.snapshot.individual.entries
									.toSlashedString())
							.addParameter(DatarouterSnapshotHandler.P_groupId, snapshotKey.groupId)
							.addParameter(DatarouterSnapshotHandler.P_snapshotId, snapshotKey.snapshotId)
							.toString();
					var entriesTd = td(a("entries").withHref(entriesHref));

					return tr(idTd, summaryTd, entriesTd);
				})
				.forEach(table::with);
		var header = h4("Snapshots in group: " + group.getGroupId());
		return div(header, table)
				.withClass("container-fluid my-4")
				.withStyle("padding-left: 0px");
	}

}
