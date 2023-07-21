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

import java.util.Comparator;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.nodewatch.service.TableStorageSummarizer;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import jakarta.inject.Inject;

public class TableStorageSummaryJob extends BaseJob{

	private static final long LIMIT_PER_TABLE = 4_000_000;

	@Inject
	private TableSamplerService tableSamplerService;
	@Inject
	private DatarouterNodes datarouterNodes;

	@Override
	public void run(TaskTracker tracker){
		tableSamplerService.scanCountableNodes()
				.sort(Comparator.comparing(Node::getName))
				.advanceUntil($ -> tracker.shouldStop())
				.each($ -> tracker.increment())
				.map(node -> new TableStorageSummarizer<>(
						tracker::shouldStop,
						tableSamplerService,
						datarouterNodes,
						node.getClientId().getName(),
						node.getFieldInfo().getTableName(),
						LIMIT_PER_TABLE))
				.forEach(TableStorageSummarizer::summarize);
	}

}
