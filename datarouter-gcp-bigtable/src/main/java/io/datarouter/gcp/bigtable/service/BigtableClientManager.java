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
package io.datarouter.gcp.bigtable.service;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.cloud.bigtable.admin.v2.BigtableInstanceAdminClient;
import com.google.cloud.bigtable.admin.v2.BigtableInstanceAdminSettings;
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient;
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminSettings;
import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.BigtableDataSettings;

import io.datarouter.gcp.bigtable.config.BigtableClientsHolder;
import io.datarouter.gcp.bigtable.config.BigtableOptions;
import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.storage.config.schema.SchemaUpdateResult;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class BigtableClientManager extends BaseClientManager{
	private static final Logger logger = LoggerFactory.getLogger(BigtableClientManager.class);

	@Inject
	private BigtableOptions bigTableClientOptions;
	@Inject
	private BigtableClientsHolder holder;
	@Inject
	private BigtableSchemaUpdateService schemaUpdateService;
	@Inject
	private SchemaUpdateOptions schemaUpdateOptions;

	@Override
	protected Future<Optional<SchemaUpdateResult>> doSchemaUpdate(PhysicalNode<?,?,?> node){
		if(schemaUpdateOptions.getEnabled()){
			return schemaUpdateService.queueNodeForSchemaUpdate(node.getFieldInfo().getClientId(), node);
		}
		return CompletableFuture.completedFuture(Optional.empty());
	}

	@Override
	public void shutdown(ClientId clientId){
		schemaUpdateService.gatherSchemaUpdates(true);
		holder.closeClient(clientId);
	}

	@Override
	protected void safeInitClient(ClientId clientId){
		try{
			BigtableDataSettings settings = BigtableDataSettings.newBuilder()
					.setProjectId(bigTableClientOptions.projectId(clientId.getName()))
					.setInstanceId(bigTableClientOptions.instanceId(clientId.getName()))
					.setCredentialsProvider(FixedCredentialsProvider.create(bigTableClientOptions.credentials(clientId
							.getName())))
					.build();
			BigtableInstanceAdminSettings adminSettings = BigtableInstanceAdminSettings.newBuilder()
					.setProjectId(bigTableClientOptions.projectId(clientId.getName()))
					.setCredentialsProvider(FixedCredentialsProvider.create(bigTableClientOptions.credentials(clientId
							.getName())))
					.build();
			BigtableTableAdminSettings tableSettings = BigtableTableAdminSettings.newBuilder()
					.setProjectId(bigTableClientOptions.projectId(clientId.getName()))
					.setInstanceId(bigTableClientOptions.instanceId(clientId.getName()))
					.setCredentialsProvider(FixedCredentialsProvider.create(bigTableClientOptions.credentials(clientId
							.getName())))
					.build();

			var client = BigtableDataClient.create(settings);
			var adminClient = BigtableInstanceAdminClient.create(adminSettings);
			var tableClient = BigtableTableAdminClient.create(tableSettings);
			holder.register(clientId, tableClient, adminClient, client);
		}catch(IOException e){
			logger.error("unable to create bigtable client", e);
		}
	}

	public BigtableTableAdminClient getTableAdminClient(ClientId clientId){
		initClient(clientId);
		return holder.getTableAdminClient(clientId);
	}

	public BigtableDataClient getTableDataClient(ClientId clientId){
		initClient(clientId);
		return holder.getDataClient(clientId);
	}

}
