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

import javax.inject.Inject;

import io.datarouter.nodewatch.joblet.TableSpanSamplerJobletCreatorFactory;
import io.datarouter.nodewatch.service.NodewatchChangelogService;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.nodewatch.storage.latesttablecount.DatarouterLatestTableCountDao;
import io.datarouter.nodewatch.storage.latesttablecount.LatestTableCountKey;
import io.datarouter.nodewatch.storage.tablecount.DatarouterTableCountDao;
import io.datarouter.nodewatch.storage.tablecount.TableCountKey;
import io.datarouter.nodewatch.storage.tablesample.DatarouterTableSampleDao;
import io.datarouter.nodewatch.storage.tablesample.TableSampleKey;
import io.datarouter.nodewatch.web.NodewatchLinks;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.PhysicalSortedStorageReaderNode;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.GlobalRedirectMav;

public class NodewatchTableActionsHandler extends BaseHandler{

	public static final String
			P_clientName = "clientName",
			P_tableName = "tableName";

	@Inject
	private NodewatchLinks links;
	@Inject
	private DatarouterNodes nodes;
	@Inject
	private TableSamplerService tableSamplerService;
	@Inject
	private TableSpanSamplerJobletCreatorFactory tableSpanSamplerJobletCreatorFactory;
	@Inject
	private DatarouterTableSampleDao tableSampleDao;
	@Inject
	private DatarouterTableCountDao tableCountDao;
	@Inject
	private DatarouterLatestTableCountDao latestTableCountDao;
	@Inject
	private NodewatchChangelogService changelogService;

	@Handler
	public Mav resample(String clientName, String tableName){
		var node = (PhysicalSortedStorageReaderNode<?,?,?>)nodes.getPhysicalNodeForClientAndTable(
				clientName,
				tableName);
		tableSpanSamplerJobletCreatorFactory.create(
				node,
				tableSamplerService.getSampleInterval(node),
				tableSamplerService.getBatchSize(node),
				true,
				true,
				System.currentTimeMillis())
				.createJoblets();
		changelogService.recordTable(getSessionInfo(), clientName, tableName, "resample table");
		return new GlobalRedirectMav(links.table(clientName, tableName));
	}

	@Handler
	public Mav deleteSamples(String clientName, String tableName){
		var prefix = TableSampleKey.prefix(clientName, tableName);
		tableSampleDao.deleteWithPrefix(prefix);
		changelogService.recordTable(getSessionInfo(), clientName, tableName, "delete table samples");
		return new GlobalRedirectMav(links.table(clientName, tableName));
	}

	@Handler
	public Mav deleteAllMetadata(String clientName, String tableName){
		var tableCountPrefix = TableCountKey.prefix(clientName, tableName);
		tableCountDao.deleteWithPrefix(tableCountPrefix);
		var tableSamplePrefix = TableSampleKey.prefix(clientName, tableName);
		tableSampleDao.deleteWithPrefix(tableSamplePrefix);
		var latestTableCountKey = new LatestTableCountKey(clientName, tableName);
		latestTableCountDao.delete(latestTableCountKey);
		changelogService.recordTable(getSessionInfo(), clientName, tableName, "delete table metadata");
		return new GlobalRedirectMav(links.table(clientName, tableName));
	}

}
