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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.paging.Page;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Options;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.spanner.admin.database.v1.CreateDatabaseMetadata;

import io.datarouter.gcp.spanner.client.SpannerClientOptions;
import io.datarouter.gcp.spanner.connection.SpannerDatabaseClientsHolder;
import io.datarouter.gcp.spanner.execute.SpannerSchemaUpdateService;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.storage.config.schema.SchemaUpdateResult;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.concurrent.FutureTool;
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

	@Override
	protected Future<Optional<SchemaUpdateResult>> doSchemaUpdate(PhysicalNode<?,?,?> node){
		if(schemaUpdateOptions.getEnabled()){
			return schemaUpdateService.queueNodeForSchemaUpdate(node.getClientId(), node);
		}
		return CompletableFuture.completedFuture(Optional.empty());
	}

	@Override
	protected void safeInitClient(ClientId clientId){
		PhaseTimer timer = new PhaseTimer(clientId.getName());
		String credentialsLocation = spannerClientOptions.credentialsLocation(clientId.getName());
		Credentials credentials;
		try{
			credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsLocation));
		}catch(IOException ex){
			throw new RuntimeException("Cannot find google credentials file: " + credentialsLocation, ex);
		}
		timer.add("read credentials");
		SpannerOptions options = SpannerOptions.newBuilder().setCredentials(credentials).build();
		Spanner spanner = options.getService();
		timer.add("build spanner service");
		DatabaseId databaseId = DatabaseId.of(
				spannerClientOptions.projectId(clientId.getName()),
				spannerClientOptions.instanceId(clientId.getName()),
				spannerClientOptions.databaseName(clientId.getName()));
		Page<Database> page = spanner.getDatabaseAdminClient().listDatabases(
				databaseId.getInstanceId().getInstance(),
				Options.pageSize(1));
		Database database = null;
		while(page != null){
			Database current = Scanner.of(page.getValues()).findFirst().orElse(null);
			if(current == null || current.getId().equals(databaseId)){
				database = current;
				break;
			}
			page = page.getNextPage();
		}
		timer.add("search database");
		if(database == null){
			if(schemaUpdateOptions.getCreateDatabases(false)){
				OperationFuture<Database,CreateDatabaseMetadata> op = spanner.getDatabaseAdminClient().createDatabase(
						databaseId.getInstanceId().getInstance(),
						databaseId.getDatabase(),
						Collections.emptyList());
				database = FutureTool.get(op);
				timer.add("create database");
			}else{
				throw new RuntimeException("Must create database before executing updates for database=" + databaseId
						.getDatabase());
			}
		}
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
