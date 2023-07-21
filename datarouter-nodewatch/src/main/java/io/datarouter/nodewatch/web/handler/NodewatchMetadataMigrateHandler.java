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
package io.datarouter.nodewatch.web.handler;

import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h5;

import java.util.List;
import java.util.Optional;

import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.config.DatarouterNodewatchPlugin;
import io.datarouter.nodewatch.service.NodewatchChangelogService;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.nodewatch.storage.tablecount.DatarouterTableCountDao;
import io.datarouter.nodewatch.storage.tablecount.TableCountKey;
import io.datarouter.nodewatch.storage.tablesample.DatarouterTableSampleDao;
import io.datarouter.nodewatch.storage.tablesample.TableSampleKey;
import io.datarouter.nodewatch.web.NodewatchHtml;
import io.datarouter.nodewatch.web.NodewatchNavService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class NodewatchMetadataMigrateHandler extends BaseHandler{

	private static final String
			P_sourceNodeName = "sourceNodeName",
			P_targetNodeName = "targetNodeName";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterNodewatchPaths paths;
	@Inject
	private NodewatchNavService navService;
	@Inject
	private TableSamplerService tableSamplerService;
	@Inject
	private NodewatchChangelogService changelogService;
	@Inject
	private DatarouterTableCountDao tableCountDao;
	@Inject
	private DatarouterTableSampleDao tableSampleDao;

	@Handler
	public Mav migrate(
			Optional<String> sourceNodeName,
			Optional<String> targetNodeName){
		boolean shouldValidate = sourceNodeName.isPresent();
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
		form.addButtonWithoutSubmitAction()
				.withDisplay("Migrate");

		if(!shouldValidate || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle(DatarouterNodewatchPlugin.NAME + " - Migrate Metadata")
					.withContent(makeFormContent(form))
					.buildMav();
		}
		migrate(sourceNodeName.get(), targetNodeName.get());
		changelogService.recordMigrateMetadata(getSessionInfo(), sourceNodeName.get(), targetNodeName.get());
		return pageFactory.startBuilder(request)
				.withTitle(DatarouterNodewatchPlugin.NAME + " - Migrate Metadata Complete")
				.withContent(makeResultContent(sourceNodeName.get(), targetNodeName.get()))
				.buildMav();
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

	private DivTag makeFormContent(HtmlForm htmlForm){
		var form = Bootstrap4FormHtml.render(htmlForm)
				.withClass("card card-body bg-light");
		return div(
				NodewatchHtml.makeHeader(
						"Migrate Metadata",
						"Copy Samples, Counts, and Latest Counts from one table to another with the same schema"),
				navService.makeNavTabs(paths.datarouter.nodewatch.metadata.migrate).render(),
				br(),
				form,
				br())
				.withClass("container");
	}

	private DivTag makeResultContent(String from, String to){
		String message = String.format("Migrated Nodewatch Metadata from %s to %s", from, to);
		return div(
				NodewatchHtml.makeHeader("Migrate Metadata Complete", "Migration Complete"),
				navService.makeNavTabs(paths.datarouter.nodewatch.metadata.migrate).render(),
				br(),
				h5(message).withClass("ml-5"))
				.withClass("container");
	}

}
