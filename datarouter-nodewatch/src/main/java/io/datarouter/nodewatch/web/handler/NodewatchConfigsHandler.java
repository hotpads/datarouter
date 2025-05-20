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
import static j2html.TagCreator.dd;
import static j2html.TagCreator.div;
import static j2html.TagCreator.dl;
import static j2html.TagCreator.dt;

import java.util.List;

import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.config.DatarouterNodewatchPlugin;
import io.datarouter.nodewatch.link.NodewatchConfigsLink;
import io.datarouter.nodewatch.web.NodewatchHtml;
import io.datarouter.nodewatch.web.NodewatchNavService;
import io.datarouter.storage.node.tableconfig.NodewatchConfiguration;
import io.datarouter.storage.node.tableconfig.NodewatchConfigurationBuilder;
import io.datarouter.storage.node.tableconfig.TableConfigurationService;
import io.datarouter.util.BooleanTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class NodewatchConfigsHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterNodewatchPaths paths;
	@Inject
	private NodewatchNavService navService;
	@Inject
	private TableConfigurationService tableConfigurationService;

	@Handler
	public Mav configs(@SuppressWarnings("unused") NodewatchConfigsLink link){
		var content = div(
				NodewatchHtml.makeHeader(
						"Table Configs",
						"Tables with custom Nodewatch configurations"),
				navService.makeNavTabs(paths.datarouter.nodewatch.configs).render(),
				br(),
				makeOverviewDiv(),
				makeTableDiv())
				.withClass("container");
		return pageFactory.startBuilder(request)
				.withTitle(DatarouterNodewatchPlugin.NAME + " - Table Configs")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	private DivTag makeOverviewDiv(){
		var dl = dl(
				dt("Default Sample Interval"),
				dd(NumberFormatter.addCommas(NodewatchConfigurationBuilder.DEFAULT_SAMPLE_SIZE)),
				dt("Default Batch Size"),
				dd(NumberFormatter.addCommas(NodewatchConfigurationBuilder.DEFAULT_BATCH_SIZE)));
		return div(dl);
	}

	private DivTag makeTableDiv(){
		List<NodewatchConfiguration> rows = tableConfigurationService.getTableConfigurations();
		var table = makeTableBuilder(rows.size()).build(rows);
		return div(table);
	}

	private J2HtmlTable<NodewatchConfiguration> makeTableBuilder(int totalTables){
		return new J2HtmlTable<NodewatchConfiguration>()
				.withClasses("sortable table table-sm table-striped border")
				.withCaption("Total Tables: " + totalTables)
				.withColumn("Client", row -> row.nodeNameWrapper.getClientName())
				.withColumn("Table", row -> row.nodeNameWrapper.getTableName())
				.withColumn("Enabled", row -> row.isCountable, BooleanTool::toString)
				.withColumn(
						"Sample Size",
						row -> row.sampleSize,
						NumberFormatter::addCommas)
				.withColumn(
						"Batch Size",
						row -> row.batchSize,
						NumberFormatter::addCommas)
				.withColumn("Percentage Alert Enabled", row -> row.enablePercentageAlert, BooleanTool::toString)
				.withColumn("Threshold Alert Enabled", row -> row.enableThresholdAlert, BooleanTool::toString);
	}

}
