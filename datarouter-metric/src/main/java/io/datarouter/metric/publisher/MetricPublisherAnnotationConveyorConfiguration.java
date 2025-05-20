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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.Conveyor.ProcessResult;
import io.datarouter.conveyor.ConveyorConfiguration;
import io.datarouter.conveyor.ConveyorRunnable;
import io.datarouter.metric.publisher.MetricAnnotationPublisher.PublishedMetricAnnotationGroup;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class MetricPublisherAnnotationConveyorConfiguration implements ConveyorConfiguration{
	private static final Logger logger = LoggerFactory.getLogger(MetricPublisherAnnotationConveyorConfiguration.class);

	@Inject
	private MetricAnnotationPublisher publisher;

	@Override
	public ProcessResult process(ConveyorRunnable conveyor){
		List<PublishedMetricAnnotationGroup> annotations = DatarouterPublishedMetricAnnotationCollectors.poll();
		try{
			publisher.publish(annotations);
		}catch(RuntimeException e){
			logger.warn("", e);
		}
		return new ProcessResult(!annotations.isEmpty());
	}

	@Override
	public boolean shouldRunOnShutdown(){
		return true;
	}
}
