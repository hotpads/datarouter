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
package io.datarouter.gcp.spanner.ddl;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.DatabaseNotFoundException;
import com.google.cloud.spanner.ErrorCode;
import com.google.cloud.spanner.ReadOnlyTransaction;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerException;
import com.google.cloud.spanner.Statement;

import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.storage.config.schema.SchemaUpdateTool;
import io.datarouter.util.timer.PhaseTimer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SpannerDatabaseCreator{
	private static final Logger logger = LoggerFactory.getLogger(SpannerDatabaseCreator.class);

	@Inject
	private SchemaUpdateOptions schemaUpdateOptions;

	public void createIfMissing(Spanner spanner, DatabaseId databaseId, PhaseTimer timer){
		//Try to check existence without the Admin API first since it has rate limiting
		boolean hasTables = hasTables(spanner, databaseId);
		timer.add("listTables");
		if(hasTables){
			String message = String.format("Database %s exists with tables", databaseId.getDatabase());
			logger.info(SchemaUpdateTool.generateFullWidthMessage(message));
			return;
		}
		//Fall back to the more expensive Admin API if no tables found
		boolean exists = exists(spanner, databaseId);
		timer.add("getDatabase");
		if(exists){
			String message = String.format("Database %s exists without tables", databaseId.getDatabase());
			logger.info(SchemaUpdateTool.generateFullWidthMessage(message));
			return;
		}
		if(!schemaUpdateOptions.getCreateDatabases(false)){
			String format = "Database %s not found, and auto-creation not enabled in schemaUpdateOptions";
			throw new RuntimeException(String.format(format, databaseId.getDatabase()));
		}
		String message = String.format("Creating spanner database %s", databaseId.getDatabase());
		logger.info(SchemaUpdateTool.generateFullWidthMessage(message));
		create(spanner, databaseId);
		timer.add("createDatabase");
	}

	private boolean hasTables(Spanner spanner, DatabaseId databaseId){
		//use a throw-away client as it won't be able to see the database after it's created
		DatabaseClient databaseClient = spanner.getDatabaseClient(databaseId);
		Statement anyOperation = Statement.of(SpannerTableOperationsTool.getListOfTables());
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
		try{
			databaseAdminClient.createDatabase(
					databaseId.getInstanceId().getInstance(),
					databaseId.getDatabase(),
					List.of())
					.get();
		}catch(InterruptedException e){
			throw new RuntimeException(e);
		}catch(ExecutionException e){
			if(e.getCause() instanceof SpannerException ex && ex.getErrorCode() == ErrorCode.ALREADY_EXISTS){
				return;
			}
			throw new RuntimeException(e);
		}
	}

}
