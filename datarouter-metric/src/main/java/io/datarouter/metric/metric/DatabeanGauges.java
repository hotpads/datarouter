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
package io.datarouter.metric.metric;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.httpclient.client.service.ServiceName;
import io.datarouter.instrumentation.gauge.GaugeDto;
import io.datarouter.metric.config.DatarouterGaugeSettingRoot;
import io.datarouter.metric.metric.conveyor.GaugeBuffers;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.metric.Gauges;
import io.datarouter.util.UlidTool;

@Singleton
public class DatabeanGauges implements Gauges{

	@Inject
	private ServiceName serviceName;
	@Inject
	private ServerName serverName;
	@Inject
	private GaugeBuffers buffers;
	@Inject
	private DatarouterGaugeSettingRoot settings;

	@Override
	public void save(String key, long value){
		if(!settings.saveGauges.get()){
			return;
		}
		GaugeDto dto = new GaugeDto(
				key,
				serviceName.get(),
				serverName.get(),
				UlidTool.nextUlid(),//pass the timestamp of the event.  periods are a server-side concern
				value);
		buffers.offer(dto);
	}

}
