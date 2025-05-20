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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.ErrorCode;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.SpannerException;
import com.google.cloud.spanner.Statement;
import com.google.spanner.admin.database.v1.UpdateDatabaseDdlMetadata;

import io.datarouter.bytes.KvString;
import io.datarouter.gcp.spanner.connection.SpannerDatabaseClientsHolder;
import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecs;
import io.datarouter.model.field.Field;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.schema.InvalidSchemaUpdateException;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.storage.config.schema.SchemaUpdateResult;
import io.datarouter.storage.config.schema.SchemaUpdateTool;
import io.datarouter.storage.node.op.raw.IndexedStorage;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.concurrent.ThreadTool;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.string.StringTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SpannerSingleTableSchemaUpdateService{
	private static final Logger logger = LoggerFactory.getLogger(SpannerSingleTableSchemaUpdateService.class);

	@Inject
	private SpannerDatabaseClientsHolder clientsHolder;
	@Inject
	private SpannerFieldCodecs fieldCodecs;
	@Inject
	private SpannerTableAlterSchemaService tableAlterSchemaService;
	@Inject
	private SchemaUpdateOptions updateOptions;

	public Optional<SchemaUpdateResult> performSchemaUpdate(
			ClientId clientId,
			Supplier<List<String>> existingTableNames,
			PhysicalNode<?,?,?> physicalNode){
		String tableName = physicalNode.getFieldInfo().getTableName();
		List<Field<?>> primaryKeyFields = physicalNode.getFieldInfo().getSamplePrimaryKey().getFields();
		List<? extends SpannerBaseFieldCodec<?,?>> primaryKeyCodecs = fieldCodecs.createCodecs(primaryKeyFields);
		Scanner.of(primaryKeyCodecs)
				.include(codec -> codec.getSpannerColumnType().isArray())
				.findFirst()
				.ifPresent(codec -> {
					String message = String.format(
							"Invalid field type used for primary key: %s",
							codec.getField().getKey().getName());
					throw new RuntimeException(message);
				});
		List<SpannerIndex> indexes = new ArrayList<>();
		List<SpannerIndex> uniqueIndexes = Scanner.of(physicalNode.getFieldInfo().getUniqueIndexes().entrySet())
				.map(entry -> new SpannerIndex(
						tableName,
						entry.getKey(),
						entry.getValue(),
						entry.getValue(),
						true))
				.list();
		var statements = new SpannerUpdateStatements();
		String entityTableName = null;
		if(physicalNode instanceof IndexedStorage<?,?> indexedStorage){
			indexes = Scanner.of(indexedStorage.getManagedNodes())
					.map(node -> new SpannerIndex(
							tableName,
							node.getName(),
							node.getIndexEntryFieldInfo().getPrimaryKeyFields(),
							node.getIndexEntryFieldInfo().getFields(),
							false))
					.list();
		}
		List<SpannerColumn> primaryKeyColumns = Scanner.of(primaryKeyCodecs)
				.map(codec -> codec.getSpannerColumn(false))
				.list();
		List<Field<?>> nonKeyFields = physicalNode.getFieldInfo().getNonKeyFields();
		List<? extends SpannerBaseFieldCodec<?,?>> nonKeyCodecs = fieldCodecs.createCodecs(nonKeyFields);
		List<SpannerColumn> nonKeyColumns = Scanner.of(nonKeyCodecs)
				.map(codec -> codec.getSpannerColumn(true))
				.list();
		if(!existingTableNames.get().contains(tableName)){
			statements.updateFunction(
					SpannerTableOperationsTool.createTable(tableName, primaryKeyColumns, nonKeyColumns,
							entityTableName),
					updateOptions::getCreateTables,
					true);
			Scanner.of(indexes, uniqueIndexes)
					.concat(Scanner::of)
					.map(index -> createIndex(index, primaryKeyColumns))
					.forEach(statement -> statements.updateFunction(
							statement,
							updateOptions::getCreateTables,
							true));
		}else{
			DatabaseClient databaseClient = clientsHolder.getDatabaseClient(clientId);
			List<SpannerColumn> allColumns = Scanner.concat(primaryKeyColumns, nonKeyColumns)
					.list();
			var columnStatement = Statement.of(SpannerTableOperationsTool.getTableSchema(tableName));
			ResultSet columnRs = databaseClient.singleUse().executeQuery(columnStatement);
			var primaryKeyStatement = Statement.of(SpannerTableOperationsTool.getTableIndexColumnsSchema(tableName,
					"PRIMARY_KEY"));
			ResultSet primaryKeyRs = databaseClient.singleUse().executeQuery(primaryKeyStatement);
			tableAlterSchemaService.generateUpdateStatementColumns(tableName, allColumns, primaryKeyColumns,
					columnRs, primaryKeyRs, statements);
			var indexesStatement = Statement.of(SpannerTableOperationsTool.getTableIndexSchema(tableName));
			ResultSet indexesRs = databaseClient.singleUse().executeQuery(indexesStatement);
			Set<String> currentIndexNames = tableAlterSchemaService.getIndexes(indexesRs);

			// drop unused indexes
			Set<String> proposedIndexNames = Scanner.concat(indexes, uniqueIndexes)
					.map(SpannerIndex::indexName)
					.collect(HashSet::new);
			Scanner.of(currentIndexNames)
					.exclude(proposedIndexNames::contains)
					.forEach(droppedIndex -> statements.updateFunction(
							SpannerTableOperationsTool.dropIndex(droppedIndex),
							updateOptions::getDropIndexes,
							false));

			// add new indexes
			Scanner.concat(indexes, uniqueIndexes)
					.exclude(index -> currentIndexNames.contains(index.indexName()))
					.forEach(index -> statements.updateFunction(
							createIndex(index, primaryKeyColumns),
							updateOptions::getAddIndexes,
							true));

			// confirm no indexes updated
			Scanner.concat(indexes, uniqueIndexes)
					.include(index -> currentIndexNames.contains(index.indexName()))
					.concatOpt(proposedIndex -> {
						Statement tableIndexColumnsSchema = Statement.of(SpannerTableOperationsTool
								.getTableIndexColumnsSchema(tableName, proposedIndex.indexName()));
						ResultSet existingIndexRs = databaseClient.singleUse().executeQuery(tableIndexColumnsSchema);
						return tableAlterSchemaService.computeChanges(primaryKeyColumns, proposedIndex,
								existingIndexRs);
					})
					.flush(indexesUpdatedInPlace -> {
						if(!indexesUpdatedInPlace.isEmpty()){
							throw new InvalidSchemaUpdateException(
									"Indexes cannot be updated in-place. Instead, rename the index. "
											+ new KvString()
											.add("TableName", tableName)
											.add("changedIndexes", indexesUpdatedInPlace.toString()));
						}
					});
		}
		String errorMessage = null;
		if(!statements.getExecuteStatements().isEmpty()){
			logger.info(SchemaUpdateTool.generateFullWidthMessage("Executing Spanner " + getClass().getSimpleName()
					+ " SchemaUpdate"));
			logger.info(String.join("\n\n", statements.getExecuteStatements()));
			long startMs = System.currentTimeMillis();
			Database database = clientsHolder.getDatabase(clientId);
			OperationFuture<Void,UpdateDatabaseDdlMetadata> future = database.updateDdl(
					statements.getExecuteStatements(),
					null);
			RetryingFuture<OperationSnapshot> pollingFuture = future.getPollingFuture();
			OperationSnapshot opSnapshot = null;
			do{
				try{
					opSnapshot = pollingFuture.getAttemptResult().get();
				}catch(InterruptedException | ExecutionException e){
					throw new RuntimeException(e);
				}catch(SpannerException e){
					if(e.getErrorCode() != ErrorCode.RESOURCE_EXHAUSTED){
						throw new RuntimeException(e);
					}
					ThreadTool.trySleep(3_000);
				}
			}while(opSnapshot == null || !opSnapshot.isDone());
			logger.info(SchemaUpdateTool.generateFullWidthMessage("Done " + DatarouterDuration.ageMs(startMs)));

			errorMessage = opSnapshot.getErrorMessage();
			if(StringTool.notNullNorEmptyNorWhitespace(errorMessage)){
				logger.error(errorMessage);
			}
		}
		if(statements.getPreventStartUp()){
			errorMessage = "an alter on Spanner table " + tableName + " is required";
		}
		if(statements.getPrintStatements().isEmpty()){
			return Optional.empty();
		}
		String printStatement = statements.getPrintStatements().stream()
				.map(statement -> statement + ";")
				.collect(Collectors.joining("\n"));

		SchemaUpdateTool.printSchemaUpdate(logger,
				clientId.getName(),
				clientsHolder.getDatabase(clientId).getId().getDatabase(),
				tableName,
				printStatement);
		return Optional.of(new SchemaUpdateResult(printStatement, errorMessage, clientId));
	}

	private String createIndex(SpannerIndex index, List<SpannerColumn> primaryKeyColumns){
		List<SpannerColumn> keyColumns = Scanner.of(fieldCodecs.createCodecs(index.keyFields()))
				.map(codec -> codec.getSpannerColumn(false))
				.list();
		if(index.getNonKeyFields().isEmpty()){
			return SpannerTableOperationsTool.createIndex(
					index.tableName(),
					index.indexName(),
					keyColumns,
					List.of(),
					index.isUnique());
		}
		// Spanner stores the primary key columns in the index automatically and will not create the index if
		// told to explicitly store a primary key column
		Set<String> primaryKeySet = Scanner.of(primaryKeyColumns)
				.map(SpannerColumn::getName)
				.collect(HashSet::new);
		List<SpannerColumn> nonKeyColumns = Scanner.of(fieldCodecs.createCodecs(index.getNonKeyFields()))
				.map(codec -> codec.getSpannerColumn(false))
				.exclude(col -> primaryKeySet.contains(col.getName()))
				.list();
		return SpannerTableOperationsTool.createIndex(
				index.tableName(),
				index.indexName(),
				keyColumns,
				nonKeyColumns,
				index.isUnique());
	}

}
