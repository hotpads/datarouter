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
package io.datarouter.job.scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Singleton;

import io.datarouter.job.BaseJob;

@Singleton
public class JobPackageTracker{

	private final SortedSet<JobPackage> jobPackages = new TreeSet<>();
	private final Map<Class<? extends BaseJob>,JobPackage> jobPackageByJobClass = new HashMap<>();

	public void register(JobPackage jobPackage){
		if(jobPackages.contains(jobPackage)){
			throw new IllegalArgumentException("jobPackage already registered:" + jobPackage);
		}
		jobPackages.add(jobPackage);
		jobPackageByJobClass.put(jobPackage.jobClass, jobPackage);
	}

	public SortedSet<JobPackage> getJobPackages(){
		return jobPackages;
	}

	public JobPackage getForClass(Class<? extends BaseJob> jobClass){
		return jobPackageByJobClass.get(jobClass);
	}

}
