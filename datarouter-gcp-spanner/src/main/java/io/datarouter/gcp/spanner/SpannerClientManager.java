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
package io.datarouter.gcp.spanner;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auth.Credentials;
import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;

import io.datarouter.gcp.spanner.client.SpannerClientOptions;
import io.datarouter.gcp.spanner.connection.SpannerDatabaseClientsHolder;
import io.datarouter.gcp.spanner.ddl.SpannerDatabaseCreator;
import io.datarouter.gcp.spanner.execute.SpannerSchemaUpdateService;
import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.storage.config.schema.SchemaUpdateResult;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.timer.PhaseTimer;

@Singleton
public class SpannerClientManager extends BaseClientManager{
	private static final Logger logger = LoggerFactory.getLogger(SpannerClientManager.class);

	@Inject
	private SpannerClientOptions spannerClientOptions;
	@Inject
	private SpannerDatabaseClientsHolder databaseClientsHolder;
	@Inject
	private SchemaUpdateOptions schemaUpdateOptions;
	@Inject
	private SpannerSchemaUpdateService schemaUpdateService;
	@Inject
	private SpannerDatabaseCreator databaseCreator;

	@Override
	protected Future<Optional<SchemaUpdateResult>> doSchemaUpdate(PhysicalNode<?,?,?> node){
		if(schemaUpdateOptions.getEnabled()){
			return schemaUpdateService.queueNodeForSchemaUpdate(node.getClientId(), node);
		}
		return CompletableFuture.completedFuture(Optional.empty());
	}

	@Override
	public void gatherSchemaUpdates(){
		schemaUpdateService.gatherSchemaUpdates(true);
	}

	@Override
	protected void safeInitClient(ClientId clientId){
		PhaseTimer timer = new PhaseTimer(clientId.getName());
		Credentials credentials = spannerClientOptions.credentials(clientId.getName());
		timer.add("read credentials");
		SpannerOptions options = SpannerOptions.newBuilder().setCredentials(credentials).build();
		Spanner spanner = options.getService();
		timer.add("build spanner service");
		DatabaseId databaseId = DatabaseId.of(
				spannerClientOptions.projectId(clientId.getName()),
				spannerClientOptions.instanceId(clientId.getName()),
				spannerClientOptions.databaseName(clientId.getName()));
		Database database = databaseCreator.createDatabaseIfNeeded(databaseId, spanner);
		timer.add("create database");
		databaseClientsHolder.register(clientId, spanner.getDatabaseClient(databaseId), spanner, database);
		logger.warn(timer.toString());
	}

	@Override
	public void shutdown(ClientId clientId){
		schemaUpdateService.gatherSchemaUpdates(true);
		databaseClientsHolder.close(clientId);
	}

	public DatabaseClient getDatabaseClient(ClientId clientId){
		initClient(clientId);
		return databaseClientsHolder.getDatabaseClient(clientId);
	}

}
