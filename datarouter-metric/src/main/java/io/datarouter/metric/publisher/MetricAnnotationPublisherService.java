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
package io.datarouter.metric.publisher;

import java.util.List;

import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.metric.config.DatarouterMetricSettingRoot;
import io.datarouter.storage.servertype.ServerTypeDetector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class MetricAnnotationPublisherService implements MetricAnnotationPublisher{

	@Inject
	private MetricAnnotationQueueDao metricAnnotationQueueDao;
	@Inject
	private MetricAnnotationNonProdQueueDao metricAnnotationNonProdQueueDao;
	@Inject
	private ServerTypeDetector detector;
	@Inject
	private DatarouterMetricSettingRoot settings;

	@Override
	public PublishingResponseDto publish(List<PublishedMetricAnnotationGroup> annotations){
		var dtos = annotations.stream().map(annotation -> new DatarouterMetricAnnotationGroupBinaryDto(
						annotation.environment(),
						annotation.serviceName(),
						annotation.name(),
						annotation.category(),
						annotation.description(),
						annotation.level().persistentString,
						annotation.timestamp().toEpochMilli(),
						annotation.serverName()))
				.toList();
		if(publishToSharedNonProdQueue()){
			metricAnnotationNonProdQueueDao.combineAndPut(dtos);
		}else{
			metricAnnotationQueueDao.combineAndPut(dtos);
		}
		return PublishingResponseDto.SUCCESS;
	}

	private Boolean publishToSharedNonProdQueue(){
		return !detector.mightBeProduction() && settings.publishNonProdDataToSharedQueue.get();
	}
}
