package com.hotpads.datarouter.client.imp.hibernate.factory;

import java.sql.Connection;
import java.sql.ResultSet;
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
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlAlterTable;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlAlterTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlCreateTableGenerator;
import com.hotpads.datarouter.client.imp.jdbc.ddl.SqlCreateTableParser;
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
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.profile.PhaseTimer;

public class HibernateSimpleClientFactory implements HibernateClientFactory{
	Logger logger = Logger.getLogger(getClass());
	
	public static final String
		hibernate_connection_prefix = "hibernate.connection.",
		provider_class = hibernate_connection_prefix + "provider_class",  //from org.hibernate.cfg.Environment.CONNECTION_PROVIDER
		connectionPoolName = hibernate_connection_prefix + "connectionPoolName";  //any name... SessionFactory simply passes them through
	
	public static final String
		paramConfigLocation = ".configLocation",
		nestedParamSessionFactory = ".param.sessionFactory";
	
	public static final String
		configLocationDefault = "hib-default.cfg.xml";
	
	protected DataRouterContext drContext;
	protected String clientName;
	protected List<String> configFilePaths;
	protected List<Properties> multiProperties;
	protected ExecutorService executorService;
	protected HibernateClient client;
	
	
	public HibernateSimpleClientFactory(DataRouterContext drContext, String clientName, 
			ExecutorService executorService) {
		this.drContext = drContext;
		this.clientName = clientName;
		this.executorService = executorService;

		this.configFilePaths = drContext.getConfigFilePaths();
		this.multiProperties = PropertiesTool.fromFiles(configFilePaths);
	}
	
	protected static final boolean SEPARATE_THREAD = true;//why do we need this separate thread?
	
