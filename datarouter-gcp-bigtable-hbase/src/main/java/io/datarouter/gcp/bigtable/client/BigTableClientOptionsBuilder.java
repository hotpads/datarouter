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
package io.datarouter.gcp.bigtable.client;

import java.util.Properties;

import io.datarouter.client.hbase.client.HBaseClientOptionsBuilder;
import io.datarouter.gcp.bigtable.BigTableClientType;
import io.datarouter.storage.client.ClientId;

public class BigTableClientOptionsBuilder extends HBaseClientOptionsBuilder{

	public BigTableClientOptionsBuilder(ClientId clientId){
		super(clientId, BigTableClientType.NAME);
	}

	public BigTableClientOptionsBuilder withProjectId(String projectId){
		String optionKeySuffix = BigTableOptions.makeBigtableKey(BigTableOptions.PROP_projectId);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, projectId);
		return this;
	}

	public BigTableClientOptionsBuilder withInstanceId(String instanceId){
		String optionKeySuffix = BigTableOptions.makeBigtableKey(BigTableOptions.PROP_instanceId);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, instanceId);
		return this;
	}

	public BigTableClientOptionsBuilder withCredentialsFileLocation(String credentialsFileLocation){
		String optionKeySuffix = BigTableOptions.makeBigtableKey(BigTableOptions.PROP_credentialsFileLocation);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, credentialsFileLocation);
		return this;
	}

	public BigTableClientOptionsBuilder withCredentialsSecretLocation(String credentialsSecretLocation){
		String optionKeySuffix = BigTableOptions.makeBigtableKey(BigTableOptions.PROP_credentialsSecretLocation);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, credentialsSecretLocation);
		return this;
	}

	@Override
	public Properties build(){
		return properties;
	}

}
