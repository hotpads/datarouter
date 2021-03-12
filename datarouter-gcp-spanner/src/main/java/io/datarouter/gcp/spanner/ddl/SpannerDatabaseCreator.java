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
package io.datarouter.gcp.spanner.ddl;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.DatabaseNotFoundException;
import com.google.cloud.spanner.ReadOnlyTransaction;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.Statement;
import com.google.spanner.admin.database.v1.CreateDatabaseMetadata;

import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.storage.config.schema.SchemaUpdateTool;
import io.datarouter.util.concurrent.FutureTool;

@Singleton
public class SpannerDatabaseCreator{
	private static final Logger logger = LoggerFactory.getLogger(SpannerDatabaseCreator.class);

	@Inject
	private SpannerTableOperationsGenerator tableOperationsGenerator;
	@Inject
	private SchemaUpdateOptions schemaUpdateOptions;

	public void createIfMissing(Spanner spanner, DatabaseId databaseId){
		//Try to check existence without the Admin API first since it has rate limiting
		if(hasTables(spanner, databaseId)){
			String message = String.format("Database %s exists with tables", databaseId.getDatabase());
			logger.info(SchemaUpdateTool.generateFullWidthMessage(message));
			return;
		}
		//Fall back to the more expensive Admin API if no tables found
		if(exists(spanner, databaseId)){
			String message = String.format("Database %s exists without tables", databaseId.getDatabase());
			logger.info(SchemaUpdateTool.generateFullWidthMessage(message));
			return;
		}
		if(schemaUpdateOptions.getCreateDatabases(false)){
			String message = String.format("Creating spanner database %s", databaseId.getDatabase());
			logger.info(SchemaUpdateTool.generateFullWidthMessage(message));
			create(spanner, databaseId);
		}else{
			String format = "Database %s not found, and auto-creation not enabled in schemaUpdateOptions";
			throw new RuntimeException(String.format(format, databaseId.getDatabase()));
		}
	}

	private boolean hasTables(Spanner spanner, DatabaseId databaseId){
		//use a throw-away client as it won't be able to see the database after it's created
		DatabaseClient databaseClient = spanner.getDatabaseClient(databaseId);
		Statement anyOperation = Statement.of(tableOperationsGenerator.getListOfTables());
		try{
			//this sometimes throws DatabaseNotFoundException, but it seems not always, so check if there's a table
			ReadOnlyTransaction txn = databaseClient.singleUseReadOnlyTransaction();
			try(ResultSet resultSet = txn.executeQuery(anyOperation)){
				return resultSet.next();
			}
		}catch(DatabaseNotFoundException dnfe){
			return false;
		}
	}

	private boolean exists(Spanner spanner, DatabaseId databaseId){
		DatabaseAdminClient databaseAdminClient = spanner.getDatabaseAdminClient();
		try{
			databaseAdminClient.getDatabase(
					databaseId.getInstanceId().getInstance(),
					databaseId.getDatabase());
			return true;
		}catch(DatabaseNotFoundException dnfe){
			return false;
		}
	}

	private void create(Spanner spanner, DatabaseId databaseId){
		DatabaseAdminClient databaseAdminClient = spanner.getDatabaseAdminClient();
		OperationFuture<Database,CreateDatabaseMetadata> op = databaseAdminClient.createDatabase(
				databaseId.getInstanceId().getInstance(),
				databaseId.getDatabase(),
				Collections.emptyList());
		FutureTool.get(op);
	}

}