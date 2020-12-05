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
package io.datarouter.client.memcached.client.options;

import java.util.Properties;

import io.datarouter.client.memcached.MemcachedClientType;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.client.ClientOptionsBuilder;

public class MemcachedClientOptionsBuilder implements ClientOptionsBuilder{

	protected final String clientIdName;
	protected final Properties properties;

	public MemcachedClientOptionsBuilder(ClientId clientId){
		this(clientId, MemcachedClientType.NAME);
	}

	protected MemcachedClientOptionsBuilder(ClientId clientId, String clientTypeName){
		clientIdName = clientId.getName();
		properties = new Properties();
		properties.setProperty(ClientOptions.makeClientTypeKey(clientIdName), clientTypeName);
	}

	public MemcachedClientOptionsBuilder withNumServers(int numberOfServers){
		String optionKeySuffix = MemcachedOptions.makeMemcachedKey(MemcachedOptions.PROP_numServers);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, String.valueOf(numberOfServers));
		return this;
	}

	public MemcachedClientOptionsBuilder withServerIndexAndInetSocketAddress(int serverIndex, String inetSocketAddress){
		String optionKeySuffix = MemcachedOptions.makeMemcachedKey(MemcachedOptions.PROP_server + "." + serverIndex);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, inetSocketAddress);
		return this;
	}

	@Override
	public Properties build(){
		return properties;
	}

	protected String makeKey(String suffix){
		return ClientOptions.makeClientPrefixedKey(clientIdName, suffix);
	}

}
