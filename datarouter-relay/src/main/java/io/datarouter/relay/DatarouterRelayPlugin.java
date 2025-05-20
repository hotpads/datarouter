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
package io.datarouter.relay;

import io.datarouter.inject.guice.BaseGuiceModule;
import io.datarouter.relay.DatarouterRelayFinder.DefaultDatarouterRelayFinder;
import io.datarouter.relay.DatarouterRelaySender.NoOpDatarouterRelaySender;
import io.datarouter.relay.DatarouterRelayTopics.DefaultDatarouterRelayTopics;
import io.datarouter.storage.config.BaseStoragePlugin;

public class DatarouterRelayPlugin extends BaseStoragePlugin{

	private final Class<? extends DatarouterRelayFinder> finder;
	private final Class<? extends DatarouterRelaySender> sender;
	private final Class<? extends DatarouterRelayTopics> topics;

	public DatarouterRelayPlugin(
			Class<? extends DatarouterRelayFinder> finder,
			Class<? extends DatarouterRelaySender> sender,
			Class<? extends DatarouterRelayTopics> topics){
		this.finder = finder;
		this.sender = sender;
		this.topics = topics;
	}

	@Override
	protected void configure(){
		bindActual(DatarouterRelayFinder.class, finder);
		bindActual(DatarouterRelaySender.class, sender);
		bindActual(DatarouterRelayTopics.class, topics);
	}

	public static class DatarouterRelayPluginDefaults extends BaseGuiceModule{

		@Override
		public void configure(){
			bindDefault(DatarouterRelayFinder.class, DefaultDatarouterRelayFinder.class);
			bindDefault(DatarouterRelaySender.class, NoOpDatarouterRelaySender.class);
			bindDefault(DatarouterRelayTopics.class, DefaultDatarouterRelayTopics.class);
		}

	}

}
