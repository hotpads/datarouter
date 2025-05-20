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
package io.datarouter.metric.link;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.datarouter.httpclient.endpoint.link.DatarouterLink;
import io.datarouter.metric.config.DatarouterMetricPaths;
import io.datarouter.metric.dashboard.web.HandlerUsageHandler.GroupBy;
import io.datarouter.util.duration.DatarouterDuration;

public class HandlerUsageLink extends DatarouterLink{

	public static final String
			P_duration = "duration",
			P_groupBy = "groupBy",
			P_excludeIrregularUsage = "excludeIrregularUsage";

	public Optional<String> duration = Optional.empty();
	public Optional<GroupBy> groupBy = Optional.empty();
	public Optional<Boolean> excludeIrregularUsage = Optional.empty();

	public HandlerUsageLink(){
		super(new DatarouterMetricPaths().datarouter.metric.handlerUsage.view);
	}

	public HandlerUsageLink withDuration(DatarouterDuration durationInDays){
		this.duration = Optional.of(durationInDays.toString(TimeUnit.DAYS));
		return this;
	}

	public HandlerUsageLink withGroupBy(GroupBy groupBy){
		this.groupBy = Optional.of(groupBy);
		return this;
	}

	public HandlerUsageLink withExcludeIrregularUsage(boolean excludeIrregularUsage){
		this.excludeIrregularUsage = Optional.of(excludeIrregularUsage);
		return this;
	}

}
