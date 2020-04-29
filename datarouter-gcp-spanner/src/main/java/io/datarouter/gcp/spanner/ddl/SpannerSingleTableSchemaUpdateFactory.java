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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;
import com.google.spanner.admin.database.v1.UpdateDatabaseDdlMetadata;

import io.datarouter.gcp.spanner.connection.SpannerDatabaseClientsHolder;
import io.datarouter.gcp.spanner.field.SpannerBaseFieldCodec;
import io.datarouter.gcp.spanner.field.SpannerFieldCodecRegistry;
import io.datarouter.gcp.spanner.node.entity.SpannerSubEntityNode;
import io.datarouter.gcp.spanner.util.SpannerEntityKeyTool;
import io.datarouter.model.field.Field;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.storage.config.schema.SchemaUpdateResult;
import io.datarouter.storage.config.schema.SchemaUpdateTool;
import io.datarouter.storage.node.op.raw.IndexedStorage;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.trace.callable.TracedCallable;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.concurrent.FutureTool;
import io.datarouter.util.string.StringTool;

@Singleton
public class SpannerSingleTableSchemaUpdateFactory{
	private static final Logger logger = LoggerFactory.getLogger(SpannerSingleTableSchemaUpdateFactory.class);

	private static final SpannerColumn PARTITON_COLUMN = new SpannerColumn(
			SpannerSubEntityNode.PARTITION_COLUMN_NAME,
			SpannerColumnType.INT64,
			false);

	@Inject
	private SpannerDatabaseClientsHolder clientsHolder;
	@Inject
	private SpannerTableOperationsGenerator tableOperationsGenerator;
	@Inject
	private SpannerFieldCodecRegistry fieldCodecRegistry;
	@Inject
	private SpannerTableAlterSchemaService tableAlterSchemaService;
	@Inject
	private SchemaUpdateOptions updateOptions;

	public class SpannerSingleTableSchemaUpdate extends TracedCallable<Optional<SchemaUpdateResult>>{

		private final ClientId clientId;
		private final Supplier<List<String>> existingTableNames;
		private final PhysicalNode<?,?,?> physicalNode;

		public SpannerSingleTableSchemaUpdate(
				ClientId clientId,
				Supplier<List<String>> existingTableNames,
				PhysicalNode<?,?,?> physicalNode){
			//TODO give correct trace thread name
			super("");
			this.clientId = clientId;
			this.existingTableNames = existingTableNames;
			this.physicalNode = physicalNode;
		}

