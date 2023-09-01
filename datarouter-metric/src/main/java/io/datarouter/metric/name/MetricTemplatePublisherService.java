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
package io.datarouter.metric.name;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.metric.collector.MetricTemplateDto;
import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.ServiceName;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class MetricTemplatePublisherService implements MetricTemplatePublisher{
	private static final Logger logger = LoggerFactory.getLogger(MetricTemplatePublisherService.class);

	@Inject
	private MetricTemplateQueueDao queueDao;
	@Inject
	private ServiceName serviceName;

	@Override
	public PublishingResponseDto publishTemplates(Collection<MetricTemplateDto> patterns){
		Scanner.of(patterns)
				.map(pattern -> new MetricTemplateBinaryDto(serviceName.get(), pattern))
				.batch(100)
				.each(batch -> logger.warn("writing size={} templates", batch.size()))
				.forEach(queueDao::combineAndPut);

		return PublishingResponseDto.SUCCESS;
	}

}
