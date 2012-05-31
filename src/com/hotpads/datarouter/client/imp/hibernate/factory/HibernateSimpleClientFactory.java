package com.hotpads.datarouter.client.imp.hibernate.factory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
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
import com.hotpads.datarouter.client.imp.jdbc.ddl.FieldSqlTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SchemaUpdateOptions;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlAlterTableClause;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlAlterTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlColumn.SqlColumnNameComparator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlColumn.SqlColumnNameTypeComparator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlCreateTableFromConnection;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlCreateTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlTable;
import com.hotpads.datarouter.client.type.HibernateClient;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.node.Nodes;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.profile.PhaseTimer;


public class HibernateSimpleClientFactory implements HibernateClientFactory {
	Logger logger = Logger.getLogger(getClass());

	public static final Boolean SCHEMA_UPDATE = true;

	public static final String 
			hibernate_connection_prefix = "hibernate.connection.",
			provider_class = hibernate_connection_prefix + "provider_class", // from org.hibernate.cfg.Environment.CONNECTION_PROVIDER
			connectionPoolName = hibernate_connection_prefix + "connectionPoolName", // any name... SessionFactory simply passes them through
			schemaUpdatePrintPrefix = "schemaUpdate.print",
			schemaUpdateExecutePrefix = "schemaUpdate.execute";

	public static final String 
			paramConfigLocation = ".configLocation",
			nestedParamSessionFactory = ".param.sessionFactory";

	public static final String configLocationDefault = "hib-default.cfg.xml";

	protected DataRouterContext drContext;
	protected String clientName;
	protected List<String> configFilePaths;
	protected List<Properties> multiProperties;
	protected SchemaUpdateOptions schemaUpdatePrintOptions;
	protected SchemaUpdateOptions schemaUpdateExecuteOptions;
	protected ExecutorService executorService;
	protected HibernateClient client;

	public HibernateSimpleClientFactory(DataRouterContext drContext, String clientName, 
			ExecutorService executorService) {
		this.drContext = drContext;
		this.clientName = clientName;
		this.executorService = executorService;

		this.configFilePaths = drContext.getConfigFilePaths();
		this.multiProperties = PropertiesTool.fromFiles(configFilePaths);
		this.schemaUpdatePrintOptions = new SchemaUpdateOptions(multiProperties, schemaUpdatePrintPrefix, true	);
		this.schemaUpdateExecuteOptions = new SchemaUpdateOptions(multiProperties, schemaUpdateExecutePrefix, false);
	}

	protected static final boolean SEPARATE_THREAD = true;// why do we need this
															// separate thread?

	@Override
	public HibernateClient getClient() {
		if (client != null) {
			return client;
		}
		// logger.warn("activating Hibernate client "+clientName);
		if (SEPARATE_THREAD) {
			synchronized (this) {
				if (client != null) {
					return client;
				}
				if ("event".equals(clientName)) {
					logger.warn("intantiating " + clientName);
					int breakpoint = 1;
				}
				Future<HibernateClient> future = executorService
						.submit(new Callable<HibernateClient>() {
							@Override
							public HibernateClient call() {
								if (client != null) {
									return client;
								}
								logger.warn("activating Hibernate client "
										+ clientName);
								return createFromScratch(drContext, clientName);
							}
						});
				try {
					client = future.get();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} catch (ExecutionException e) {
					throw new RuntimeException(e);
				}
			}
			return client;
		} else {
			return createFromScratch(drContext, clientName);
		}
	}

