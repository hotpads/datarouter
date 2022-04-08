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
package io.datarouter.web.plugins.opencencus.metrics;

import java.util.List;
import java.util.function.Function;

import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginConfigValue;
import io.datarouter.web.plugins.opencencus.metrics.OpencencusMetricsAppListener.OpencencusMetricsDto;

public interface OpencencusMetricsMapper extends PluginConfigValue<OpencencusMetricsMapper>{

	PluginConfigKey<OpencencusMetricsMapper> KEY = new PluginConfigKey<>(
			"opencencusMetricsMapper",
			PluginConfigType.CLASS_LIST);

	@Override
	default PluginConfigKey<OpencencusMetricsMapper> getKey(){
		return KEY;
	}

	List<Function<OpencencusMetricsDto,String>> getMappers();

}
