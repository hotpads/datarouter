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
package io.datarouter.client.mysql.ddl.execute;

import java.sql.SQLSyntaxErrorException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.exceptions.MysqlErrorNumbers;

import io.datarouter.client.mysql.connection.MysqlConnectionPoolHolder;
import io.datarouter.client.mysql.connection.MysqlConnectionPoolHolder.MysqlConnectionPool;
import io.datarouter.client.mysql.ddl.domain.MysqlCharacterSet;
import io.datarouter.client.mysql.ddl.domain.MysqlCollation;
import io.datarouter.client.mysql.ddl.domain.MysqlRowFormat;
import io.datarouter.client.mysql.ddl.domain.MysqlTableOptions;
import io.datarouter.client.mysql.ddl.domain.SqlTable;
import io.datarouter.client.mysql.ddl.generate.Ddl;
import io.datarouter.client.mysql.ddl.generate.SqlAlterTableGeneratorFactory;
import io.datarouter.client.mysql.ddl.generate.SqlAlterTableGeneratorFactory.SqlAlterTableGenerator;
import io.datarouter.client.mysql.ddl.generate.SqlCreateTableGenerator;
import io.datarouter.client.mysql.ddl.generate.imp.ConnectionSqlTableGenerator;
import io.datarouter.client.mysql.ddl.generate.imp.FieldSqlTableGenerator;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.model.field.Field;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.storage.config.schema.SchemaUpdateResult;
import io.datarouter.storage.config.schema.SchemaUpdateTool;
import io.datarouter.storage.node.op.raw.IndexedStorage;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.util.timer.PhaseTimer;

@Singleton
public class MysqlSingleTableSchemaUpdateService{
	private static final Logger logger = LoggerFactory.getLogger(MysqlSingleTableSchemaUpdateService.class);

	@Inject
	private FieldSqlTableGenerator fieldSqlTableGenerator;
	@Inject
	private SqlCreateTableGenerator sqlCreateTableGenerator;
	@Inject
	private SqlAlterTableGeneratorFactory sqlAlterTableGeneratorFactory;
	@Inject
	private SchemaUpdateOptions schemaUpdateOptions;
	@Inject
	private MysqlConnectionPoolHolder mysqlConnectionPoolHolder;

	public Optional<SchemaUpdateResult> performSchemaUpdate(
			ClientId clientId,
			Supplier<List<String>> existingTableNames,
			PhysicalNode<?,?,?> physicalNode){
		MysqlConnectionPool connectionPool = mysqlConnectionPoolHolder.getConnectionPool(clientId);
		if(schemaUpdateOptions.getIgnoreClients().contains(clientId.getName()) || !clientId.getWritable()){
			return Optional.empty();
		}

		String tableName = physicalNode.getFieldInfo().getTableName();
		String currentTableAbsoluteName = clientId.getName() + "." + tableName;
		if(schemaUpdateOptions.getIgnoreTables().contains(currentTableAbsoluteName)){
			return Optional.empty();
		}

		String schemaName = connectionPool.getSchemaName();
		DatabeanFieldInfo<?, ?, ?> fieldInfo = physicalNode.getFieldInfo();
		List<Field<?>> primaryKeyFields = fieldInfo.getPrimaryKeyFields();
		List<Field<?>> nonKeyFields = fieldInfo.getNonKeyFields();
		Map<String, List<Field<?>>> indexes = Collections.emptyMap();
		Map<String, List<Field<?>>> uniqueIndexes = fieldInfo.getUniqueIndexes();
		MysqlTableOptions mysqlTableOptions = MysqlTableOptions.make(fieldInfo.getSampleFielder());
		MysqlCollation collation = mysqlTableOptions.getCollation();
		MysqlCharacterSet characterSet = mysqlTableOptions.getCharacterSet();
		MysqlRowFormat rowFormat = mysqlTableOptions.getRowFormat();

		if(physicalNode instanceof IndexedStorage){
			IndexedStorage<?,?> indexedStorage = (IndexedStorage<?,?>)physicalNode;
			indexes = indexedStorage.getManagedNodes().stream()
					.collect(Collectors.toMap(ManagedNode::getName,
							managedNode -> managedNode.getIndexEntryFieldInfo().getFields()));
		}

		SqlTable requested = fieldSqlTableGenerator.generate(tableName, primaryKeyFields,
				nonKeyFields, collation, characterSet, rowFormat, indexes, uniqueIndexes);
		boolean exists = existingTableNames.get().contains(tableName);
		if(!exists){
			String createDdl = sqlCreateTableGenerator.generateDdl(requested, schemaName);
			if(schemaUpdateOptions.getCreateTables(false)){
				logger.info(SchemaUpdateTool.generateFullWidthMessage("Creating the table " + tableName));
				logger.info(createDdl);
				try{
					MysqlTool.execute(connectionPool, createDdl);
					logger.info(SchemaUpdateTool.generateFullWidthMessage("Created " + tableName));
				}catch(RuntimeException e){
					Throwable cause = e.getCause();
					if(!(cause instanceof SQLSyntaxErrorException)){
						throw e;
					}
					int errorCode = ((SQLSyntaxErrorException)e.getCause()).getErrorCode();
					if(errorCode != MysqlErrorNumbers.ER_TABLE_EXISTS_ERROR){
						throw e;
					}
					logger.warn(SchemaUpdateTool.generateFullWidthMessage("Did not create " + tableName
							+ " because it already exists"));
				}
				return Optional.empty();
			}
			logger.info(SchemaUpdateTool.generateFullWidthMessage("Please Execute SchemaUpdate"));
			logger.info(createDdl);
			return Optional.of(new SchemaUpdateResult(createDdl, tableName + " creation is required", clientId));
		}
		SqlTable executeCurrent = ConnectionSqlTableGenerator.generate(connectionPool, tableName, schemaName);
		SqlAlterTableGenerator executeAlterTableGenerator = sqlAlterTableGeneratorFactory
				.new SqlAlterTableGenerator(executeCurrent, requested, schemaName);
		//execute the alter table
		Ddl ddl = executeAlterTableGenerator.generateDdl();
		if(ddl.executeStatement.isPresent()){
			PhaseTimer alterTableTimer = new PhaseTimer();
			logger.info(SchemaUpdateTool.generateFullWidthMessage("Executing " + getClass().getSimpleName()
					+ " SchemaUpdate"));
			logger.info(ddl.executeStatement.get());
			MysqlTool.execute(connectionPool, ddl.executeStatement.get());
			alterTableTimer.add("Completed SchemaUpdate for " + tableName);
			logger.info(SchemaUpdateTool.generateFullWidthMessage(alterTableTimer.toString()));
		}


		if(ddl.printStatement.isEmpty()){
			return Optional.empty();
		}
		logger.info(SchemaUpdateTool.generateFullWidthMessage("Please Execute SchemaUpdate"));
		logger.info(ddl.printStatement.get());
		logger.info(SchemaUpdateTool.generateFullWidthMessage("Thank You"));

		String errorMessage = null;
		if(ddl.preventStartUp){
			errorMessage = "an alter on " + tableName + " is required";
		}
		return Optional.of(new SchemaUpdateResult(ddl.printStatement.get(), errorMessage, clientId));
	}

}
