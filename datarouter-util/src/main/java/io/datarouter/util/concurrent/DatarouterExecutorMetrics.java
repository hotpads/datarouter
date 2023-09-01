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
package io.datarouter.util.concurrent;

import io.datarouter.instrumentation.metric.node.BaseMetricRoot;
import io.datarouter.instrumentation.metric.node.MetricNode;

public class DatarouterExecutorMetrics extends BaseMetricRoot{

	public static final String METRIC_ROOT = "executor";
	private static final DatarouterExecutorMetrics ROOT = new DatarouterExecutorMetrics();

	public static DatarouterNamedExecutorMetrics name(String name){
		return ROOT.executorName(name);
	}

	//setup

	private DatarouterExecutorMetrics(){
		super(METRIC_ROOT);
	}

	private DatarouterNamedExecutorMetrics executorName(String name){
		return variable(DatarouterNamedExecutorMetrics::new, name);
	}

	public static class DatarouterNamedExecutorMetrics
	extends MetricNodeVariable<DatarouterNamedExecutorMetrics>{
		private DatarouterNamedExecutorMetrics(){
			super("name", "Executor name", DatarouterNamedExecutorMetrics::new);
		}

		public final MetricNode processed = literal("processed");
		public final MetricNode callerRuns = literal("callerRuns");

		public final MetricNode poolSize = literal("poolSize");
		public final MetricNode activeCount = literal("activeCount");
		public final MetricNode queueSize = literal("queueSize");
		public final MetricNode maxPoolSize = literal("maxPoolSize");
		public final MetricNode remainingQueueCapacity = literal("remainingQueueCapacity");
		public final MetricNode completedTaskCount = literal("completedTaskCount");
	}

}
