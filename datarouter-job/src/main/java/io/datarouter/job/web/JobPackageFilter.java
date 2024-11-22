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
package io.datarouter.job.web;

import java.util.stream.Stream;

import io.datarouter.job.scheduler.JobPackage;
import io.datarouter.job.scheduler.JobPackageTracker;
import io.datarouter.util.string.StringTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JobPackageFilter{

	@Inject
	private JobPackageTracker jobPackageTracker;

	public Stream<JobPackage> streamMatches(
			String categoryName,
			String keyword,
			boolean hideEnabled,
			boolean hideDisabled){
		return jobPackageTracker.getJobPackages().stream()
				.filter(jobPackage -> matchesEnabled(jobPackage, hideEnabled, hideDisabled))
				.filter(jobPackage -> matchesCategoryName(jobPackage, categoryName))
				.filter(jobPackage -> matchesKeyword(jobPackage, keyword));
	}

	private static boolean matchesEnabled(
			JobPackage jobPackage,
			boolean hideEnabled,
			boolean hideDisabled){
		boolean enabled = jobPackage.shouldRunSupplier.get();
		if(hideEnabled && enabled){
			return false;
		}
		return !hideDisabled || enabled;
	}

	private static boolean matchesCategoryName(JobPackage jobPackage, String categoryName){
		if(StringTool.isNullOrEmptyOrWhitespace(categoryName)){
			return true;
		}
		return StringTool.equalsCaseInsensitive(jobPackage.jobCategoryName, categoryName);
	}

	private static boolean matchesKeyword(JobPackage jobPackage, String keyword){
		if(StringTool.isEmpty(keyword)){
			return true;
		}
		return jobPackage.jobClass.getSimpleName().toLowerCase().contains(keyword.toLowerCase());
	}

}
