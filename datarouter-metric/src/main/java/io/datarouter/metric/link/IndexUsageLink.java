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
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.web.handler.params.Params;

public class IndexUsageLink extends DatarouterLink{

	public static final String P_duration = "duration";

	public Optional<String> duration = Optional.empty();

	public IndexUsageLink(Params params){
		super(new DatarouterMetricPaths().datarouter.metric.indexUsage.view);
	}

	public IndexUsageLink withDuration(DatarouterDuration durationInDays){
		this.duration = Optional.of(durationInDays.toString(TimeUnit.DAYS));
		return this;
	}

}
