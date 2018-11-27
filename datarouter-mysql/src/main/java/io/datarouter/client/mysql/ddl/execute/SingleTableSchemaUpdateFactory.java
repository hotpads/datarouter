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
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.exceptions.MysqlErrorNumbers;

import io.datarouter.client.mysql.connection.MysqlConnectionPoolFactory.MysqlConnectionPool;
import io.datarouter.client.mysql.ddl.domain.MysqlCharacterSet;
import io.datarouter.client.mysql.ddl.domain.MysqlCollation;
import io.datarouter.client.mysql.ddl.domain.MysqlRowFormat;
import io.datarouter.client.mysql.ddl.domain.MysqlTableOptions;
import io.datarouter.client.mysql.ddl.domain.SqlTable;
import io.datarouter.client.mysql.ddl.generate.SqlAlterTableGeneratorFactory;
import io.datarouter.client.mysql.ddl.generate.SqlAlterTableGeneratorFactory.SqlAlterTableGenerator;
import io.datarouter.client.mysql.ddl.generate.SqlCreateTableGenerator;
import io.datarouter.client.mysql.ddl.generate.imp.ConnectionSqlTableGenerator;
import io.datarouter.client.mysql.ddl.generate.imp.FieldSqlTableGenerator;
import io.datarouter.client.mysql.util.MysqlTool;
import io.datarouter.model.field.Field;
import io.datarouter.storage.config.schema.SchemaUpdateOptions;
import io.datarouter.storage.node.op.raw.IndexedStorage;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.util.lazy.Lazy;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.util.tuple.Pair;

@Singleton
public class SingleTableSchemaUpdateFactory{
	private static final Logger logger = LoggerFactory.getLogger(SingleTableSchemaUpdateFactory.class);

	@Inject
	private FieldSqlTableGenerator fieldSqlTableGenerator;
	@Inject
	private SqlCreateTableGenerator sqlCreateTableGenerator;
	@Inject
	private SqlAlterTableGeneratorFactory sqlAlterTableGeneratorFactory;
	@Inject
	private SchemaUpdateOptions schemaUpdateOptions;

	public class SingleTableSchemaUpdate implements Callable<Optional<String>>{

		private static final int CONSOLE_WIDTH = 80;

		private final String clientName;
		private final MysqlConnectionPool connectionPool;
		private final Lazy<List<String>> existingTableNames;
		private final PhysicalNode<?,?,?> physicalNode;

		public SingleTableSchemaUpdate(String clientName, MysqlConnectionPool connectionPool,
				Lazy<List<String>> existingTableNames, PhysicalNode<?,?,?> physicalNode){
			this.clientName = clientName;
			this.connectionPool = connectionPool;
			this.existingTableNames = existingTableNames;
			this.physicalNode = physicalNode;
		}

