package com.hotpads.datarouter.client.imp.jdbc.ddl.execute;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlRowFormat;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlAlterTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlCreateTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp.ConnectionSqlTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp.FieldSqlTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.node.op.raw.IndexedStorage;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.util.core.DrMapTool;
import com.hotpads.util.core.profile.PhaseTimer;

public class SingleTableSchemaUpdate
implements Callable<Void>{
	private static final Logger logger = LoggerFactory.getLogger(SingleTableSchemaUpdate.class);

	private final JdbcFieldCodecFactory fieldCodecFactory;
	private final String clientName;
	private final JdbcConnectionPool connectionPool;
	private final String schemaName;
	private final List<String> existingTableNames;
	private final SchemaUpdateOptions printOptions;
	private final SchemaUpdateOptions executeOptions;
	private final PhysicalNode<?,?> physicalNode;

	//we write back to these 2 thread-safe collections that are passed in
	private final Set<String> updatedTables;
	private final List<String> printedSchemaUpdates;


	public SingleTableSchemaUpdate(JdbcFieldCodecFactory fieldCodecFactory, String clientName,
			JdbcConnectionPool connectionPool, List<String> existingTableNames, SchemaUpdateOptions printOptions,
			SchemaUpdateOptions executeOptions, Set<String> updatedTables, List<String> printedSchemaUpdates,
			PhysicalNode<?,?> physicalNode){
		this.fieldCodecFactory = fieldCodecFactory;
		this.clientName = clientName;
		this.connectionPool = connectionPool;
		this.schemaName = connectionPool.getSchemaName();
		this.printOptions = printOptions;
		this.executeOptions = executeOptions;
		this.updatedTables = updatedTables;
		this.printedSchemaUpdates = printedSchemaUpdates;
		this.existingTableNames = existingTableNames;
		this.physicalNode = physicalNode;
	}

	@Override
	public Void call(){
		if( ! physicalNode.getFieldInfo().getFieldAware()){
			return null;
		}

		String tableName = physicalNode.getTableName();
		DatabeanFieldInfo<?, ?, ?> fieldInfo = physicalNode.getFieldInfo();
		List<Field<?>> primaryKeyFields = fieldInfo.getPrimaryKeyFields();
		List<Field<?>> nonKeyFields = fieldInfo.getNonKeyFields();
		Map<String, List<Field<?>>> indexes = DrMapTool.nullSafe(fieldInfo.getIndexes());
		Map<String, List<Field<?>>> uniqueIndexes = DrMapTool.nullSafe(fieldInfo.getUniqueIndexes());
		MySqlCollation collation = fieldInfo.getCollation();
		MySqlCharacterSet characterSet = fieldInfo.getCharacterSet();
		MySqlRowFormat rowFormat = fieldInfo.getRowFormat();


		if(executeOptions.getIgnoreClients().contains(clientName)){
			return null;
		}
		List<String> tablesToIgnore = executeOptions.getIgnoreTables();
		String currentTableAbsoluteName = clientName + "." + tableName;
		if(tablesToIgnore.contains(currentTableAbsoluteName)){
			return null;
		}

		if(physicalNode instanceof IndexedStorage){
			IndexedStorage<?,?> indexedStorage = (IndexedStorage<?,?>)physicalNode;
			for(ManagedNode<?,?,?,?,?> managedNode : indexedStorage.getManagedNodes()){
				indexes.put(managedNode.getName(), managedNode.getFieldInfo().getFields());
			}
		}

		FieldSqlTableGenerator generator = new FieldSqlTableGenerator(fieldCodecFactory, physicalNode.getTableName(),
				primaryKeyFields, nonKeyFields, collation, characterSet, rowFormat);
		generator.setIndexes(indexes);
		generator.setUniqueIndexes(uniqueIndexes);

		SqlTable requested = generator.generate();
		Connection connection = null;
		String ddl = null;
		try{
			if(!connectionPool.isWritable()){
				return null;
			}
			if(updatedTables.contains(tableName)){
				return null;
			}
			updatedTables.add(tableName);
			connection = connectionPool.checkOut();
			Statement statement = connection.createStatement();
			boolean exists = existingTableNames.contains(tableName);
			if(!exists){
				ddl = new SqlCreateTableGenerator(requested, schemaName).generateDdl();
				if(executeOptions.getCreateTables()){
					logger.info("========================================== Creating the table " +tableName
							+" ============================");
					logger.info(ddl);
					statement.execute(ddl);
					logger.info("============================================================================="
					+"=======================");
				}else{
					logger.info("========================================== Please Execute SchemaUpdate"
							+" ============================");
					logger.info(ddl);
					printedSchemaUpdates.add(ddl);
				}
			} else{
				//execute the alter table
				ConnectionSqlTableGenerator executeConstructor = new ConnectionSqlTableGenerator(connection, tableName,
						schemaName);
				SqlTable executeCurrent = executeConstructor.generate();
				SqlAlterTableGenerator executeAlterTableGenerator = new SqlAlterTableGenerator(
						executeOptions, executeCurrent, requested, schemaName);
				if(executeAlterTableGenerator.willAlterTable()){
					ddl = executeAlterTableGenerator.generateDdl();
					PhaseTimer alterTableTimer = new PhaseTimer();
					logger.info("--------------- Executing "+getClass().getSimpleName()
							+" SchemaUpdate ---------------");
					logger.info(ddl);
					//execute it
					statement.execute(ddl);
					alterTableTimer.add("Completed SchemaUpdate for "+tableName);
					logger.info("----------------- "+alterTableTimer+" -------------------");
				}

				//print the alter table
				ConnectionSqlTableGenerator prinitConstructor = new ConnectionSqlTableGenerator(connection, tableName,
						schemaName);
				SqlTable printCurrent = prinitConstructor.generate();
				SqlAlterTableGenerator printAlterTableGenerator = new SqlAlterTableGenerator(printOptions,
						printCurrent, requested, schemaName);
				if(printAlterTableGenerator.willAlterTable()){
					logger.info("# ==================== Please Execute SchemaUpdate ==========================");
					//print it
					String alterTablePrintString = printAlterTableGenerator.generateDdl();
					printedSchemaUpdates.add(alterTablePrintString);
					logger.info(alterTablePrintString);
					logger.info("# ========================== Thank You ======================================");
				}
			}
		} catch (Exception e){
			logger.error("error on {}", ddl, e);
			throw new RuntimeException(e);
		} finally{
			connectionPool.checkIn(connection);
		}
		return null;
	}
}
