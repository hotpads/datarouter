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
package io.datarouter.client.git;

import org.eclipse.jgit.lib.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.web.shutdown.ShutdownService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterGitProgressMonitorFactory{

	@Inject
	private ShutdownService shutdownService;

	public DatarouterGitProgressMonitor newMonitor(DatarouterGitOp op){
		return new DatarouterGitProgressMonitor(op);
	}

	public class DatarouterGitProgressMonitor implements ProgressMonitor{
		private static final Logger logger = LoggerFactory.getLogger(DatarouterGitProgressMonitor.class);

		private final DatarouterGitOp op;
		private Long startMs = null;

		private DatarouterGitProgressMonitor(DatarouterGitOp op){
			this.op = op;
		}

		@Override
		public void start(int totalTasks){
			startMs = System.currentTimeMillis();
			DatarouterGitMetrics.incOp(op);
			logger.info("{} start totalTasks={}", op.name, totalTasks);
		}

		@Override
		public void beginTask(String title, int totalWork){
			logger.info("{} being task title={} totalWork={}", op.name, title, totalWork);
		}

		@Override
		public void update(int completed){
			logger.debug("{} update completed={}", op.name, completed);
		}

		@Override
		public void endTask(){
			if(startMs != null){
				long diffMs = System.currentTimeMillis() - startMs;
				Metrics.measure("Git " + op.name + " durationMs", diffMs);
			}
			logger.info("{} end task", op.name);
		}

		@Override
		public boolean isCancelled(){
			boolean isCancelled = shutdownService.isShutdownOngoing();
			logger.debug("{} isCancelled={}", op.name, isCancelled);
			return isCancelled;
		}

		@Override
		public void showDuration(boolean enabled){
			if(enabled){
				long durationMs = System.currentTimeMillis() - startMs.longValue();
				logger.info(" duration [ms]", durationMs);
			}
		}

	}

}
