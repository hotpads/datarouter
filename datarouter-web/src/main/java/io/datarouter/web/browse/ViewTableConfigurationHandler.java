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
package io.datarouter.web.browse;

import static j2html.TagCreator.dd;
import static j2html.TagCreator.div;
import static j2html.TagCreator.dl;
import static j2html.TagCreator.dt;
import static j2html.TagCreator.h2;

import java.util.List;

import javax.inject.Inject;

import io.datarouter.storage.node.tableconfig.NodewatchConfiguration;
import io.datarouter.storage.node.tableconfig.NodewatchConfigurationBuilder;
import io.datarouter.storage.node.tableconfig.TableConfigurationService;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.specialized.DivTag;

public class ViewTableConfigurationHandler extends BaseHandler{

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private TableConfigurationService tableConfigurationService;

	@Handler(defaultHandler = true)
	public Mav view(){
		List<NodewatchConfiguration> rows = tableConfigurationService.getTableConfigurations();
		return pageFactory.startBuilder(request)
				.withTitle("Custom Table Configurations")
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(makeContent(rows))
				.buildMav();
	}

	private static DivTag makeContent(List<NodewatchConfiguration> rows){
		var header = h2("Custom Table Configurations");
		var overview = dl(
				dt("Default Sample Interval"),
				dd(format(NodewatchConfigurationBuilder.DEFAULT_SAMPLE_SIZE)),
				dt("Default Batch Size"),
				dd(format(NodewatchConfigurationBuilder.DEFAULT_BATCH_SIZE)));
		var table = new J2HtmlTable<NodewatchConfiguration>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withCaption("Total Tables: " + rows.size())
				.withColumn("Client", row -> row.nodeNameWrapper.getClientName())
				.withColumn("Table", row -> row.nodeNameWrapper.getTableName())
				.withColumn("Enabled", row -> row.isCountable)
				.withColumn("Sample Size", row -> format(row.sampleSize))
				.withColumn("Batch Size", row -> format(row.batchSize))
				.withColumn("Percentage Alert Enabled", row -> row.enablePercentageAlert)
				.withColumn("Threshold Alert Enabled", row -> row.enableThresholdAlert)
				.build(rows);
		return div(header, overview, table)
				.withClass("container-fluid my-4");
	}

	private static String format(Integer num){
		if(num == null){
			return "";
		}
		return NumberFormatter.addCommas(num);
	}

}
