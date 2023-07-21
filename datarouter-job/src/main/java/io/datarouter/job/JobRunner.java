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
package io.datarouter.job;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.task.MemoryTaskTracker;
import io.datarouter.instrumentation.task.TaskTracker;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Lightweight class for running jobs in the current thread without locking.
 */
@Singleton
public class JobRunner{

	@Inject
	private DatarouterInjector injector;

	/**
	 * Set up the minimum execution environment needed by a test
	 */
	public void runClassInTest(Class<? extends BaseJob> jobClass){
		BaseJob job = injector.getInstance(jobClass);
		runInTest(job);
	}

	private void runInTest(BaseJob job){
		Class<? extends BaseJob> jobClass = job.getClass();
		TaskTracker tracker = new MemoryTaskTracker(jobClass);
		try{
			job.run(tracker);
		}catch(RuntimeException e){
			throw e;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

}
