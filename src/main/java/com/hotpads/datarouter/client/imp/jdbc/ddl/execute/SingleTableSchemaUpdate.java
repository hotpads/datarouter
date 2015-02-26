package com.hotpads.datarouter.client.imp.jdbc.ddl.execute;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlAlterTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.SqlCreateTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp.ConnectionSqlTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp.FieldSqlTableGenerator;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.util.core.MapTool;
import com.hotpads.util.core.profile.PhaseTimer;

public class SingleTableSchemaUpdate
implements Callable<Void>{

	protected String clientName;
	protected JdbcConnectionPool connectionPool;
	private String schemaName;
	private List<String> existingTableNames;
	protected SchemaUpdateOptions printOptions;
	protected SchemaUpdateOptions executeOptions;
	
	//we write back to these 2 thread-safe collections that are passed in
	protected Set<String> updatedTables;
	protected List<String> printedSchemaUpdates;
	
	private PhysicalNode<?,?> physicalNode;
	
	public SingleTableSchemaUpdate(String clientName, JdbcConnectionPool connectionPool,
			List<String> existingTableNames, SchemaUpdateOptions printOptions, SchemaUpdateOptions executeOptions,
			Set<String> updatedTables, List<String> printedSchemaUpdates, PhysicalNode<?,?> physicalNode){
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
//		logger.warn("createOrUpdateTableIfNeeded:"+physicalNode.getTableName());
		if( ! physicalNode.getFieldInfo().getFieldAware()){ return null; }

		//TODO don't forget to comment this condition when testing ManyFielTypeBeanIntegrationTest 
		//if(!ListTool.create("property", "drTestHibernate0","place","stat","test","view","config").contains(clientName)){ return; }
		//if(ListTool.create("event").contains(clientName)){ return; }
		// if(!schemaUpdateOptions.anyTrue()){ return; }
		
		String tableName = physicalNode.getTableName();
		DatabeanFieldInfo<?, ?, ?> fieldInfo = physicalNode.getFieldInfo();
		List<Field<?>> primaryKeyFields = fieldInfo.getPrimaryKeyFields();
		List<Field<?>> nonKeyFields = fieldInfo.getNonKeyFields();
		Map<String, List<Field<?>>>  indexes = MapTool.nullSafe(fieldInfo.getIndexes());
		MySqlCollation collation = fieldInfo.getCollation();
		MySqlCharacterSet character_set = fieldInfo.getCharacterSet();
		

		if(executeOptions.getIgnoreClients().contains(clientName)){ return null; }
		List<String> tablesToIgnore = executeOptions.getIgnoreTables();
		String currentTableAbsoluteName = clientName + "." + tableName;
		if(tablesToIgnore.contains(currentTableAbsoluteName)){ return null; }

		for(ManagedNode<?, ?, ?> managedNode : physicalNode.getManagedNodes()){
			indexes.put(managedNode.getName(), managedNode.getFieldInfo().getFields());
		}
		
		FieldSqlTableGenerator generator = new FieldSqlTableGenerator(physicalNode.getTableName(), primaryKeyFields, 
				nonKeyFields, collation, character_set);
		generator.setIndexes(indexes);

		SqlTable requested = generator.generate();
		Connection connection = null;
		try{
			if(!connectionPool.isWritable()){ return null; }
			if(updatedTables.contains(tableName)){ return null; }
			updatedTables.add(tableName);
			connection = connectionPool.getDataSource().getConnection();
			Statement statement = connection.createStatement();
			boolean exists = existingTableNames.contains(tableName);
			if(!exists){
				System.out.println("========================================== Creating the table " +tableName 
						+" ============================");
				String sql = new SqlCreateTableGenerator(requested, schemaName).generateDdl();
				if(!executeOptions.getCreateTables()){
					System.out.println("Please execute: "+sql);
				}
				else{
					System.out.println(sql);
					statement.execute(sql);
					System.out.println("============================================================================="
					+"=======================");
					
				}
			} else{
				/*if(!schemaUpdateOptions.anyAlterTrue()){
					return;
				}*/
				
				//execute the alter table
				ConnectionSqlTableGenerator executeConstructor = new ConnectionSqlTableGenerator(connection, tableName,
						schemaName);
				SqlTable executeCurrent = executeConstructor.generate();
				SqlAlterTableGenerator executeAlterTableGenerator = new SqlAlterTableGenerator(
						executeOptions, executeCurrent, requested, schemaName);
				if(executeAlterTableGenerator.willAlterTable()){
					String alterTableExecuteString = executeAlterTableGenerator.generateDdl();
					PhaseTimer alterTableTimer = new PhaseTimer();
					System.out.println("--------------- Executing "+getClass().getSimpleName()
							+" SchemaUpdate ---------------");
					System.out.println(alterTableExecuteString);
					//execute it
					statement.execute(alterTableExecuteString);
					alterTableTimer.add("Completed SchemaUpdate for "+tableName);
					System.out.println("----------------- "+alterTableTimer+" -------------------");
				}
				
				//print the alter table
				ConnectionSqlTableGenerator prinitConstructor = new ConnectionSqlTableGenerator(connection, tableName, 
						schemaName);
				SqlTable printCurrent = prinitConstructor.generate();
				SqlAlterTableGenerator printAlterTableGenerator = new SqlAlterTableGenerator(printOptions,
						printCurrent, requested, schemaName);
				if(printAlterTableGenerator.willAlterTable()){
					System.out.println("# ==================== Please Execute SchemaUpdate ==========================");
					//print it
					String alterTablePrintString = printAlterTableGenerator.generateDdl();
					printedSchemaUpdates.add(alterTablePrintString);
					System.out.println(alterTablePrintString);
					System.out.println("# ========================== Thank You ======================================");
				}		
			}
		} catch (Exception e){
			throw new RuntimeException(e);
		} finally{
			JdbcTool.closeConnection(connection);
		}
		return null;
	}
}