		@Override
		public Optional<SchemaUpdateResult> wrappedCall(){
			Database database = clientsHolder.getDatabase(clientId);
			String tableName;
			List<Field<?>> primaryKeyFields = SpannerEntityKeyTool.getPrimaryKeyFields(
					physicalNode.getFieldInfo().getSamplePrimaryKey(),
					physicalNode.getFieldInfo().isSubEntity());
			if(physicalNode.getFieldInfo().isSubEntity()){
				tableName = SpannerEntityKeyTool.getEntityTableName(physicalNode.getFieldInfo());
			}else{
				tableName = physicalNode.getFieldInfo().getTableName();
			}
			List<? extends SpannerBaseFieldCodec<?,?>> primaryKeyCodecs = fieldCodecRegistry.createCodecs(
					primaryKeyFields);
			for(SpannerBaseFieldCodec<?,?> codec : primaryKeyCodecs){
				if(codec.getSpannerColumnType().isArray()){
					throw new RuntimeException("Invalid field type used for primary key: " + codec.getField().getKey()
							.getName());
				}
			}
			List<SpannerIndex> indexes = new ArrayList<>();
			List<SpannerIndex> uniqueIndexes = physicalNode.getFieldInfo().getUniqueIndexes().entrySet().stream()
					.map(entry -> new SpannerIndex(
							tableName,
							entry.getKey(),
							entry.getValue(),
							Collections.emptyList(),
							true))
					.collect(Collectors.toList());
			var statements = new SpannerUpdateStatements();
			String entityTableName = null;
			if(physicalNode instanceof IndexedStorage){
				IndexedStorage<?,?> indexedStorage = (IndexedStorage<?,?>)physicalNode;
				indexes = indexedStorage.getManagedNodes().stream()
						.map(node -> new SpannerIndex(
								tableName,
								node.getName(),
								node.getIndexEntryFieldInfo().getPrimaryKeyFields(),
								node.getIndexEntryFieldInfo().getFields(),
								false))
						.collect(Collectors.toList());
			}
			List<SpannerColumn> primaryKeyColumns = primaryKeyCodecs.stream()
					.map(codec -> codec.getSpannerColumn(false))
					.collect(Collectors.toList());
			List<SpannerColumn> nonKeyColumns = fieldCodecRegistry.createCodecs(
					physicalNode.getFieldInfo().getNonKeyFields()).stream()
					.map(codec -> codec.getSpannerColumn(true))
					.collect(Collectors.toList());
			if(physicalNode.getFieldInfo().isSubEntity()){
				primaryKeyColumns.add(0, PARTITON_COLUMN);
				entityTableName = physicalNode.getFieldInfo().getTableName();
				if(!existingTableNames.get().contains(entityTableName)){
					List<? extends SpannerBaseFieldCodec<?,?>> entityKeyCodecs = fieldCodecRegistry.createCodecs(
							physicalNode.getFieldInfo().getEkPkFields());
					List<SpannerColumn> entityColumns = entityKeyCodecs.stream()
							.map(codec -> codec.getSpannerColumn(false))
							.collect(Collectors.toList());
					entityColumns.add(0, PARTITON_COLUMN);
					statements.updateFunction(
							tableOperationsGenerator.createTable(entityTableName, entityColumns, null, null),
							updateOptions::getCreateTables,
							true);
				}
			}
			if(!existingTableNames.get().contains(tableName)){
				statements.updateFunction(tableOperationsGenerator.createTable(tableName, primaryKeyColumns,
						nonKeyColumns, entityTableName),
						updateOptions::getCreateTables,
						true);
				indexes.stream()
						.map(index -> createIndex(index, primaryKeyColumns))
						.forEach(statement -> statements.updateFunction(statement, updateOptions::getAddIndexes, true));
				uniqueIndexes.stream()
						.map(index -> createIndex(index, primaryKeyColumns))
						.forEach(statement -> statements.updateFunction(statement, updateOptions::getAddIndexes, true));
			}else{
				DatabaseClient databaseClient = clientsHolder.getDatabaseClient(clientId);
				List<SpannerColumn> allColumns = ListTool.concatenate(primaryKeyColumns, nonKeyColumns);
				if(physicalNode.getFieldInfo().isSubEntity()){
					allColumns.add(0, PARTITON_COLUMN);
				}
				ResultSet columnRs = databaseClient.singleUse().executeQuery(Statement.of(tableOperationsGenerator
						.getTableSchema(tableName)));
				ResultSet primaryKeyRs = databaseClient.singleUse().executeQuery(Statement.of(tableOperationsGenerator
						.getTableIndexColumnsSchema(tableName, "PRIMARY_KEY")));
				tableAlterSchemaService.generateUpdateStatementColumns(tableName, allColumns, primaryKeyColumns,
						columnRs, primaryKeyRs, statements);
				ResultSet indexesRs = databaseClient.singleUse().executeQuery(Statement.of(tableOperationsGenerator
						.getTableIndexSchema(tableName)));
				Set<String> currentIndexes = tableAlterSchemaService.getIndexes(indexesRs);
				for(SpannerIndex index : ListTool.concatenate(indexes, uniqueIndexes)){
					ResultSet indexRs = databaseClient.singleUse().executeQuery(Statement.of(tableOperationsGenerator
							.getTableIndexColumnsSchema(tableName, index.getIndexName())));
					if(!tableAlterSchemaService.indexEqual(index, indexRs)){
						if(currentIndexes.contains(index.getIndexName())){
							statements.updateFunction(
									tableOperationsGenerator.dropIndex(index.getIndexName()),
									updateOptions::getDropIndexes,
									false);
						}
						statements.updateFunction(createIndex(index, primaryKeyColumns), updateOptions::getAddIndexes,
								true);
					}
					currentIndexes.remove(index.getIndexName());
				}
				currentIndexes.forEach(name -> statements.updateFunction(
						tableOperationsGenerator.dropIndex(name),
						updateOptions::getDropIndexes,
						false));
			}
			String errorMessage = null;
			if(!statements.getExcuteStatments().isEmpty()){
				logger.warn("Running the following spanner Schema updates: \n"
						+ String.join("\n\n", statements.getExcuteStatments()));
				OperationFuture<Void,UpdateDatabaseDdlMetadata> future = database.updateDdl(
						statements.getExcuteStatments(), null);
				errorMessage = FutureTool.get(future.getPollingFuture().getAttemptResult()).getErrorMessage();
				if(StringTool.notNullNorEmptyNorWhitespace(errorMessage)){
					logger.error(errorMessage);
				}
			}
			if(statements.getPreventStartUp()){
				errorMessage = "Need to alter the following spanner table: " + tableName;
			}
			if(statements.getPrintStatements().isEmpty()){
				return Optional.empty();
			}
			String printStatement = String.join("\n", statements.getPrintStatements());
			logger.info(SchemaUpdateTool.generateFullWidthMessage("Please Execute SchemaUpdate for Spanner"));
			logger.warn(printStatement);
			logger.info(SchemaUpdateTool.generateFullWidthMessage("Thank You"));
			return Optional.of(new SchemaUpdateResult(printStatement, errorMessage, clientId));
		}

		private String createIndex(SpannerIndex index, List<SpannerColumn> primaryKeyColumns){
			List<SpannerColumn> keyColumns = fieldCodecRegistry.createCodecs(index.getKeyFields()).stream()
					.map(codec -> codec.getSpannerColumn(false))
					.collect(Collectors.toList());
			if(index.getNonKeyFields().isEmpty()){
				return tableOperationsGenerator.createIndex(index.getTableName(), index.getIndexName(), keyColumns,
						Collections.emptyList(), index.isUnique());
			}
			// Spanner stores the primary key columns in the index automatically and will not create the index if
			// told to explicitly store a primary key column
			Set<String> primaryKeySet = primaryKeyColumns.stream()
					.map(SpannerColumn::getName)
					.collect(Collectors.toSet());
			List<SpannerColumn> nonKeyColumns = fieldCodecRegistry.createCodecs(index.getNonKeyFields()).stream()
					.map(codec -> codec.getSpannerColumn(false))
					.filter(col -> !primaryKeySet.contains(col.getName()))
					.collect(Collectors.toList());
			return tableOperationsGenerator.createIndex(
					index.getTableName(),
					index.getIndexName(),
					keyColumns,
					nonKeyColumns,
					index.isUnique());
		}

	}

}
