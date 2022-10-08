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
import static j2html.TagCreator.td;

import java.util.List;

import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;

import io.datarouter.filesystem.snapshot.group.SnapshotGroup;
import io.datarouter.filesystem.snapshot.group.SnapshotGroups;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.reader.ScanningSnapshotReader;
import io.datarouter.filesystem.snapshot.web.SnapshotRecordStringDecoder;
import io.datarouter.filesystem.snapshot.web.SnapshotRecordStrings;
import io.datarouter.snapshotmanager.DatarouterSnapshotExecutors.DatarouterSnapshotWebExecutor;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalLong;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.DomContent;

public class DatarouterSnapshotEntriesHandler extends BaseHandler{

	public static final String P_groupId = "groupId";
	public static final String P_snapshotId = "snapshotId";
	public static final String P_offset = "offset";
	public static final String P_limit = "limit";

	@Inject
	private DatarouterSnapshotWebExecutor exec;
	@Inject
	private DatarouterSnapshotPaths snapshotPaths;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private SnapshotGroups groups;

	@Handler
	public Mav entries(
			@Param(P_groupId) String groupId,
			@Param(P_snapshotId) String snapshotId,
			@Param(P_offset) OptionalLong optOffset,
			@Param(P_limit) OptionalLong optLimit){
		var snapshotKey = new SnapshotKey(groupId, snapshotId);
		SnapshotGroup group = groups.getGroup(snapshotKey.groupId());
		DomContent content;
		if(group.getSnapshotEntryDecoderClass() == null){
			String message = String.format("%s not defined for groupId=%s",
					SnapshotRecordStringDecoder.class.getSimpleName(),
					snapshotKey.groupId());
			content = div(message);
		}else{
			long offset = optOffset.orElse(0L);
			long limit = optLimit.orElse(100L);
			content = buildTable(snapshotKey, offset, limit);
		}
		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Filesystem - Snapshot Entries")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	private DomContent buildTable(
			SnapshotKey snapshotKey,
			long offset,
			long limit){
		SnapshotGroup group = groups.getGroup(snapshotKey.groupId());
		var reader = new ScanningSnapshotReader(snapshotKey, exec, 2, groups, 1);
		SnapshotRecordStringDecoder decoder = ReflectionTool.create(group.getSnapshotEntryDecoderClass());
		List<SnapshotRecordStrings> rows = reader.scan(0)
				// TODO go directly to the first row
				.skip(offset)
				.limit(limit)
				.map(decoder::decode)
				.list();
		var table = new J2HtmlTable<SnapshotRecordStrings>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withColumn("id", SnapshotRecordStrings::id)
				.withColumn(decoder.keyName(), SnapshotRecordStrings::key)
				.withColumn(decoder.valueName(), row -> {
					if(row.value() == null){
						return "";
					}else if(row.value().length() < 64){
						return row.value();
					}else{
						return row.value().subSequence(0, 64) + "...";
					}
				})
				.withHtmlColumn("details", row -> {
					String href = new URIBuilder()
							.setPath(request.getContextPath() + snapshotPaths.datarouter.snapshot.individual.entry
									.toSlashedString())
							.addParameter(DatarouterSnapshotEntryHandler.P_groupId, snapshotKey.groupId())
							.addParameter(DatarouterSnapshotEntryHandler.P_snapshotId, snapshotKey.snapshotId())
							.addParameter(DatarouterSnapshotEntryHandler.P_id, Long.toString(row.id()))
							.toString();
					return td(a("view").withHref(href));
				})
				.build(rows);
		return table;
	}

}
