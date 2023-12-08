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
package io.datarouter.gcp.bigtable.client;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.bigtable.hbase.BigtableConfiguration;

import io.datarouter.client.hbase.HBaseClientManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class BigTableClientManager extends HBaseClientManager{
	private static final Logger logger = LoggerFactory.getLogger(BigTableClientManager.class);

	@Inject
	private BigTableOptions bigTableOptions;

	@Override
	public boolean monitorLatency(){
		return false;
	}

	@Override
	protected Connection makeConnection(String clientName){
		String projectId = bigTableOptions.projectId(clientName);
		String instanceId = bigTableOptions.instanceId(clientName);
		Configuration config = BigtableConfiguration.configure(projectId, instanceId);
		BigTableCredentials credentials = bigTableOptions.bigtableConfigurationCredentialsKeyValue(clientName);
		logger.warn("connecting to bigtable projectId={} instanceId={} credsType={}",
				projectId, instanceId, credentials.key());
		config.set(credentials.key(), credentials.value());
		return BigtableConfiguration.connect(config);
	}

}
