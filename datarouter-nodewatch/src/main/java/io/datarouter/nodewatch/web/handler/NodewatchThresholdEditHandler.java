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

import java.util.Optional;

import io.datarouter.nodewatch.config.DatarouterNodewatchPaths;
import io.datarouter.nodewatch.config.DatarouterNodewatchPlugin;
import io.datarouter.nodewatch.service.NodewatchChangelogService;
import io.datarouter.nodewatch.storage.alertthreshold.DatarouterTableSizeAlertThresholdDao;
import io.datarouter.nodewatch.storage.alertthreshold.TableSizeAlertThreshold;
import io.datarouter.nodewatch.storage.alertthreshold.TableSizeAlertThresholdKey;
import io.datarouter.nodewatch.web.NodewatchHtml;
import io.datarouter.nodewatch.web.NodewatchLinks;
import io.datarouter.nodewatch.web.NodewatchNavService;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.GlobalRedirectMav;
import io.datarouter.web.html.form.HtmlForm;
import io.datarouter.web.html.form.HtmlForm.HtmlFormMethod;
import io.datarouter.web.html.form.HtmlFormValidator;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4FormHtml;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import jakarta.inject.Inject;

public class NodewatchThresholdEditHandler extends BaseHandler{

	public static final String
			P_clientName = "clientName",
			P_tableName = "tableName",
			P_maxRows = "maxRows",
			P_update = "update";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatarouterNodewatchPaths paths;
	@Inject
	private NodewatchLinks links;
	@Inject
	private NodewatchNavService navService;
	@Inject
	private DatarouterTableSizeAlertThresholdDao dao;
	@Inject
	private NodewatchChangelogService changelogService;

	@Handler
	public Mav edit(
			String clientName,
			String tableName,
			Optional<String> maxRows,
			Optional<Boolean> update){

		var key = new TableSizeAlertThresholdKey(clientName, tableName);
		Optional<String> existingMaxRows = dao.find(key)
				.map(TableSizeAlertThreshold::getMaxRows)
				.map(Number::toString);

		var header = NodewatchHtml.makeHeader(
				"Edit Alerting Threshold",
				"Recieve an email alert if the table exceeds this max row count");
		var nav = navService.makeNavTabs(paths.datarouter.nodewatch.threshold.edit)
				.addThresholdEditTab(clientName, tableName)
				.render();
		var subheader = NodewatchHtml.makeTableInfoDiv(clientName, tableName);

		// show form
		var form = new HtmlForm(HtmlFormMethod.POST)
				.withAction(paths.datarouter.nodewatch.threshold.edit.getValue());
		form.addHiddenField(P_clientName, clientName);
		form.addHiddenField(P_tableName, tableName);
		form.addTextField()
				.withLabel("Alert over N rows")
				.withName(P_maxRows)
				.withValue(
						maxRows.or(() -> existingMaxRows).orElse(""),
						update.orElse(false),
						HtmlFormValidator::positiveLong)
				.withPlaceholder("number");
		form.addButtonWithoutSubmitAction()
				.withLabel("Update")
				.withName(P_update)
				.withValue(Boolean.TRUE.toString());
		var formDiv = Bootstrap4FormHtml.render(form)
				.withClass("card card-body bg-light")
				.withStyle("width:300px");

		var deleteButton = NodewatchHtml.makeDangerButton(
				"Delete threshold",
				links.thresholdDelete(clientName, tableName),
				String.format("Are you sure you want to delete the alert for %s?", tableName));
		var content = div(
				header,
				nav,
				br(),
				subheader,
				br(),
				formDiv,
				br(),
				deleteButton)
				.withClass("container");

		if(!update.orElse(false) || form.hasErrors()){
			return pageFactory.startBuilder(request)
					.withTitle(DatarouterNodewatchPlugin.NAME + " - Edit Threshold")
					.withContent(content)
					.buildMav();
		}

		//process
		var databean = new TableSizeAlertThreshold(
				key,
				maxRows.map(Long::valueOf).orElseThrow());
		dao.put(databean);
		changelogService.recordTable(getSessionInfo(), clientName, tableName, "edit threshold");
		return new GlobalRedirectMav(links.tables());
	}

	@Handler
	public Mav delete(
			String clientName,
			String tableName){
		var key = new TableSizeAlertThresholdKey(clientName, tableName);
		dao.delete(key);
		changelogService.recordTable(getSessionInfo(), clientName, tableName, "delete threshold");
		return new GlobalRedirectMav(links.tables());
	}


}
