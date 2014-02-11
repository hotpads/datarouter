package com.hotpads.datarouter.client.imp.hibernate.factory;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import com.hotpads.datarouter.client.Clients;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientImp;
import com.hotpads.datarouter.client.imp.hibernate.HibernateConnectionProvider;
import com.hotpads.datarouter.client.imp.hibernate.util.JdbcTool;
import com.hotpads.datarouter.client.imp.jdbc.factory.JdbcSimpleClientFactory;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.node.Nodes;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.profile.PhaseTimer;


public class HibernateSimpleClientFactory 
extends JdbcSimpleClientFactory{
	private static Logger logger = Logger.getLogger(HibernateSimpleClientFactory.class);

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

	public static final String configLocationDefault = "hib-default.cfg.xml";
	

	public HibernateSimpleClientFactory(DataRouterContext drContext, String clientName){
		super(drContext, clientName);
	}

	protected static final boolean SEPARATE_THREAD = true;// why do we need this separate thread?


	@Override
	public HibernateClientImp call(){
		PhaseTimer timer = new PhaseTimer(clientName);

		AnnotationConfiguration sfConfig = new AnnotationConfiguration();

		// base config file for a SessionFactory
		String configFileLocation = PropertiesTool.getFirstOccurrence(multiProperties, Clients.prefixClient + clientName
					+ paramConfigLocation);
		if(StringTool.isEmpty(configFileLocation)){
			configFileLocation = configLocationDefault;
		}
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
		sfConfig.setProperty(provider_class,HibernateConnectionProvider.class.getName());
		sfConfig.setProperty(connectionPoolName, connectionPool.getName());
		timer.add("gotPool");


		// only way to get the connection pool to the ConnectionProvider is
		// ThreadLocal or JNDI... using ThreadLocal
		HibernateConnectionProvider.bindDataSourceToThread(connectionPool);
		SessionFactory sessionFactory = sfConfig.buildSessionFactory();
		HibernateConnectionProvider.clearConnectionPoolFromThread();
		timer.add("built " + connectionPool);

		HibernateClientImp client = new HibernateClientImp(clientName, connectionPool, sessionFactory);

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

}