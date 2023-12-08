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
package io.datarouter.client.gcp.pubsub.config;

import io.datarouter.storage.config.environment.EnvironmentType;
import io.datarouter.storage.servertype.ServerTypes;
import io.datarouter.storage.setting.DatarouterSettingCategory;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.cached.CachedSetting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterGcpPubsubSettingsRoot extends SettingRoot{

	public final CachedSetting<Boolean> runGcpPubsubQueueMetricMonitoringJob;

	/*
	 * This allows pubsub to act more like SQS where a read of an empty queue will not block.
	 * If false, pubsub will block for tens of seconds waiting to see if a new message arrives in an empty queue.
	 * It's not clear why it's deprecated.
	 * Some datarouter code like joblets was written for SQS first, so we can use this to mimic SQS behavior.
	 */
	public final CachedSetting<Boolean> returnImmediately;

	@Inject
	public DatarouterGcpPubsubSettingsRoot(SettingFinder finder, ServerTypes serverTypes){
		super(finder, DatarouterSettingCategory.DATAROUTER, "gcpPubsub.");
		runGcpPubsubQueueMetricMonitoringJob = registerBooleans("runGcpPubsubQueueMetricMonitoringJob", defaultTo(false)
				.withServerType(EnvironmentType.PRODUCTION, serverTypes.getJobServerType(), true));
		returnImmediately = registerBooleans("returnImmediately", defaultTo(true));
	}

}
