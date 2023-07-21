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
package io.datarouter.job.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.datarouter.job.BaseJob;
import io.datarouter.job.scheduler.JobWrapper;
import io.datarouter.job.util.Outcome;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class LocalTriggerLockService{

	// allow only one JobWrapper per class
	private final ConcurrentMap<Class<? extends BaseJob>,JobWrapper> jobWrapperByJobClass;

	@Inject
	public LocalTriggerLockService(){
		this.jobWrapperByJobClass = new ConcurrentHashMap<>();
	}

	public Outcome acquire(JobWrapper jobWrapper){
		JobWrapper existingJobWrapper = jobWrapperByJobClass.putIfAbsent(jobWrapper.job.getClass(), jobWrapper);

		return existingJobWrapper == null // no previous JobWrapper found, so we got the lock
				? Outcome.success()
				: Outcome.failure("Unable to acquire local lock for job " + jobWrapper.job);
	}

	public void release(Class<? extends BaseJob> jobClass){
		jobWrapperByJobClass.remove(jobClass);
	}

	public JobWrapper getForClass(Class<? extends BaseJob> key){
		return jobWrapperByJobClass.get(key);
	}

	public int getNumRunningJobs(){
		return jobWrapperByJobClass.size();
	}

	public List<JobWrapper> getJobWrappers(){
		return new ArrayList<>(jobWrapperByJobClass.values());
	}

	public void onShutdown(){
		jobWrapperByJobClass.values().forEach(JobWrapper::requestStop);
	}

}
