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

import java.util.Map;

import javax.inject.Inject;

import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.group.SnapshotGroups;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.ContainerTag;

public class DatarouterSnapshotHandler extends BaseHandler{

	public static final String P_groupId = "groupId";
	public static final String P_snapshotId = "snapshotId";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private SnapshotGroups groups;

	@Handler
	public Mav summary(
			@Param(P_groupId) String groupId,
			@Param(P_snapshotId) String snapshotId){
		var snapshotKey = new SnapshotKey(groupId, snapshotId);
		RootBlock rootBlock = groups.getGroup(groupId).root(BlockKey.root(snapshotKey));
		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Filesystem - Snapshot Groups")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(buildSummary(rootBlock))
				.buildMav();
	}

	private ContainerTag<?> buildSummary(RootBlock rootBlock){
		var table = new J2HtmlTable<Map.Entry<String,String>>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withColumn("Key", row -> row.getKey())
				.withColumn("Value", row -> row.getValue())
				.build(rootBlock.toKeyValueStrings().entrySet());
		return table;
	}

}
