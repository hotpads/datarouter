/**
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
package io.datarouter.metric.dto;

import java.util.Objects;

import io.datarouter.metric.types.MetricNameType;
import io.datarouter.metric.types.MetricType;
import io.datarouter.pathnode.PathNode;

public class MetricName{

	public final String displayName;
	private final String nameOrPrefix;
	public final MetricNameType nameType;
	public final MetricType metricType;

	private MetricName(String displayName, PathNode nameOrPrefix, MetricNameType nameType, MetricType metricType){
		this(displayName, nameOrPrefix.join("", " ", ""), nameType, metricType);
	}

	/**
	 * @param displayName string to show on the UI
	 * @param nameOrPrefix if nameType is AVAILABLE give the metric prefix, if nameType is EXACT give the exact metric
	 *        name
	 * @param nameType available metric or an exact metric name
	 * @param metricType should be null if nameType is MetricNameType.AVAILABLE
	 */
	private MetricName(String displayName, String nameOrPrefix, MetricNameType nameType, MetricType metricType){
		this.displayName = displayName;
		this.nameOrPrefix = nameOrPrefix;
		this.nameType = nameType;
		this.metricType = metricType;
	}

	public String getNameOrPrefix(){
		if(nameType == MetricNameType.AVAILABLE){
			return nameOrPrefix + ".*";
		}
		return nameOrPrefix;
	}

	public static MetricName exactMetric(String displayName, String metricName, MetricType metricType){
		Objects.requireNonNull(metricType);
		return new MetricName(displayName, metricName, MetricNameType.EXACT, metricType);
	}

	public static MetricName exactMetric(String displayName, PathNode metricName, MetricType metricType){
		Objects.requireNonNull(metricType);
		return new MetricName(displayName, metricName, MetricNameType.EXACT, metricType);
	}

	public static MetricName availableMetric(String displayName, String prefix){
		return new MetricName(displayName, prefix, MetricNameType.AVAILABLE, null);
	}

	public static MetricName availableMetric(String displayName, PathNode prefix){
		return new MetricName(displayName, prefix, MetricNameType.AVAILABLE, null);
	}

}
