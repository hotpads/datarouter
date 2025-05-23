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
package io.datarouter.metric.config;

import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.metric.publisher.DatarouterMetricAnnotationCollector;
import io.datarouter.metric.publisher.DatarouterMetricCollector;
import io.datarouter.metric.publisher.DatarouterPublishedMetricAnnotationCollectors;
import io.datarouter.metric.publisher.DatarouterPublishedMetricCollectors;
import io.datarouter.web.listener.DatarouterAppListener;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterMetricAppListener implements DatarouterAppListener{

	@Inject
	private DatarouterMetricCollector collector;
	@Inject
	private DatarouterMetricAnnotationCollector annotationCollector;

	@Override
	public void onStartUp(){
		Metrics.registerCollector(collector);
		Metrics.registerCollector(annotationCollector);
		DatarouterPublishedMetricCollectors.register(collector);
		DatarouterPublishedMetricAnnotationCollectors.register(annotationCollector);
	}

	@Override
	public void onShutDown(){
		collector.stopAndFlushAll();
		annotationCollector.stopAndFlushAll();
	}

	@Override
	public boolean safeToExecuteInParallel(){
		return false;
	}

}
