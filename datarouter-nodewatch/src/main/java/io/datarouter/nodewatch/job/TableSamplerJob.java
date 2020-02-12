/**
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

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.nodewatch.config.DatarouterNodewatchExecutors.DatarouterTableSamplerExecutor;
import io.datarouter.nodewatch.joblet.TableSpanSamplerJobletCreator;
import io.datarouter.nodewatch.joblet.TableSpanSamplerJobletCreatorFactory;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.util.concurrent.FutureTool;

public class TableSamplerJob extends BaseJob{

	public static final Duration SCHEDULING_INTERVAL = Duration.ofMinutes(10);

	@Inject
	private DatarouterTableSamplerExecutor executor;
	@Inject
	private TableSamplerService tableSamplerService;
	@Inject
	private TableSpanSamplerJobletCreatorFactory jobletCreatorFactory;

	@Override
	public void run(TaskTracker tracker){
		long startTimeMs = System.currentTimeMillis();
		List<TableSpanSamplerJobletCreator<?,?,?>> jobletCreators = tableSamplerService.streamCountableNodes()
				.map(node -> jobletCreatorFactory.create(node, tableSamplerService.getSampleInterval(node),
						tableSamplerService.getBatchSize(node), false, true, startTimeMs))
				.collect(Collectors.toList());
		FutureTool.submitAndGetAll(jobletCreators, executor);
	}

}
