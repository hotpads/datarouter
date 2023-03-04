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
package io.datarouter.client.gcp.pubsub;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.gcp.pubsub.client.GcpPubsubClientManager;
import io.datarouter.client.gcp.pubsub.config.PubSubPluginValidator;
import io.datarouter.client.gcp.pubsub.web.GcpPubsubWebInspector;
import io.datarouter.storage.client.ClientType;
import io.datarouter.web.browse.DatarouterClientWebInspectorRegistry;

@Singleton
public class GcpPubsubClientType implements ClientType<GcpPubsubClientNodeFactory,GcpPubsubClientManager>{

	public static final String NAME = "gcpPubsub";

	@Inject
	public GcpPubsubClientType(
			DatarouterClientWebInspectorRegistry datarouterClientWebInspectorRegistry,
			@SuppressWarnings("unused")
			PubSubPluginValidator validator){
		datarouterClientWebInspectorRegistry.register(NAME, GcpPubsubWebInspector.class);
	}

	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public Class<GcpPubsubClientNodeFactory> getClientNodeFactoryClass(){
		return GcpPubsubClientNodeFactory.class;
	}

	@Override
	public Class<GcpPubsubClientManager> getClientManagerClass(){
		return GcpPubsubClientManager.class;
	}

}
