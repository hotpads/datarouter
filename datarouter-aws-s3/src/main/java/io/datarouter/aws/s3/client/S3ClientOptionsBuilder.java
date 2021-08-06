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
package io.datarouter.aws.s3.client;

import java.util.Properties;

import io.datarouter.aws.s3.S3ClientType;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.client.ClientOptionsBuilder;

public class S3ClientOptionsBuilder implements ClientOptionsBuilder{

	private final String clientName;
	private final Properties properties;

	public S3ClientOptionsBuilder(ClientId clientId){
		clientName = clientId.getName();
		properties = new Properties();
		properties.setProperty(ClientOptions.makeClientTypeKey(clientName), S3ClientType.NAME);
	}

	public S3ClientOptionsBuilder withCredentialsLocation(String accessKeyLocation){
		String optionKey = makeKey(S3Options.PROP_credentialsLocation);
		properties.setProperty(optionKey, accessKeyLocation);
		return this;
	}

	public S3ClientOptionsBuilder withAccessKey(String accessKey){
		String optionKey = makeKey(S3Options.PROP_accessKey);
		properties.setProperty(optionKey, accessKey);
		return this;
	}

	public S3ClientOptionsBuilder withSecretKey(String secretKey){
		String optionKey = makeKey(S3Options.PROP_secretKey);
		properties.setProperty(optionKey, secretKey);
		return this;
	}

	@Override
	public Properties build(){
		return properties;
	}

	private String makeKey(String suffix){
		return ClientOptions.makeClientPrefixedKey(clientName, suffix);
	}

}
