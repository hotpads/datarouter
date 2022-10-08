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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.inject.Inject;

import io.datarouter.filesystem.snapshot.group.SnapshotGroup;
import io.datarouter.filesystem.snapshot.group.SnapshotGroups;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.reader.SnapshotIdReader;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotRecord;
import io.datarouter.filesystem.snapshot.web.SnapshotRecordStringDecoder;
import io.datarouter.filesystem.snapshot.web.SnapshotRecordStrings;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.specialized.TableTag;

public class DatarouterSnapshotEntryHandler extends BaseHandler{

	public static final String P_groupId = "groupId";
	public static final String P_snapshotId = "snapshotId";
	public static final String P_id = "id";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private SnapshotGroups groups;

	@Handler
	public Mav entry(
			@Param(P_groupId) String groupId,
			@Param(P_snapshotId) String snapshotId,
			@Param(P_id) Long id){
		var snapshotKey = new SnapshotKey(groupId, snapshotId);
		return pageFactory.startBuilder(request)
				.withTitle("Datarouter Filesystem - Snapshot Entry")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(buildTable(snapshotKey, id))
				.buildMav();
	}

	private TableTag buildTable(
			SnapshotKey snapshotKey,
			long id){
		SnapshotGroup group = groups.getGroup(snapshotKey.groupId());
		var reader = new SnapshotIdReader(snapshotKey, groups);
		SnapshotRecord record = reader.getRecord(id);
		SnapshotRecordStringDecoder decoder = ReflectionTool.create(group.getSnapshotEntryDecoderClass());
		SnapshotRecordStrings decoded = decoder.decode(record);
		List<Row> rows = new ArrayList<>();
		rows.add(new Row("id", Long.toString(record.id())));
		rows.add(new Row(decoder.keyName(), decoded.key()));
		rows.add(new Row(decoder.valueName(), decoded.value()));
		IntStream.range(0, decoded.columnValues().size())
				.mapToObj(column -> new Row(
						decoder.columnValueName(column),
						decoded.columnValues().get(column)))
				.forEach(rows::add);
		var table = new J2HtmlTable<Row>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withColumn("field", Row::header)
				.withColumn("value", Row::content)
				.build(rows);
		return table;
	}

	private record Row(
			String header,
			String content){
	}

}
