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
package io.datarouter.gcp.spanner.client;

import java.util.Properties;

import io.datarouter.gcp.spanner.SpannerClientType;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.client.ClientOptionsBuilder;

public class SpannerClientOptionsBuilder implements ClientOptionsBuilder{

	private final String clientIdName;
	private final Properties properties;

	public SpannerClientOptionsBuilder(ClientId clientId){
		clientIdName = clientId.getName();
		properties = new Properties();
		properties.setProperty(ClientOptions.makeClientTypeKey(clientIdName), SpannerClientType.NAME);
	}

	public SpannerClientOptionsBuilder withProjectId(String projectId){
		String optionKeySuffix = SpannerClientOptions.makeSpannerKey(SpannerClientOptions.PROP_projectId);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, projectId);
		return this;
	}

	public SpannerClientOptionsBuilder withInstanceId(String instanceId){
		String optionKeySuffix = SpannerClientOptions.makeSpannerKey(SpannerClientOptions.PROP_instanceId);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, instanceId);
		return this;
	}

	public SpannerClientOptionsBuilder withDatabaseName(String databaseName){
		String optionKeySuffix = SpannerClientOptions.makeSpannerKey(SpannerClientOptions.PROP_databaseName);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, databaseName);
		return this;
	}

	public SpannerClientOptionsBuilder withCredentialsFileLocation(String credentialsFileLocation){
		String optionKeySuffix = SpannerClientOptions.makeSpannerKey(SpannerClientOptions.PROP_credentialsFileLocation);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, credentialsFileLocation);
		return this;
	}

	public SpannerClientOptionsBuilder withCredentialsSecretLocation(String credentialsSecretLocation){
		String optionKeySuffix = SpannerClientOptions.makeSpannerKey(SpannerClientOptions
				.PROP_credentialsSecretLocation);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, credentialsSecretLocation);
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
