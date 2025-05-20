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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import io.datarouter.instrumentation.metric.MetricAnnotationCollector;
import io.datarouter.instrumentation.metric.MetricAnnotationLevel;
import io.datarouter.metric.config.DatarouterMetricSettingRoot;
import io.datarouter.metric.publisher.MetricAnnotationPublisher.PublishedMetricAnnotationGroup;
import io.datarouter.types.MilliTime;

public abstract class BaseDatarouterMetricAnnotationCollector implements MetricAnnotationCollector{

	private final ConcurrentLinkedQueue<PublishedMetricAnnotationGroup> annotations = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean stopped = new AtomicBoolean(false);

	private final DatarouterMetricSettingRoot metricSettingRoot;
	private final String environmentName;
	private final String serviceName;
	private final String serverName;

	public BaseDatarouterMetricAnnotationCollector(
			DatarouterMetricSettingRoot metricSettingRoot,
			String environmentName,
			String serviceName,
			String serverName){
		this.metricSettingRoot = metricSettingRoot;
		this.environmentName = environmentName;
		this.serviceName = serviceName;
		this.serverName = serverName;
	}

	@Override
	public void annotate(String name, String category, String description, MetricAnnotationLevel level, long timestamp){
		if(stopped.get()
				|| !metricSettingRoot.saveAnnotationsToMemory.get()){
			return;
		}
		annotations.add(
				new PublishedMetricAnnotationGroup(environmentName, serviceName, name, category, description, level,
						MilliTime.ofEpochMilli(timestamp), serverName));
	}

	public synchronized List<PublishedMetricAnnotationGroup> poll(){
		List<PublishedMetricAnnotationGroup> currAnnotations = new ArrayList<>(annotations);
		annotations.clear();
		return currAnnotations;
	}

	@Override
	public void stopAndFlushAll(){
		stopped.set(true);
	}
}
