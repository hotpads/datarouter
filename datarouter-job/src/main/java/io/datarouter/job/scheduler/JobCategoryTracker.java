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

import java.util.SortedSet;
import java.util.TreeSet;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JobCategoryTracker{

	private final SortedSet<String> jobCategoryNames;

	@Inject
	public JobCategoryTracker(){
		this.jobCategoryNames = new TreeSet<>();
	}

	public void register(String jobCategoryName){
		jobCategoryNames.add(jobCategoryName);
	}

	public SortedSet<String> getJobCategoryNames(){
		return jobCategoryNames;
	}

}
