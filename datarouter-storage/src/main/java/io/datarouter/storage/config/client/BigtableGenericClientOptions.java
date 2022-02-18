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
package io.datarouter.storage.config.client;

import io.datarouter.storage.client.ClientId;

public class BigtableGenericClientOptions{

	public final ClientId clientId;
	public final String projectId;
	public final String instanceId;
	public String credentialsFileLocation;
	public String credentialsSecretLocation;

	public BigtableGenericClientOptions(ClientId clientId, String projectId, String instanceId){
		this.clientId = clientId;
		this.projectId = projectId;
		this.instanceId = instanceId;
	}

	public BigtableGenericClientOptions withCredentialsFileLocation(String credentialsFileLocation){
		this.credentialsFileLocation = credentialsFileLocation;
		return this;
	}

	public BigtableGenericClientOptions withCredentialsSecretLocation(String credentialsSecretLocation){
		this.credentialsSecretLocation = credentialsSecretLocation;
		return this;
	}

}
