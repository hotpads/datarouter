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

import java.util.HashMap;
import java.util.Map;

import io.datarouter.client.gcp.pubsub.config.PubSubPluginValidator.DefaultPubSubPluginValidator;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginConfigValue;
import io.datarouter.web.config.BaseWebPlugin;

public class DatarouterGcpPubsubPlugin extends BaseWebPlugin{

	public DatarouterGcpPubsubPlugin(DatarouterGcpPubsubPluginBuilder builder){
		addSettingRoot(DatarouterGcpPubsubSettingsRoot.class);
		addPluginEntry(BaseTriggerGroup.KEY, DatarouterGcpPubsubTriggerGroup.class);
		addRouteSet(DatarouterGcpPubsubRouteSet.class);
		addPluginEntry(builder.getSharedQueueNameRegistry());
	}

	@Override
	protected void configure(){
		bind(PubSubPluginValidator.class).to(DefaultPubSubPluginValidator.class);
	}

	public static class DatarouterGcpPubsubPluginBuilder{

		private final Map<String,String> queueOwnerByQueueName = new HashMap<>();

		public DatarouterGcpPubsubPluginBuilder addSharedQueueNames(Map<String,String> queueOwnerByQueueName){
			this.queueOwnerByQueueName.putAll(queueOwnerByQueueName);
			return this;
		}

		public SharedQueueNameRegistry getSharedQueueNameRegistry(){
			return new SharedQueueNameRegistry(queueOwnerByQueueName);
		}

		public DatarouterGcpPubsubPlugin build(){
			return new DatarouterGcpPubsubPlugin(this);
		}

	}

	public static class SharedQueueNameRegistry implements PluginConfigValue<SharedQueueNameRegistry>{

		public static final PluginConfigKey<SharedQueueNameRegistry> KEY = new PluginConfigKey<>(
				"sharedQueueNameRegistry",
				PluginConfigType.INSTANCE_SINGLE);

		public final Map<String,String> queueOwnerByQueueName;

		public SharedQueueNameRegistry(Map<String,String> queueOwnerByQueueName){
			this.queueOwnerByQueueName = queueOwnerByQueueName;
		}

		@Override
		public PluginConfigKey<SharedQueueNameRegistry> getKey(){
			return KEY;
		}
	}

}
