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

import java.time.Duration;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.nodewatch.config.DatarouterNodewatchExecutors.DatarouterTableSamplerExecutor;
import io.datarouter.nodewatch.joblet.TableSpanSamplerJobletCreator;
import io.datarouter.nodewatch.joblet.TableSpanSamplerJobletCreatorFactory;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.scanner.Threads;
import jakarta.inject.Inject;

public class TableSamplerJob extends BaseJob{

	// If this runs too frequently, then the table with the most samples may not be able to finish scanning the samples
	// and creating joblets
	public static final Duration SCHEDULING_INTERVAL = Duration.ofMinutes(3);

	@Inject
	private DatarouterTableSamplerExecutor executor;
	@Inject
	private TableSamplerService tableSamplerService;
	@Inject
	private TableSpanSamplerJobletCreatorFactory jobletCreatorFactory;

	@Override
	public void run(TaskTracker tracker){
		long startTimeMs = System.currentTimeMillis();
		tableSamplerService.scanCountableNodes()
				.map(node -> jobletCreatorFactory.create(
						node,
						tableSamplerService.getSampleInterval(node),
						tableSamplerService.getBatchSize(node),
						false,
						true,
						startTimeMs))
				.parallelUnordered(new Threads(executor, 10))
				.each(TableSpanSamplerJobletCreator::createJoblets)
				.advanceUntil(_ -> tracker.increment().shouldStop())
				.count();
	}

}
