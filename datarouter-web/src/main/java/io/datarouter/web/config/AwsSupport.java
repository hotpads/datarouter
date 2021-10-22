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
package io.datarouter.web.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.ConnPoolControl;

import io.datarouter.inject.InstanceInventory;
import io.datarouter.inject.InstanceInventoryKey;
import io.datarouter.util.lang.ReflectionTool;

@Singleton
public class AwsSupport{

	public static final InstanceInventoryKey<ConnPoolControl<?>> AWS_CONNECTION_MANAGER = new InstanceInventoryKey<>();

	@Inject
	private InstanceInventory instanceInventory;

	public void registerConnectionManager(String clientName, Object awsClient){
		var client = ReflectionTool.get("client", awsClient);
		var httpClient = ReflectionTool.get("httpClient", client);
		var cm = (PoolingHttpClientConnectionManager)ReflectionTool.get("cm", httpClient);
		instanceInventory.add(AWS_CONNECTION_MANAGER, clientName, cm);
	}

}
