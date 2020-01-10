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
package io.datarouter.client.hbase.client;

import java.util.Properties;

import io.datarouter.client.hbase.HBaseClientType;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.client.ClientOptionsBuilder;

public class HBaseClientOptionsBuilder implements ClientOptionsBuilder{

	protected final String clientIdName;
	protected final Properties properties;

	public HBaseClientOptionsBuilder(ClientId clientId){
		this(clientId, HBaseClientType.NAME);
	}

	protected HBaseClientOptionsBuilder(ClientId clientId, String clientTypeName){
		clientIdName = clientId.getName();
		properties = new Properties();
		properties.setProperty(ClientOptions.makeClientTypeKey(clientIdName), clientTypeName);
	}

	public HBaseClientOptionsBuilder withZookeeperQuorum(String zookeeperQuorum){
		String optionKeySuffix = HBaseOptions.makeHbaseKey(HBaseOptions.PROP_zookeeperQuorum);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, zookeeperQuorum);
		return this;
	}

	public HBaseClientOptionsBuilder withMaxHTables(String maxHTables){
		String optionKeySuffix = HBaseOptions.makeHbaseKey(HBaseOptions.PROP_maxHTables);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, maxHTables);
		return this;
	}

	public HBaseClientOptionsBuilder withMinThreadsPerHTable(String minThreadsPerHTable){
		String optionKeySuffix = HBaseOptions.makeHbaseKey(HBaseOptions.PROP_minThreadsPerHTable);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, minThreadsPerHTable);
		return this;
	}

	public HBaseClientOptionsBuilder withMaxThreadsPerHTable(String maxThreadsPerHTable){
		String optionKeySuffix = HBaseOptions.makeHbaseKey(HBaseOptions.PROP_maxThreadsPerHTable);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, maxThreadsPerHTable);
		return this;
	}

	public HBaseClientOptionsBuilder withMinPoolSize(int minPoolSize){
		String optionKeySuffix = HBaseOptions.makeHbaseKey(HBaseOptions.PROP_minPoolSize);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, String.valueOf(minPoolSize));
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