	public HibernateClientImp createFromScratch(DataRouterContext drContext, String clientName) {
		PhaseTimer timer = new PhaseTimer(clientName);

		HibernateClientImp client = new HibernateClientImp(clientName);

		AnnotationConfiguration sfConfig = new AnnotationConfiguration();

		// base config file for a SessionFactory
		String configFileLocation = PropertiesTool.getFirstOccurrence(multiProperties, Clients.prefixClient + clientName
					+ paramConfigLocation);
		if (StringTool.isEmpty(configFileLocation)) {
			configFileLocation = configLocationDefault;
		}
		sfConfig.configure(configFileLocation);

		// //hibernate databeans (register before connecting to db)
		@SuppressWarnings("unchecked")
		Collection<Class<? extends Databean<?, ?>>> relevantDatabeanTypes = drContext.getNodes().getTypesForClient(
				clientName);
		for (Class<? extends Databean<?, ?>> databeanClass : CollectionTool.nullSafe(relevantDatabeanTypes)) {
			// TODO skip fieldAware databeans
			// logger.warn(clientName+":"+databeanClass);
			try {
				sfConfig.addClass(databeanClass);
			} catch (org.hibernate.MappingNotFoundException mnfe) {
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
		try {
			connection = JdbcTool.checkOutConnectionFromPool(connectionPool);
			List<String> tableNames = JdbcTool.showTables(connection);
			System.out.println("table names : ");
			for (String s : tableNames) {
				System.out.println(s);
			}
			Nodes nodes = drContext.getNodes();
			List<? extends PhysicalNode<?, ?>> physicalNodes = nodes.getPhysicalNodesForClient(clientName);
			for(PhysicalNode<?, ?> physicalNode : IterableTool.nullSafe(physicalNodes)){
				String tableName = physicalNode.getTableName();
				// logger.warn(clientName+":"+tableName);
				DatabeanFieldInfo<?, ?, ?> fieldInfo = physicalNode.getFieldInfo();
				if (SCHEMA_UPDATE && fieldInfo.getFieldAware()) {
					createOrUpdateTableIfNeeded(tableNames, connectionPool, physicalNode);
				}
			}
		} finally {
			JdbcTool.closeConnection(connection);// is this how you return it to
													// the pool?
		}
		timer.add("schema update");

		logger.warn(timer);

		return client;
	}

	@Override
	public boolean isInitialized() {
		return client != null;
	}

	protected JdbcConnectionPool getConnectionPool(String clientName,
			List<Properties> multiProperties) {
		boolean writable = ClientId.getWritableNames(drContext.getClientPool().getClientIds()).contains(clientName);
		JdbcConnectionPool connectionPool = new JdbcConnectionPool(clientName, multiProperties, writable);
		return connectionPool;
	}

	protected void createOrUpdateTableIfNeeded(List<String> tableNames, JdbcConnectionPool connectionPool, 
			PhysicalNode<?, ?> physicalNode) {

		if (!SCHEMA_UPDATE) {
			return;
		}
		// if(!schemaUpdateOptions.anyTrue()){ return; }

		String tableName = physicalNode.getTableName();
		DatabeanFieldInfo<?, ?, ?> fieldInfo = physicalNode.getFieldInfo();
		List<Field<?>> primaryKeyFields = fieldInfo.getPrimaryKeyFields();
		List<Field<?>> nonKeyFields = fieldInfo.getNonKeyFields();
		List<List<Field<?>>> indexes = ListTool.nullSafe(fieldInfo.getIndexes());

		FieldSqlTableGenerator generator = new FieldSqlTableGenerator(physicalNode.getTableName(), primaryKeyFields, 
				nonKeyFields);
		generator.setIndexes(indexes);

		SqlTable requested = generator.generate();
		Connection connection = null;
		try {
			if (!connectionPool.isWritable()) { return; }
			connection = connectionPool.getDataSource().getConnection();
			Statement statement = connection.createStatement();
			boolean exists = tableNames.contains(tableName);
			if (!exists) {
				String sql = new SqlCreateTableGenerator(requested).generate();
				if(schemaUpdatePrintOptions.getCreateTables()){
					System.out.println("Please execute: "+sql);
				}
				if (schemaUpdateExecuteOptions.getCreateTables()) {
					System.out.println("** Creating table :" + sql);
					statement.execute(sql);
				}
			} else {
				/*if (!schemaUpdateOptions.anyAlterTrue()) {
					return;
				}*/
				
				
				// TODO DELETE THIS COMPARATORS
				//SqlColumnNameComparator nameComparator = new SqlColumnNameComparator(true);
				//SqlColumnNameTypeComparator nameTypeComparator = new SqlColumnNameTypeComparator(true);
				
				//execute the alter table
				SqlCreateTableFromConnection constructor = new SqlCreateTableFromConnection(connection, tableName);
				SqlTable current = constructor.getTable();
				SqlAlterTableGenerator alterTableGenerator = new SqlAlterTableGenerator(schemaUpdateExecuteOptions,
																							current, requested);
				String alterTableString = alterTableGenerator.getMasterAlterStatement();
				
				if(StringTool.notEmpty(alterTableString)){
					System.out.println("Executing ...");
					System.out.println(alterTableString);
					//execute it
					statement.execute(alterTableString);
				}
				
				//print the alter table
				constructor = new SqlCreateTableFromConnection(connection, tableName);
				current = constructor.getTable();
				alterTableGenerator = new SqlAlterTableGenerator(
						schemaUpdateExecuteOptions, current, requested);
				alterTableString = alterTableGenerator.getMasterAlterStatement();
				if(StringTool.notEmpty(alterTableString)){
					System.out.println("Please execute ...");
					//print it
					System.out.println(alterTableString);
				}		
				//				// */
				//				// System.out.println("current : " +current);
				//				// System.out.println("requested : " +requested);
				//				SqlAlterTableGenerator alterTablePrintGenerator = new SqlAlterTableGenerator(
				//						schemaUpdatePrintOptions, current, requested);
				////					String alterTableStringToPrint = alterTablePrintGenerator.ge....
				//				// List<SqlAlterTable> alterations =
				//				// alterTableGenerator.generate();
				//				List<SqlAlterTable> alterTablePrintStatements = alterTablePrintGenerator
				//						.getAlterTableStatements(nameTypeComparator);
				//				for (SqlAlterTable s : alterTablePrintStatements) {
				//					printStatementDependingOnSchemaUpdateOption(schemaUpdateOptions,s);
				//					executeStatementDependingOnSchemaUpdateOption(schemaUpdateOptions,s, statement);
				//					// statement.execute(s);
				//				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			JdbcTool.closeConnection(connection);
		}
	}
				//
				//	private void executeStatementDependingOnSchemaUpdateOption(
				//			SchemaUpdateOptions schemaUpdate, SqlAlterTable alterTable, Statement statement) throws SQLException {
				//		// TODO Auto-generated method stub
				//		switch (alterTable.getType()) {
				//		case ADD_COLUMN:
				//			if(schemaUpdate.getExecuteAddColumns()) {
				//				System.out.println(":---------- Execution ----------:");
				//				statement.execute(alterTable.getAlterTable());
				//			}
				//			break;
				//		case DROP_COLUMN:
				//			if(schemaUpdate.getExecuteDeleteColumns()) {
				//				System.out.println(":---------- Execution ----------:");
				//				statement.execute(alterTable.getAlterTable());
				//			}
				//			break;
				//		case MODIFY:
				//			if(schemaUpdate.getExecuteModify()) {
				//				System.out.println(":---------- Execution ----------:");
				//				statement.execute(alterTable.getAlterTable());
				//			}
				//			break;
				//		case ADD_INDEX:
				//			if(schemaUpdate.getExecuteAddIndex()) {
				//				System.out.println(":---------- Execution ----------:");
				//				statement.execute(alterTable.getAlterTable());
				//			}
				//			break;
				//		case DROP_INDEX:
				//			if(schemaUpdate.getExecuteDropIndex()) {
				//				System.out.println(":---------- Execution ----------:");
				//				statement.execute(alterTable.getAlterTable());
				//			}
				//			break;
				//		case CREATE_TABLE:
				//			if(schemaUpdate.getExecuteCreateTables()) {
				//				System.out.println(":---------- Execution ----------:");
				//				statement.execute(alterTable.getAlterTable());
				//			}
				//			break;
				//		case DROP_TABLE:
				//			if(schemaUpdate.getExecuteDropTables()) {
				//				System.out.println(":---------- Execution ----------:");
				//				statement.execute(alterTable.getAlterTable());
				//			}
				//			break;
				//		def
	// *ault:
				//			break;
				//		}
				//	}
				//
				//	private void printStatementDependingOnSchemaUpdateOption(
				//			SchemaUpdateOptions schemaUpdate, SqlAlterTable alterTable) {
				//		// TODO Auto-generated method stub
				//		switch (alterTable.getType()) {
				//		case ADD_COLUMN:
				//			if(schemaUpdate.getPrintAddColumns()) System.out.println(alterTable.getAlterTable());
				//			break;
				//		case DROP_COLUMN:
				//			if(schemaUpdate.getPrintDeleteColumns()) System.out.println(alterTable.getAlterTable());
				//			break;
				//		case MODIFY:
				//			if(schemaUpdate.getPrintModifyColumn()) System.out.println(alterTable.getAlterTable());
				//			break;
				//		case ADD_INDEX:
				//			if(schemaUpdate.getPrintAddIndex()) System.out.println(alterTable.getAlterTable());
				//			break;
				//		case DROP_INDEX:
				//			if(schemaUpdate.getPrintDropIndex()) System.out.println(alterTable.getAlterTable());
				//			break;
				//		case CREATE_TABLE:
				//			if(schemaUpdate.getPrintCreateTables()) System.out.println(alterTable.getAlterTable());
				//			break;
				//		case DROP_TABLE:
				//			if(schemaUpdate.getPrintDropTables()) System.out.println(alterTable.getAlterTable());
				//			break;
				//		default:
				//			break;
				//		}
				//	}
}