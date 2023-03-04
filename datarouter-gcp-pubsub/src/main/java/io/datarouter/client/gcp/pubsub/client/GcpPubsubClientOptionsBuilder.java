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
package io.datarouter.client.gcp.pubsub.client;

import java.util.Properties;

import io.datarouter.client.gcp.pubsub.GcpPubsubClientType;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.client.ClientOptionsBuilder;

public class GcpPubsubClientOptionsBuilder implements ClientOptionsBuilder{

	private final String clientIdName;
	private final Properties properties;

	public GcpPubsubClientOptionsBuilder(ClientId clientId){
		clientIdName = clientId.getName();
		properties = new Properties();
		properties.setProperty(ClientOptions.makeClientTypeKey(clientIdName), GcpPubsubClientType.NAME);
	}

	public GcpPubsubClientOptionsBuilder withProjectId(String projectId){
		String optionKey = makeKey(GcpPubsubOptions.PROP_projectId);
		properties.setProperty(optionKey, projectId);
		return this;
	}

	public GcpPubsubClientOptionsBuilder withCredentialsFileLocation(String accessKeyLocation){
		String optionKey = makeKey(GcpPubsubOptions.PROP_credentialsFileLocation);
		properties.setProperty(optionKey, accessKeyLocation);
		return this;
	}

	public GcpPubsubClientOptionsBuilder withCredentialsSecretLocation(String gcpPubsubCredentialsLocation){
		String optionKey = makeKey(GcpPubsubOptions.PROP_credentialsSecretLocation);
		properties.setProperty(optionKey, gcpPubsubCredentialsLocation);
		return this;
	}

	@Override
	public Properties build(){
		return properties;
	}

	private String makeKey(String suffix){
		return ClientOptions.makeClientPrefixedKey(clientIdName, suffix);
	}

}