		@Override
		public Optional<String> call(){
			if(schemaUpdateOptions.getIgnoreClients().contains(clientName) || !connectionPool.isWritable()){
				return Optional.empty();
			}

			String tableName = physicalNode.getFieldInfo().getTableName();
			String currentTableAbsoluteName = clientName + "." + tableName;
			if(schemaUpdateOptions.getIgnoreTables().contains(currentTableAbsoluteName)){
				return Optional.empty();
			}

			String schemaName = connectionPool.getSchemaName();
			DatabeanFieldInfo<?, ?, ?> fieldInfo = physicalNode.getFieldInfo();
			List<Field<?>> primaryKeyFields = fieldInfo.getPrimaryKeyFields();
			List<Field<?>> nonKeyFields = fieldInfo.getNonKeyFields();
			Map<String, List<Field<?>>> indexes = Collections.emptyMap();
			Map<String, List<Field<?>>> uniqueIndexes = fieldInfo.getUniqueIndexes();
			MysqlTableOptions mysqlTableOptions = MysqlTableOptions.make(fieldInfo);
			MysqlCollation collation = mysqlTableOptions.getCollation();
			MysqlCharacterSet characterSet = mysqlTableOptions.getCharacterSet();
			MysqlRowFormat rowFormat = mysqlTableOptions.getRowFormat();

			if(physicalNode instanceof IndexedStorage){
				IndexedStorage<?,?> indexedStorage = (IndexedStorage<?,?>)physicalNode;
				indexes = indexedStorage.getManagedNodes().stream()
						.collect(Collectors.toMap(ManagedNode::getName,
								managedNode -> managedNode.getFieldInfo().getFields()));
			}

			SqlTable requested = fieldSqlTableGenerator.generate(tableName, primaryKeyFields,
					nonKeyFields, collation, characterSet, rowFormat, indexes, uniqueIndexes);
			Optional<String> printedSchemaUpdate = Optional.empty();
			boolean exists = existingTableNames.get().contains(tableName);
			if(!exists){
				String ddl = sqlCreateTableGenerator.generateDdl(requested, schemaName);
				if(schemaUpdateOptions.getCreateTables(false)){
					logger.info(generateFullWidthMessage("Creating the table " + tableName));
					logger.info(ddl);
					try{
						MysqlTool.execute(connectionPool, ddl);
						logger.info(generateFullWidthMessage("Created " + tableName));
					}catch(RuntimeException e){
						Throwable cause = e.getCause();
						if(!(cause instanceof SQLSyntaxErrorException)){
							throw e;
						}
						int errorCode = ((SQLSyntaxErrorException)e.getCause()).getErrorCode();
						if(errorCode != MysqlErrorNumbers.ER_TABLE_EXISTS_ERROR){
							throw e;
						}
						logger.warn(generateFullWidthMessage("Did not create " + tableName
								+ " because it already exists"));
					}
				}else{
					logger.info(generateFullWidthMessage("Please Execute SchemaUpdate"));
					logger.info(ddl);
					printedSchemaUpdate = Optional.of(ddl);
				}
			}else{
				SqlTable executeCurrent = ConnectionSqlTableGenerator.generate(connectionPool, tableName, schemaName);
				SqlAlterTableGenerator executeAlterTableGenerator = sqlAlterTableGeneratorFactory
						.new SqlAlterTableGenerator(executeCurrent, requested, schemaName);
				//execute the alter table
				Pair<Optional<String>,Optional<String>> ddl = executeAlterTableGenerator.generateDdl();
				if(ddl.getLeft().isPresent()){
					PhaseTimer alterTableTimer = new PhaseTimer();
					logger.info(generateFullWidthMessage("Executing " + getClass().getSimpleName() + " SchemaUpdate"));
					logger.info(ddl.getLeft().get());
					MysqlTool.execute(connectionPool, ddl.getLeft().get());
					alterTableTimer.add("Completed SchemaUpdate for " + tableName);
					logger.info(generateFullWidthMessage(alterTableTimer.toString()));
				}

				//print the alter table
				if(ddl.getRight().isPresent()){
					logger.info(generateFullWidthMessage("Please Execute SchemaUpdate"));
					printedSchemaUpdate = ddl.getRight();
					logger.info(ddl.getRight().get());
					logger.info(generateFullWidthMessage("Thank You"));
				}
			}
			return printedSchemaUpdate;
		}

		private String generateFullWidthMessage(String message){
			StringBuilder fullWidthMessage = new StringBuilder();
			int numCharsOnSide = (CONSOLE_WIDTH - message.length()) / 2 - 1;
			if(numCharsOnSide <= 0){
				return message;
			}
			int chars;
			for(chars = 0; chars < numCharsOnSide; chars++){
				fullWidthMessage.append("=");
			}
			fullWidthMessage.append(" ").append(message).append(" ");
			chars += message.length();
			for(; chars < CONSOLE_WIDTH; chars++){
				fullWidthMessage.append("=");
			}
			return fullWidthMessage.toString();
		}
	}

}
