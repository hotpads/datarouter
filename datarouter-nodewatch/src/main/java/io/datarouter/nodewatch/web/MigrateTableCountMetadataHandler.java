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
package io.datarouter.nodewatch.web;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h2;

import java.util.List;

import javax.inject.Inject;

import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.nodewatch.storage.tablecount.DatarouterTableCountDao;
import io.datarouter.nodewatch.storage.tablecount.TableCountKey;
import io.datarouter.nodewatch.storage.tablesample.DatarouterTableSampleDao;
import io.datarouter.nodewatch.storage.tablesample.TableSampleKey;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;

public class MigrateTableCountMetadataHandler extends BaseHandler{

	private static final String
			P_sourceNodeName = "sourceNodeName",
			P_targetNodeName = "targetNodeName",
			P_submitAction = "submitAction";

	@Inject
	private TableSamplerService tableSamplerService;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private DatarouterTableCountDao tableCountDao;
	@Inject
	private DatarouterTableSampleDao tableSampleDao;

	@Handler(defaultHandler = true)
	public Mav migrateTableCountMetadata(
			@Param(P_sourceNodeName) OptionalString sourceNodeName,
			@Param(P_targetNodeName) OptionalString targetNodeName,
			@Param(P_submitAction) OptionalString submitAction){
		List<String> possibleNodes = tableSamplerService.scanCountableNodes()
				.map(node -> node.getClientId().getName() + "." + node.getFieldInfo().getTableName())
				.append("")
				.sort()
				.list();
		var form = new HtmlForm()
				.withMethod("post");
		form.addSelectField()
				.withDisplay("Source Node Name")
				.withName(P_sourceNodeName)
				.withValues(possibleNodes);
		form.addSelectField()
				.withDisplay("Target Node Name")
				.withName(P_targetNodeName)
				.withValues(possibleNodes);
		form.addButton()
				.withDisplay("Migrate")
				.withValue("anything");

		if(submitAction.isEmpty() || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle("Migrate Table Count Metadata")
					.withContent(Html.makeContent(form))
					.buildMav();
		}
		migrate(sourceNodeName.get(), targetNodeName.get());
		String message = "Completed Table Count Metadata from " + sourceNodeName.get() + " to " + targetNodeName.get();
		var dto = new DatarouterChangelogDtoBuilder(
				"Table Count Metadata Migration",
				sourceNodeName.get() + " to " + targetNodeName.get(),
				"migrate",
				getSessionInfo().getNonEmptyUsernameOrElse(""))
				.build();
		changelogRecorder.record(dto);
		return pageFactory.message(request, message);
	}

	private void migrate(String sourceNode, String targetNode){
		String sourceClientName = sourceNode.split("\\.")[0];
		String sourceTableName = sourceNode.split("\\.")[1];
		String targetClientName = targetNode.split("\\.")[0];
		String targetTableName = targetNode.split("\\.")[1];

		//migrate rows in TableCount
		var tableCountKeyPrefix = new TableCountKey(sourceClientName, sourceTableName, null);
		tableCountDao.scanWithPrefix(tableCountKeyPrefix)
				.map(tableCount -> {
					tableCount.getKey().setClientName(targetClientName);
					tableCount.getKey().setTableName(targetTableName);
					return tableCount;
				})
				.batch(100)
				.forEach(tableCountDao::putMulti);

		//migrate rows in TableSample
		var tableSampleKeyPrefix = new TableSampleKey(sourceClientName, sourceTableName, null, null);
		tableSampleDao.scanWithPrefix(tableSampleKeyPrefix)
				.map(tableSample -> {
					tableSample.getKey().setClientName(targetClientName);
					tableSample.getKey().setTableName(targetTableName);
					return tableSample;
				})
				.batch(100)
				.forEach(tableSampleDao::putMulti);
	}

	private static class Html{

		public static DivTag makeContent(HtmlForm htmlForm){
			var form = Bootstrap4FormHtml.render(htmlForm)
					.withClass("card card-body bg-light");
			return div(
					h2("Table Count Migration"),
					form,
					br())
					.withClass("container mt-3");
		}

	}

}
