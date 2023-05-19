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

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import io.datarouter.plugin.dataexport.config.DatarouterDataExportDirectorySupplier;
import io.datarouter.plugin.dataexport.service.DatabeanImportService;
import io.datarouter.plugin.dataexport.service.DatarouterDataExportChangelogService;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;
import io.datarouter.storage.util.Subpath;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;

public class DatarouterDatabeanImportHandler extends BaseHandler{

	public static final String
			P_exportId = "exportId",
			P_nodeName = "nodeName";

	private static final int PUT_BATCH_SIZE = 100;

	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private DatabeanImportService databeanImportService;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private DatarouterDataExportDirectorySupplier directorySupplier;
	@Inject
	private DatarouterDataExportChangelogService changelogService;

	@Handler
	private Mav importDatabeans(
			@Param(P_exportId) String exportId){

		// collect input filenames
		List<PathbeanKey> keys = directorySupplier.getDirectory().scanKeys(new Subpath(exportId))
				.list();

		// import each file
		var totalDatabeans = new AtomicLong(0);
		keys.forEach(key -> {
			String nodeName = key.getFile();
			MapStorageNode<?,?,?> node = (MapStorageNode<?,?,?>)datarouterNodes.getNode(nodeName);
			long numDatabeans = databeanImportService.importFromDirectory(
					directorySupplier.getDirectory(),
					key,
					exportId,
					node,
					PUT_BATCH_SIZE);
			totalDatabeans.addAndGet(numDatabeans);
		});

		// changelog
		changelogService.record(
				exportId,
				"localImport",
				getSessionInfo().getRequiredSession().getUsername());

		// results
		return pageFactory.startBuilder(request)
				.withTitle("Databean Import Complete")
				.withContent(DatabeanExportHtml.makeImportCompleteContent(exportId, keys, totalDatabeans.get()))
				.buildMav();
	}

}
