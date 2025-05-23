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
package io.datarouter.instrumentation.metric;

import io.datarouter.instrumentation.metric.collector.MetricTemplateDto;
import io.datarouter.instrumentation.metric.collector.MetricTemplates;

public abstract class MetricRecorder{

	public abstract String toMetricName();
	public abstract MetricTemplateDto makePatternDto(String description);

	public final void count(String description){
		count(1, description);
	}

	public final void count(){
		count(1);
	}

	public final void count(long delta){
		count(delta, null);
	}

	public final void count(long delta, String description){
		Metrics.count(toMetricName(), delta);
		MetricTemplates.add(makePatternDto(description));
	}

	public final void measure(long value){
		measure(value, null);
	}

	public final void measure(long value, String description){
		Metrics.measure(toMetricName(), value);
		MetricTemplates.add(makePatternDto(description));
	}

	public final void measureWithPercentiles(long value){
		measureWithPercentiles(value, null);
	}

	public final void measureWithPercentiles(long value, String description){
		Metrics.measureWithPercentiles(toMetricName(), value);
		MetricTemplates.add(makePatternDto(description));
	}

}