	@Override
	public HibernateClient getClient(){
		if(client!=null){ return client; }
//		logger.warn("activating Hibernate client "+clientName);
		if(SEPARATE_THREAD){
			synchronized(this){
				if(client!=null){ return client; }
				Future<HibernateClient> future = executorService.submit(new Callable<HibernateClient>(){
					@Override public HibernateClient call(){
						if(client!=null){ return client; }
						logger.warn("activating Hibernate client "+clientName);
						return createFromScratch(drContext, clientName);
					}
				});
				try{
					client = future.get();
				}catch(InterruptedException e){
					throw new RuntimeException(e);
				}catch(ExecutionException e){
					throw new RuntimeException(e);
				}
			}
			return client;
		}else{
			return createFromScratch(drContext, clientName);
		}
	}
	
	
	public HibernateClientImp createFromScratch(DataRouterContext drContext, String clientName){
		PhaseTimer timer = new PhaseTimer(clientName);
		
		HibernateClientImp client = new HibernateClientImp(clientName);
		
		AnnotationConfiguration sfConfig = new AnnotationConfiguration();
		
		//base config file for a SessionFactory
		String configFileLocation = PropertiesTool.getFirstOccurrence(multiProperties, 
				Clients.prefixClient+clientName+paramConfigLocation);
		if(StringTool.isEmpty(configFileLocation)){ configFileLocation = configLocationDefault; }
		sfConfig.configure(configFileLocation);

		//databean config
		@SuppressWarnings("unchecked")
		Collection<Class<? extends Databean<?,?>>> relevantDatabeanTypes = drContext.getNodes()
				.getTypesForClient(clientName);
		for(Class<? extends Databean<?,?>> databeanClass : CollectionTool.nullSafe(relevantDatabeanTypes)){
//			logger.warn(clientName+":"+databeanClass);
			try{
				sfConfig.addClass(databeanClass);
			}catch(org.hibernate.MappingNotFoundException mnfe){
				sfConfig.addAnnotatedClass(databeanClass);
			}
		}
		timer.add("parse");

		//connection pool config
		JdbcConnectionPool connectionPool = getConnectionPool(clientName, multiProperties);
		client.setConnectionPool(connectionPool);
		sfConfig.setProperty(provider_class, HibernateConnectionProvider.class.getName());
		sfConfig.setProperty(connectionPoolName, connectionPool.getName());
		timer.add("gotPool");
		


		Nodes nodes = drContext.getNodes();
		List<? extends PhysicalNode<?,?>> physicalNodes = nodes.getPhysicalNodesForClient(clientName);
		for(PhysicalNode<?,?> physicalNode : IterableTool.nullSafe(physicalNodes)){
			String tableName = physicalNode.getTableName();
			DatabeanFieldInfo<?,?,?> fieldInfo = physicalNode.getFieldInfo();
			if(fieldInfo.getFieldAware()){//use mohcine's table creator
				List<Field<?>> primaryKeyFields = fieldInfo.getPrimaryKeyFields();
				List<Field<?>> nonKeyFields = fieldInfo.getNonKeyFields();
				FieldSqlTableGenerator generator = new FieldSqlTableGenerator(physicalNode.getTableName(),
						primaryKeyFields, nonKeyFields);
				SqlTable requested = generator.generate();
				try {
					Connection connection = connectionPool.getDataSource().getConnection();
					List<String> tableNames = JdbcTool.showTables(connection);
					Statement statement = connection.createStatement();
					boolean exists = tableNames.contains(tableName);
					if( ! exists){
						//do create table
						String sql = new SqlCreateTableGenerator(requested).generate();
						statement.execute(sql);
					}else{
						ResultSet resultSet = statement.executeQuery("show create table "+physicalNode.getTableName());
						resultSet.next();
						SqlTable current = new SqlCreateTableParser(resultSet.getString(2)).parse();
						SqlAlterTableGenerator alterTableGenerator = new SqlAlterTableGenerator(current, requested);
						List<SqlAlterTable> alterations = alterTableGenerator.generate();
						String alterTableStatement = alterTableGenerator.getAlterTableStatement();
						statement.execute(alterTableStatement);
					}
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
				
			}else{//use hibernate's table creator
				Class<? extends Databean<?,?>> databeanClass = physicalNode.getDatabeanType();
				try{
					sfConfig.addClass(databeanClass);
				}catch(org.hibernate.MappingNotFoundException mnfe){
					sfConfig.addAnnotatedClass(databeanClass);
				}
			}
		}
		timer.add("schema update");
		
		//readOnly?... currently being enforced in the connectionPool, and users should only declare "Reader" nodes
//		String slaveString = properties.getProperty(Clients.prefixClient+clientName+Clients.paramSlave);
//		boolean slave = BooleanTool.isTrue(slaveString);
		//TODO mind whether it's a slave or not
		
		//only way to get the connection pool to the ConnectionProvider is ThreadLocal or JNDI... using ThreadLocal
		HibernateConnectionProvider.bindDataSourceToThread(connectionPool);
		SessionFactory sessionFactory = sfConfig.buildSessionFactory();
		HibernateConnectionProvider.clearConnectionPoolFromThread();
		client.setSessionFactory(sessionFactory);
		timer.add("built "+connectionPool);
		
//		//compare table schemas
//		for(Class<? extends Databean<?,?>> databeanClass : relevantDatabeanTypes) {
//	//		ResultSet resultSet2 = stmt.executeQuery("show create table Cheese;");
//			String createStatement = "create table etc"; //resultSet2.getStringOrSomethng();
//			SqlTable current = new SqlCreateTableParser(createStatement).parse();
//			SqlTable requested = null;//new SqlTableDatabeanGenerator(databeanClass).generate();
//			if(current==null) {
//				String create = new SqlCreateTableGenerator(requested).generate();
//				//issue create table statement
//			}else if(!current.equals(requested)) {
//				//issue alter table statement
//				String alter = new SqlAlterTableGenerator(current, requested).generate();
//				//send alter statement to server somehow
//			}//else it is ok
//		}
		
		logger.warn(timer);
		
		return client;
	}
	
	@Override
	public boolean isInitialized(){
		return client!=null;
	}
	
	protected JdbcConnectionPool getConnectionPool(String clientName, List<Properties> multiProperties){
		boolean writable = ClientId.getWritableNames(drContext.getClientPool().getClientIds())
				.contains(clientName);
		JdbcConnectionPool connectionPool = new JdbcConnectionPool(clientName, multiProperties, writable);
		return connectionPool;
	}
	
	
}