package com.hotpads.datarouter.client.imp.jdbc.ddl.execute;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlRowFormat;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlAlterTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlCreateTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp.ConnectionSqlTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp.FieldSqlTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.util.JdbcTool;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.node.op.raw.IndexedStorage;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.concurrent.Lazy;
import com.hotpads.util.core.profile.PhaseTimer;

public class SingleTableSchemaUpdate
implements Callable<Optional<String>>{
	private static final Logger logger = LoggerFactory.getLogger(SingleTableSchemaUpdate.class);

	private static final int CONSOLE_WIDTH = 80;

	private final FieldSqlTableGenerator fieldSqlTableGenerator;
	private final String clientName;
	private final JdbcConnectionPool connectionPool;
	private final Lazy<List<String>> existingTableNames;
	private final SchemaUpdateOptions printOptions;
	private final SchemaUpdateOptions executeOptions;
	private final PhysicalNode<?,?> physicalNode;

	public SingleTableSchemaUpdate(FieldSqlTableGenerator fieldSqlTableGenerator, String clientName,
			JdbcConnectionPool connectionPool, Lazy<List<String>> existingTableNames, SchemaUpdateOptions printOptions,
			SchemaUpdateOptions executeOptions, PhysicalNode<?,?> physicalNode){
		this.fieldSqlTableGenerator = fieldSqlTableGenerator;
		this.clientName = clientName;
		this.connectionPool = connectionPool;
		this.printOptions = printOptions;
		this.executeOptions = executeOptions;
		this.existingTableNames = existingTableNames;
		this.physicalNode = physicalNode;
	}

	@Override
	public Optional<String> call(){
		if(executeOptions.getIgnoreClients().contains(clientName) || !connectionPool.isWritable()){
			return Optional.empty();
		}

		String tableName = physicalNode.getTableName();
		String currentTableAbsoluteName = clientName + "." + tableName;
		if(executeOptions.getIgnoreTables().contains(currentTableAbsoluteName)){
			return Optional.empty();
		}

		String schemaName = connectionPool.getSchemaName();
		DatabeanFieldInfo<?, ?, ?> fieldInfo = physicalNode.getFieldInfo();
		List<Field<?>> primaryKeyFields = fieldInfo.getPrimaryKeyFields();
		List<Field<?>> nonKeyFields = fieldInfo.getNonKeyFields();
		Map<String, List<Field<?>>> indexes = fieldInfo.getIndexes();
		Map<String, List<Field<?>>> uniqueIndexes = fieldInfo.getUniqueIndexes();
		MySqlCollation collation = fieldInfo.getCollation();
		MySqlCharacterSet characterSet = fieldInfo.getCharacterSet();
		MySqlRowFormat rowFormat = fieldInfo.getRowFormat();

		if(physicalNode instanceof IndexedStorage){
			IndexedStorage<?,?> indexedStorage = (IndexedStorage<?,?>)physicalNode;
			for(ManagedNode<?,?,?,?,?> managedNode : indexedStorage.getManagedNodes()){
				indexes.put(managedNode.getName(), managedNode.getFieldInfo().getFields());
			}
		}

		SqlTable requested = fieldSqlTableGenerator.generate(tableName, primaryKeyFields,
				nonKeyFields, collation, characterSet, rowFormat, indexes, uniqueIndexes);
		Optional<String> printedSchemaUpdate = Optional.empty();
		boolean exists = existingTableNames.get().contains(tableName);
		if(!exists){
			String ddl = new SqlCreateTableGenerator(requested, schemaName).generateDdl();
			if(executeOptions.getCreateTables()){
				logger.info(generateFullWidthMessage("Creating the table " + tableName));
				logger.info(ddl);
				JdbcTool.execute(connectionPool, ddl);
				logger.info(generateFullWidthMessage("Created " + tableName));
			}else{
				logger.info(generateFullWidthMessage("Please Execute SchemaUpdate"));
				logger.info(ddl);
				printedSchemaUpdate = Optional.of(ddl);
			}
		}else{
			//execute the alter table
			SqlTable executeCurrent = ConnectionSqlTableGenerator.generate(connectionPool, tableName, schemaName);
			SqlAlterTableGenerator executeAlterTableGenerator = new SqlAlterTableGenerator(
					executeOptions, executeCurrent, requested, schemaName);
			Optional<String> ddl = executeAlterTableGenerator.generateDdl();
			if(ddl.isPresent()){
				PhaseTimer alterTableTimer = new PhaseTimer();
				logger.info(generateFullWidthMessage("Executing " + getClass().getSimpleName() + " SchemaUpdate"));
				logger.info(ddl.get());
				JdbcTool.execute(connectionPool, ddl.get());
				alterTableTimer.add("Completed SchemaUpdate for " + tableName);
				logger.info(generateFullWidthMessage(alterTableTimer.toString()));
			}

			//print the alter table
			SqlTable printCurrent = ConnectionSqlTableGenerator.generate(connectionPool, tableName, schemaName);
			SqlAlterTableGenerator printAlterTableGenerator = new SqlAlterTableGenerator(printOptions,
					printCurrent, requested, schemaName);
			ddl = printAlterTableGenerator.generateDdl();
			if(ddl.isPresent()){
				logger.info(generateFullWidthMessage("Please Execute SchemaUpdate"));
				printedSchemaUpdate = ddl;
				logger.info(ddl.get());
				logger.info(generateFullWidthMessage("Thank You"));
			}
		}
		return printedSchemaUpdate;
	}

	private static String generateFullWidthMessage(String message){
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
