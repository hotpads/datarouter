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
package io.datarouter.gcp.spanner.config;

import java.util.List;
import java.util.function.Function;

import io.datarouter.util.string.StringTool;
import io.datarouter.web.plugins.opencencus.metrics.OpencencusMetricsAppListener.OpencencusMetricsDto;
import io.datarouter.web.plugins.opencencus.metrics.OpencencusMetricsMapper;
import jakarta.inject.Singleton;

@Singleton
public class GcpSpannerOpencencusMetrics implements OpencencusMetricsMapper{

	private static final String PREFIX = "cloud.google.com/java/spanner/";

	@Override
	public List<Function<OpencencusMetricsDto,String>> getMappers(){
		return List.of(
				metric -> mapSpanner(PREFIX + "max_in_use_sessions", metric),
				metric -> mapSpanner(PREFIX + "max_allowed_sessions", metric),
				metric -> mapSpanner(PREFIX + "get_session_timeouts", metric),
				metric -> mapSpanner(PREFIX + "num_acquired_sessions", metric),
				metric -> mapSpanner(PREFIX + "num_released_sessions", metric),
				metric -> mapSpanner(PREFIX + "num_sessions_in_pool", metric));
	}

	private String mapSpanner(String ocName, OpencencusMetricsDto metric){
		if(!metric.name().equals(ocName)){
			return null;
		}
		String name = metric.name().substring(PREFIX.length());
		name = StringTool.snakeCaseToCamelCase(name);
		String database = metric.labels().get(1);
		if(metric.labels().size() > 4){
			name += " " + StringTool.snakeCaseToCamelCase(metric.labels().get(4));
		}
		return "Datarouter client spanner " + database + " " + name;
	}

}
