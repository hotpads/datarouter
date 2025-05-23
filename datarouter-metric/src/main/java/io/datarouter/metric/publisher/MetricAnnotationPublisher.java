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

import io.datarouter.instrumentation.metric.MetricAnnotationLevel;
import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.types.MilliTime;

public interface MetricAnnotationPublisher{

	PublishingResponseDto publish(List<PublishedMetricAnnotationGroup> annotations);

	class NoOpMetricAnnotationPublisher implements MetricAnnotationPublisher{

		@Override
		public PublishingResponseDto publish(List<PublishedMetricAnnotationGroup> annotations){
			return PublishingResponseDto.NO_OP;
		}
	}

	record PublishedMetricAnnotationGroup(
			String environment,
			String serviceName,
			String name,
			String category,
			String description,
			MetricAnnotationLevel level,
			MilliTime timestamp,
			String serverName){
	}
}
