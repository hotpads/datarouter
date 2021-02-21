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
package io.datarouter.web.metriclinks;

import java.util.Optional;

public class MetricLinkDto{

	public final String name;
	public final Optional<String> exactMetricName;
	public final Optional<String> availableMetricPrefix;

	public MetricLinkDto(String name, Optional<String> exactMetricName, Optional<String> availableMetricPrefix){
		this.name = name;
		this.exactMetricName = exactMetricName;
		this.availableMetricPrefix = availableMetricPrefix;
	}

}
