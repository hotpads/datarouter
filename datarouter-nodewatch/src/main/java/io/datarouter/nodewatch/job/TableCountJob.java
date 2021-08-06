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
package io.datarouter.nodewatch.job;

import javax.inject.Inject;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.nodewatch.storage.latesttablecount.DatarouterLatestTableCountDao;
import io.datarouter.nodewatch.storage.latesttablecount.LatestTableCount;
import io.datarouter.nodewatch.storage.tablecount.DatarouterTableCountDao;
import io.datarouter.nodewatch.storage.tablecount.TableCount;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.PhysicalSortedStorageReaderNode;
import io.datarouter.storage.node.tableconfig.ClientTableEntityPrefixNameWrapper;

public class TableCountJob extends BaseJob{

	@Inject
	private DatarouterLatestTableCountDao latestTableCountDao;
	@Inject
	private DatarouterTableCountDao tableCountDao;
	@Inject
	private TableSamplerService tableSamplerService;

	@Override
	public void run(TaskTracker tracker){
		for(PhysicalSortedStorageReaderNode<?,?,?> node : tableSamplerService.scanCountableNodes().iterable()){
			ClientTableEntityPrefixNameWrapper nodeNames = new ClientTableEntityPrefixNameWrapper(node);
			String clientName = nodeNames.getClientName();
			String tableName = nodeNames.getTableName();
			TableCount tableCount = tableSamplerService.getCurrentTableCountFromSamples(clientName, tableName);
			tableCountDao.put(tableCount);
			latestTableCountDao.put(new LatestTableCount(tableCount));
			if(tracker.increment().setLastItemProcessed(nodeNames.toString()).shouldStop()){
				return;
			}
		}
	}
}
