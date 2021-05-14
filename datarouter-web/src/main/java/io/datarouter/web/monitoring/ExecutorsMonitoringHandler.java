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
package io.datarouter.web.monitoring;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import io.datarouter.inject.InstanceRegistry;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.concurrent.NamedThreadFactory;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;

public class ExecutorsMonitoringHandler extends BaseHandler{

	@Inject
	private InstanceRegistry instanceRegistry;
	@Inject
	private DatarouterWebFiles files;

	@Handler(defaultHandler = true)
	protected Mav view(){
		Mav mav = new Mav(files.jsp.admin.datarouter.executorsMonitoring.executorsJsp);
		mav.put("executors", getExecutors());
		return mav;
	}

	@Handler
	public Collection<TextExecutor> getExecutors(){
		return Scanner.of(instanceRegistry.getAllInstancesOfType(ThreadPoolExecutor.class))
				.map(TextExecutor::new)
				.sort(Comparator.comparing(executor -> StringTool.nullSafe(executor.name)))
				.list();
	}

	public static class TextExecutor{

		private int poolSize;
		private int activeCount;
		private String maxPoolSize;
		private String queueSize;
		private String name;
		private String remainingQueueCapacity;
		private long completedTaskCount;

		public TextExecutor(ThreadPoolExecutor executor){
			this.poolSize = executor.getPoolSize();
			this.activeCount = executor.getActiveCount();
			this.maxPoolSize = asString(executor.getMaximumPoolSize());
			this.queueSize = asString(executor.getQueue().size());
			this.remainingQueueCapacity = asString(executor.getQueue().remainingCapacity());
			this.completedTaskCount = executor.getCompletedTaskCount();
			this.name = NamedThreadFactory.findName(executor.getThreadFactory()).orElse(null);
		}

		private String asString(int num){
			return num == Integer.MAX_VALUE ? "MAX" : String.valueOf(num);
		}

		public int getPoolSize(){
			return poolSize;
		}

		public int getActiveCount(){
			return activeCount;
		}

		public String getMaxPoolSize(){
			return maxPoolSize;
		}

		public String getQueueSize(){
			return queueSize;
		}

		public String getName(){
			return name;
		}

		public String getRemainingQueueCapacity(){
			return remainingQueueCapacity;
		}

		public long getCompletedTaskCount(){
			return completedTaskCount;
		}

	}

}
