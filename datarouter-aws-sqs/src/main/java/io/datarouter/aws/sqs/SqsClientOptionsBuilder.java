/**
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
package io.datarouter.aws.sqs;

import java.util.Properties;

import com.amazonaws.regions.Regions;

import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.client.ClientOptionsBuilder;

public class SqsClientOptionsBuilder implements ClientOptionsBuilder{

	private final String clientIdName;
	private final Properties properties;

	public SqsClientOptionsBuilder(ClientId clientId){
		clientIdName = clientId.getName();
		properties = new Properties();
		properties.setProperty(ClientOptions.makeClientTypeKey(clientIdName), SqsClientType.NAME);
	}

	public SqsClientOptionsBuilder withRegion(Regions region){
		String optionKey = makeKey(SqsOptions.PROP_region);
		properties.setProperty(optionKey, region.getName());
		return this;
	}

	public SqsClientOptionsBuilder withCredentialsLocation(String accessKeyLocation){
		String optionKey = makeKey(SqsOptions.PROP_credentialsLocation);
		properties.setProperty(optionKey, accessKeyLocation);
		return this;
	}

	public SqsClientOptionsBuilder withAccessKey(String accessKey){
		String optionKey = makeKey(SqsOptions.PROP_accessKey);
		properties.setProperty(optionKey, accessKey);
		return this;
	}

	public SqsClientOptionsBuilder withSecretKey(String secretKey){
		String optionKey = makeKey(SqsOptions.PROP_secretKey);
		properties.setProperty(optionKey, secretKey);
		return this;
	}

	public SqsClientOptionsBuilder withNamespace(String namespace){
		String optionKey = makeKey(SqsOptions.PROP_namespace);
		properties.setProperty(optionKey, namespace);
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
