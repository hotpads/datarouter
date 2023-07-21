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
package io.datarouter.plugin.dataexport.web;

import io.datarouter.plugin.dataexport.service.DatabeanExportChangelogService;
import io.datarouter.plugin.dataexport.service.importing.DatabeanImportService;
import io.datarouter.plugin.dataexport.service.importing.DatabeanImportService.DatabeanImportResponse;
import io.datarouter.types.Ulid;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import jakarta.inject.Inject;

public class DatabeanImportHandler extends BaseHandler{

	public static final String
			P_exportId = "exportId",
			P_nodeName = "nodeName";

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatabeanImportService databeanImportService;
	@Inject
	private DatabeanExportChangelogService changelogService;

	@Handler
	private Mav importDatabeans(
			@Param(P_exportId) String exportIdString){
		Ulid exportId = new Ulid(exportIdString);

		// import
		DatabeanImportResponse response = databeanImportService.importAllTables(exportId);

		// changelog
		changelogService.record(
				exportId.toString(),
				"localImport",
				getSessionInfo().getRequiredSession().getUsername());

		// results
		return pageFactory.startBuilder(request)
				.withTitle("Databean Import Complete")
				.withContent(DatabeanExportHtml.makeImportCompleteContent(
						exportId.toString(),
						response.nodeNames(),
						response.totalDatabeans()))
				.buildMav();
	}

}
