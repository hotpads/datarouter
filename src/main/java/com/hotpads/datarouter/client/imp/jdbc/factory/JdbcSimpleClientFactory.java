package com.hotpads.datarouter.client.imp.jdbc.factory;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.Clients;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientImp;
import com.hotpads.datarouter.client.imp.hibernate.HibernateConnectionProvider;
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
import com.hotpads.datarouter.client.type.JdbcClient;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.node.Nodes;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
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


public class JdbcSimpleClientFactory implements JdbcClientFactory{
	Logger logger = Logger.getLogger(getClass());

	public static Boolean SCHEMA_UPDATE = false;

	public static final String 
			SERVER_NAME = "server.name",
			ADMINISTRATOR_EMAIL = "administrator.email",
			hibernate_connection_prefix = "hibernate.connection.",
			provider_class = hibernate_connection_prefix + "provider_class", // from org.hibernate.cfg.Environment.CONNECTION_PROVIDER
			connectionPoolName = hibernate_connection_prefix + "connectionPoolName", // any name... SessionFactory simply passes them through
			schemaUpdatePrintPrefix = "schemaUpdate.print",
			schemaUpdateExecutePrefix = "schemaUpdate.execute";

	public static final String 
			paramConfigLocation = ".configLocation",
			nestedParamSessionFactory = ".param.sessionFactory";

	protected DataRouterContext drContext;
	protected String clientName;
	protected Set<String> configFilePaths;
	protected List<Properties> multiProperties;
	protected SchemaUpdateOptions schemaUpdatePrintOptions;
	protected SchemaUpdateOptions schemaUpdateExecuteOptions;
	protected Set<String> updatedTables;
	protected List<String> printedSchemaUpdates;	
	protected ExecutorService executorService;
	protected JdbcClient client;
	

	public JdbcSimpleClientFactory(DataRouterContext drContext, String clientName, 
			ExecutorService executorService){
		this.drContext = drContext;
		this.clientName = clientName;
		this.executorService = executorService;

		this.configFilePaths = drContext.getConfigFilePaths();
		this.multiProperties = PropertiesTool.fromFiles(configFilePaths);
		this.schemaUpdatePrintOptions = new SchemaUpdateOptions(multiProperties, schemaUpdatePrintPrefix, true	);
		this.schemaUpdateExecuteOptions = new SchemaUpdateOptions(multiProperties, schemaUpdateExecutePrefix, false);
		this.updatedTables = SetTool.createTreeSet();
		this.printedSchemaUpdates = ListTool.createArrayList();
		SCHEMA_UPDATE = BooleanTool.isTrue(PropertiesTool.getFirstOccurrence(multiProperties, "schemaUpdate.enable"));
	}

	protected static final boolean SEPARATE_THREAD = true;// why do we need this separate thread?

	@Override
	public JdbcClient getClient(){
		if(client != null){
			return client;
		}
		// logger.warn("activating Hibernate client "+clientName);
		if(SEPARATE_THREAD){
			synchronized (this){
				if(client != null){
					return client;
				}
				if("event".equals(clientName)){
					logger.warn("instantiating " + clientName);
					int breakpoint = 1;
				}
				Future<JdbcClient> future = executorService
						.submit(new Callable<JdbcClient>(){
							@Override
							public JdbcClient call(){
								if(client != null){
									return client;
								}
								logger.warn("activating Hibernate client "
										+ clientName);
								return createFromScratch(drContext, clientName);
							}
						});
				try{
					client = future.get();
				} catch (InterruptedException e){
					throw new RuntimeException(e);
				} catch (ExecutionException e){
					throw new RuntimeException(e);
				}
			}
			return client;
		} else{
			return createFromScratch(drContext, clientName);
		}
	}

	public JdbcClientImp createFromScratch(DataRouterContext drContext, String clientName){
		PhaseTimer timer = new PhaseTimer(clientName);

		JdbcClientImp client = new JdbcClientImp(clientName);

		AnnotationConfiguration sfConfig = new AnnotationConfiguration();

		// base config file for a SessionFactory
		String configFileLocation = PropertiesTool.getFirstOccurrence(multiProperties, Clients.prefixClient + clientName
					+ paramConfigLocation);
//		if(StringTool.isEmpty(configFileLocation)){
//			configFileLocation = configLocationDefault;
//		}
		sfConfig.configure(configFileLocation);

		// //hibernate databeans (register before connecting to db)
		@SuppressWarnings("unchecked")
		Collection<Class<? extends Databean<?, ?>>> relevantDatabeanTypes = drContext.getNodes().getTypesForClient(
				clientName);
		for (Class<? extends Databean<?, ?>> databeanClass : CollectionTool.nullSafe(relevantDatabeanTypes)){
			// TODO skip fieldAware databeans
			// logger.warn(clientName+":"+databeanClass);
			try{
				sfConfig.addClass(databeanClass);
			} catch (org.hibernate.MappingNotFoundException mnfe){
				sfConfig.addAnnotatedClass(databeanClass);
			}
		}
		timer.add("SessionFactory");

		// connect to the database
		JdbcConnectionPool connectionPool = getConnectionPool(clientName, multiProperties);
		client.setConnectionPool(connectionPool);
		sfConfig.setProperty(provider_class,HibernateConnectionProvider.class.getName());
		sfConfig.setProperty(connectionPoolName, connectionPool.getName());
		timer.add("gotPool");

		// only way to get the connection pool to the ConnectionProvider is
		// ThreadLocal or JNDI... using ThreadLocal
		HibernateConnectionProvider.bindDataSourceToThread(connectionPool);
		SessionFactory sessionFactory = sfConfig.buildSessionFactory();
		HibernateConnectionProvider.clearConnectionPoolFromThread();
		client.setSessionFactory(sessionFactory);
		timer.add("built " + connectionPool);

		// datarouter fieldAware databeans (register after connecting to db)
		Connection connection = null;
		try{
			connection = JdbcTool.checkOutConnectionFromPool(connectionPool);
			List<String> tableNames = JdbcTool.showTables(connection);
//			System.out.println("table names : ");
//			for (String s : tableNames){
//				System.out.println(s);
//			}
			Nodes nodes = drContext.getNodes();
			List<? extends PhysicalNode<?, ?>> physicalNodes = nodes.getPhysicalNodesForClient(clientName);
			for(PhysicalNode<?, ?> physicalNode : IterableTool.nullSafe(physicalNodes)){
				String tableName = physicalNode.getTableName();
				// logger.warn(clientName+":"+tableName);
				DatabeanFieldInfo<?, ?, ?> fieldInfo = physicalNode.getFieldInfo();
				if(SCHEMA_UPDATE && fieldInfo.getFieldAware()){
					createOrUpdateTableIfNeeded(tableNames, connectionPool, physicalNode);
				}
			}
		} finally{
			JdbcTool.closeConnection(connection);// is this how you return it to
													// the pool?
		}
		sendSchemaUpdateEmail();
		timer.add("schema update");

		logger.warn(timer);

		return client;
	}

	@Override
	public boolean isInitialized(){
		return client != null;
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