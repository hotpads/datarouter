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
package io.datarouter.joblet.metriclink;

import java.util.List;
import java.util.Optional;

import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletTypeFactory;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.tag.Tag;
import io.datarouter.web.metriclinks.MetricLinkDto;
import io.datarouter.web.metriclinks.MetricLinkDto.LinkDto;
import io.datarouter.web.metriclinks.MetricLinkPage;
import jakarta.inject.Inject;

public abstract class JobletMetricLinkPage implements MetricLinkPage{

	@Inject
	private JobletTypeFactory jobletTypeFactory;

	@Override
	public String getName(){
		return "Joblets";
	}

	protected List<MetricLinkDto> buildMetricLinks(Tag tag){
		return Scanner.of(jobletTypeFactory.getAllTypes())
				.include(type -> type.tag == tag)
				.map(JobletType::getPersistentString)
				.map(type -> {
					var link = LinkDto.of("Joblet .* " + type + "$");
					return new MetricLinkDto(type, Optional.empty(), Optional.of(link));
				})
				.list();
	}

}
