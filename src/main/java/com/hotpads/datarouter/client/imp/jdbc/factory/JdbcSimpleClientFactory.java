package com.hotpads.datarouter.client.imp.jdbc.factory;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.JdbcClientImp;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlAlterTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlCreateTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCharacterSet;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlCollation;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.SqlTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp.ConnectionSqlTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.generate.imp.FieldSqlTableGenerator;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.node.Nodes;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.util.DataRouterEmailTool;
import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.profile.PhaseTimer;


public class JdbcSimpleClientFactory 
implements ClientFactory{
	private static Logger logger = Logger.getLogger(JdbcSimpleClientFactory.class);

	public static Boolean SCHEMA_UPDATE = false;

	public static final String 
			SERVER_NAME = "server.name",
			ADMINISTRATOR_EMAIL = "administrator.email",
			schemaUpdatePrintPrefix = "schemaUpdate.print",
			schemaUpdateExecutePrefix = "schemaUpdate.execute";

	public static final String 
			paramConfigLocation = ".configLocation";

	protected DataRouterContext drContext;
	protected String clientName;
	protected Set<String> configFilePaths;
	protected List<Properties> multiProperties;
	protected SchemaUpdateOptions schemaUpdatePrintOptions;
	protected SchemaUpdateOptions schemaUpdateExecuteOptions;
	protected Set<String> updatedTables;
	protected List<String> printedSchemaUpdates;	
	

	public JdbcSimpleClientFactory(DataRouterContext drContext, String clientName){
		this.drContext = drContext;
		this.clientName = clientName;

		this.configFilePaths = drContext.getConfigFilePaths();
		this.multiProperties = PropertiesTool.fromFiles(configFilePaths);
		this.schemaUpdatePrintOptions = new SchemaUpdateOptions(multiProperties, schemaUpdatePrintPrefix, true	);
		this.schemaUpdateExecuteOptions = new SchemaUpdateOptions(multiProperties, schemaUpdateExecutePrefix, false);
		this.updatedTables = SetTool.createTreeSet();
		this.printedSchemaUpdates = ListTool.createArrayList();
		SCHEMA_UPDATE = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, "schemaUpdate.enable"));
	}
		
	@Override
	public Client call(){
		PhaseTimer timer = new PhaseTimer(clientName);
		
		JdbcConnectionPool connectionPool = getConnectionPool(clientName, multiProperties);
		timer.add("gotPool");

		// datarouter fieldAware databeans (register after connecting to db)
		Connection connection = null;
		try{
			connection = connectionPool.checkOut();
			List<String> tableNames = JdbcTool.showTables(connection);
			Nodes<?,?,?> nodes = drContext.getNodes();
			List<? extends PhysicalNode<?, ?>> physicalNodes = nodes.getPhysicalNodesForClient(clientName);
			for(PhysicalNode<?, ?> physicalNode : IterableTool.nullSafe(physicalNodes)){
//				String tableName = physicalNode.getTableName();
				// logger.warn(clientName+":"+tableName);
				DatabeanFieldInfo<?, ?, ?> fieldInfo = physicalNode.getFieldInfo();
				if(SCHEMA_UPDATE && fieldInfo.getFieldAware()){
					createOrUpdateTableIfNeeded(tableNames, connectionPool, physicalNode);
				}
			}
		} finally{
			connectionPool.checkIn(connection);// is this how you return it to the pool?
		}
		
		sendSchemaUpdateEmail();
		timer.add("schema update");
		
		JdbcClientImp client = new JdbcClientImp(clientName, connectionPool);
		timer.add("client");
		
		logger.warn(timer);
		return client;
	}

	protected JdbcConnectionPool getConnectionPool(String clientName, List<Properties> multiProperties){
		boolean writable = ClientId.getWritableNames(drContext.getClientPool().getClientIds()).contains(clientName);
		JdbcConnectionPool connectionPool = new JdbcConnectionPool(clientName, multiProperties, writable);
		return connectionPool;
	}

	protected void createOrUpdateTableIfNeeded(List<String> tableNames, JdbcConnectionPool connectionPool, 
			PhysicalNode<?, ?> physicalNode){
//		logger.warn("createOrUpdateTableIfNeeded:"+physicalNode.getTableName());
		if( ! physicalNode.getFieldInfo().getFieldAware()){ return; }

		if(!SCHEMA_UPDATE){
			return;
		}
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
		

		if(schemaUpdateExecuteOptions.getIgnoreClients().contains(clientName)){ return; }
		List<String> tablesToIgnore = schemaUpdateExecuteOptions.getIgnoreTables();
		String currentTableAbsoluteName = clientName + "." + tableName;
		if(tablesToIgnore.contains(currentTableAbsoluteName)){ return; }

		FieldSqlTableGenerator generator = new FieldSqlTableGenerator(physicalNode.getTableName(), primaryKeyFields, 
				nonKeyFields, collation, character_set);
		generator.setIndexes(indexes);

		SqlTable requested = generator.generate();
		Connection connection = null;
		try{
			if(!connectionPool.isWritable()){ return; }
			if(updatedTables.contains(tableName)){ return; }
			updatedTables.add(tableName);
			connection = connectionPool.getDataSource().getConnection();
			Statement statement = connection.createStatement();
			boolean exists = tableNames.contains(tableName);
			if(!exists){
				System.out.println("========================================== Creating the table " +tableName 
						+" ============================");
				String sql = new SqlCreateTableGenerator(requested, JdbcTool.getSchemaName(connectionPool))
						.generateDdl();
				if(!schemaUpdateExecuteOptions.getCreateTables()){
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
				ConnectionSqlTableGenerator executeConstructor = new ConnectionSqlTableGenerator(connection, tableName, JdbcTool.getSchemaName(connectionPool));
				SqlTable executeCurrent = executeConstructor.generate();
				SqlAlterTableGenerator executeAlterTableGenerator = new SqlAlterTableGenerator(
						schemaUpdateExecuteOptions, executeCurrent, requested, JdbcTool.getSchemaName(connectionPool));
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
				ConnectionSqlTableGenerator prinitConstructor = new ConnectionSqlTableGenerator(connection, tableName, JdbcTool.getSchemaName(connectionPool));
				SqlTable printCurrent = prinitConstructor.generate();
				SqlAlterTableGenerator printAlterTableGenerator = new SqlAlterTableGenerator(schemaUpdatePrintOptions,
						printCurrent, requested, JdbcTool.getSchemaName(connectionPool));
				if(printAlterTableGenerator.willAlterTable()){
					System.out.println("========================================== Please Execute SchemaUpdate ======"
							+"======================");
					//print it
					String alterTablePrintString = printAlterTableGenerator.generateDdl();
					printedSchemaUpdates.add(alterTablePrintString);
					System.out.println(alterTablePrintString);
					System.out.println("========================================== Thank You ========================" 
							+"======================");
				}		
			}
		} catch (Exception e){
			throw new RuntimeException(e);
		} finally{
			JdbcTool.closeConnection(connection);
		}
	}
	
	protected void sendSchemaUpdateEmail(){
		if(CollectionTool.isEmpty(printedSchemaUpdates)){ return; }
		String administratorEmail = PropertiesTool.getFirstOccurrence(multiProperties, ADMINISTRATOR_EMAIL);
		String serverName = PropertiesTool.getFirstOccurrence(multiProperties, SERVER_NAME);
		if(StringTool.isEmpty(administratorEmail) || StringTool.isEmpty(serverName)){ return; }
		String subject = "SchemaUpdate request from "+serverName;
		StringBuilder body = new StringBuilder();
		for(String update : IterableTool.nullSafe(printedSchemaUpdates)){
			body.append(update + "\n\n");
		}
		DataRouterEmailTool.sendEmail("schemaupdate@hotpads.com", administratorEmail, subject, body.toString());
	}
}