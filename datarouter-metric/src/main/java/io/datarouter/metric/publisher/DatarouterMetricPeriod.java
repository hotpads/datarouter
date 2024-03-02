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

import java.time.Duration;

public enum DatarouterMetricPeriod{
	PERIOD_5s(Duration.ofSeconds(5)),
	PERIOD_20s(Duration.ofSeconds(20)),
	PERIOD_1m(Duration.ofMinutes(1)),
	PERIOD_5m(Duration.ofMinutes(5)),
	PERIOD_20m(Duration.ofMinutes(20)),
	PERIOD_1h(Duration.ofHours(1)),
	PERIOD_4h(Duration.ofHours(4)),
	PERIOD_1d(Duration.ofDays(1)),
	;

	private final Duration period;

	DatarouterMetricPeriod(Duration period){
		this.period = period;
	}

	public long getPeriodMs(){
		return period.toMillis();
	}

}
