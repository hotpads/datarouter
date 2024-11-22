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

import static j2html.TagCreator.caption;
import static j2html.TagCreator.div;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.tr;

import java.util.Optional;

import io.datarouter.auth.role.DatarouterUserRoleRegistry;
import io.datarouter.storage.config.DatarouterPropertiesService;
import io.datarouter.web.browse.widget.NodeWidgetTableCountLinkSupplier;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import jakarta.inject.Inject;

public class DatarouterHomepageHandler extends BaseHandler{

	@Inject
	private DatarouterWebFiles files;
	@Inject
	private DatarouterWebPaths paths;
	@Inject
	private NodeWidgetTableCountLinkSupplier nodeWidgetTableCountLink;
	@Inject
	private DatarouterPropertiesService datarouterPropertiesService;
	@Inject
	private DatarouterClientHandlerService clientHandlerService;

	@Handler(defaultHandler = true)
	protected Mav view(){
		Mav mav = new Mav(files.jsp.admin.datarouter.datarouterMenuJsp);

		//DatarouterProperties info
		mav.put("serverPropertiesTable", buildDatarouterPropertiesTable());

		//Clients
		var showStorage = getSessionInfo().hasRole(DatarouterUserRoleRegistry.DATAROUTER_ADMIN);
		mav.put("showStorage", showStorage);
		if(showStorage){
			mav.put("clientsTable",
					clientHandlerService.buildClientsTable()
							.withClass("col-12 col-sm-6")
							.render());

			mav.put("viewNodeDataPath", paths.datarouter.nodes.browseData.toSlashedString()
					+ "?submitAction=browseData&nodeName=");
			mav.put("getNodeDataPath", paths.datarouter.nodes.getData.toSlashedString() + "?nodeName=");
			mav.put("countKeysPath", paths.datarouter.nodes.browseData.toSlashedString() + "/countKeys?nodeName=");

			Optional<String> tableCountLink = Optional.ofNullable(nodeWidgetTableCountLink.get())
					.map(link -> link + "?nodeName=");
			mav.put("tableCountLink", tableCountLink.orElse(""));
			mav.put("showTableCountLink", tableCountLink.isPresent());
		}
		return mav;
	}

	private String buildDatarouterPropertiesTable(){
		var tbody = tbody();
		datarouterPropertiesService.getAllProperties().stream()
				.map(entry -> tr(td(entry.key()), td(entry.value() == null ? "" : entry.value())))
				.forEach(tbody::with);
		var table = table(caption("Server Info").withStyle("caption-side: top"), tbody)
				.withClass("table table-striped table-bordered table-sm");
		return div(table)
				.withClass("col-12 col-sm-6")
				.render();
	}
}
