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
package io.datarouter.job.metriclink;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.TriggerGroupClasses;
import io.datarouter.job.scheduler.JobPackage;
import io.datarouter.scanner.WarnOnModifyList;
import io.datarouter.storage.tag.Tag;
import io.datarouter.web.metriclinks.MetricLinkDto;
import io.datarouter.web.metriclinks.MetricLinkDto.LinkDto;
import io.datarouter.web.metriclinks.MetricLinkPage;
import jakarta.inject.Inject;

public abstract class JobMetricLinkPage implements MetricLinkPage{

	@Inject
	private TriggerGroupClasses triggerGroupClasses;

	@Override
	public String getName(){
		return "Jobs";
	}

	protected List<MetricLinkDto> buildMetricLinks(Tag tag){
		return triggerGroupClasses.get().stream()
				.filter(triggerGroup -> triggerGroup.tag == tag)
				.map(BaseTriggerGroup::getJobPackages)
				.flatMap(Collection::stream)
				.map(JobPackage::toString)
				.map(jobName -> {
					var availbleMetric = LinkDto.of("Datarouter job " + jobName);
					return new MetricLinkDto(jobName, Optional.empty(), Optional.of(availbleMetric));
				})
				.collect(WarnOnModifyList.deprecatedCollector());
	}

	/*
	 * DO NOT DELETE
	 * unused but keep this example to figure out why IntelliJ throws an error
	 * the eclipse compiler doesn't show any errors or warnings
	 */
	@SuppressWarnings("unused")
	private List<MetricLinkDto> buildMetricLinksScanner(Tag tag){
		return triggerGroupClasses.get()
				.include(triggerGroup -> triggerGroup.tag == tag)
				.concatIter(BaseTriggerGroup::getJobPackages)
				.map(JobPackage::toString)
				.map(jobName -> {
					var availbleMetric = LinkDto.of("Datarouter job " + jobName);
					return new MetricLinkDto(jobName, Optional.empty(), Optional.of(availbleMetric));
				})
				.list();
	}

}
