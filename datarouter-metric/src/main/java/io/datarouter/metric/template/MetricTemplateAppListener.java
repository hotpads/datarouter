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
package io.datarouter.metric.template;

import io.datarouter.instrumentation.metric.collector.MetricTemplates;
import io.datarouter.metric.config.DatarouterMetricTemplateSettingRoot;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.web.listener.DatarouterAppListener;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class MetricTemplateAppListener implements DatarouterAppListener{

	@Inject
	private ServiceName serviceName;
	@Inject
	private MetricTemplateBuffer buffer;
	@Inject
	private DatarouterMetricTemplateSettingRoot settings;

	@Override
	public void onStartUp(){
		var collector = new DatarouterMetricTemplateCollector(serviceName.get(), buffer, settings.saveToMemory);
		MetricTemplates.addCollector(collector);
	}

	@Override
	public void onShutDown(){
		MetricTemplates.stopAndFlushAll();
	}

}
