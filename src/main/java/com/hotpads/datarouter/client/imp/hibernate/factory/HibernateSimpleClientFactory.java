package com.hotpads.datarouter.client.imp.hibernate.factory;

import java.util.Collection;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.Clients;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientImp;
import com.hotpads.datarouter.client.imp.hibernate.HibernateConnectionProvider;
import com.hotpads.datarouter.client.imp.jdbc.ddl.execute.ParallelSchemaUpdate;
import com.hotpads.datarouter.client.imp.jdbc.factory.JdbcSimpleClientFactory;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.PropertiesTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.profile.PhaseTimer;


public class HibernateSimpleClientFactory 
extends JdbcSimpleClientFactory{
	private static Logger logger = LoggerFactory.getLogger(HibernateSimpleClientFactory.class);

	private static final String 
		HIBERNATE_CONNECTION_PREFIX = "hibernate.connection.",
		PROVIDER_CLASS = HIBERNATE_CONNECTION_PREFIX + "provider_class", // from org.hibernate.cfg.Environment.CONNECTION_PROVIDER
		CONNECTION_POOL_NAME = HIBERNATE_CONNECTION_PREFIX + "connectionPoolName", // any name... SessionFactory simply passes them through
		PARAM_CONFIG_LOCATION = ".configLocation",
		CONFIG_LOCATION_DEFAULT = "hib-default.cfg.xml";
	

	public HibernateSimpleClientFactory(DataRouterContext drContext, String clientName){
		super(drContext, clientName);
	}


	@Override
	public HibernateClientImp call(){
		PhaseTimer timer = new PhaseTimer(clientName);

		// base config file for a SessionFactory
		String configFileLocation = PropertiesTool.getFirstOccurrence(multiProperties, Clients.prefixClient + clientName
					+ PARAM_CONFIG_LOCATION);
		if(StringTool.isEmpty(configFileLocation)){
			configFileLocation = CONFIG_LOCATION_DEFAULT;
		}
		AnnotationConfiguration sfConfig = new AnnotationConfiguration();
		sfConfig.configure(configFileLocation);

		//this code will skip all nodes with fielders, which is the desired behavior, but some jdbc nodes are still using hibernate TxnOps =(
//		List<? extends PhysicalNode<?, ?>> physicalNodes = drContext.getNodes().getPhysicalNodesForClient(clientName);
//		for(PhysicalNode<?, ?> physicalNode : IterableTool.nullSafe(physicalNodes)){
//			DatabeanFieldInfo<?, ?, ?> fieldInfo = physicalNode.getFieldInfo();
//			if(fieldInfo.getFieldAware()){ continue; }//skip databeans with fielders
//			Class<? extends Databean<?, ?>> databeanClass = fieldInfo.getDatabeanClass();
		
		//add all databeanClasses until we're sure that none are using hibernate code (like GetJobletForProcessing)
		Collection<Class<? extends Databean<?, ?>>> relevantDatabeanTypes = drContext.getNodes().getTypesForClient(
				clientName);
		for (Class<? extends Databean<?, ?>> databeanClass : CollectionTool.nullSafe(relevantDatabeanTypes)){
		
			try{
				sfConfig.addClass(databeanClass);
			} catch (org.hibernate.MappingNotFoundException mnfe){
				sfConfig.addAnnotatedClass(databeanClass);
			}
		}
		timer.add("SessionFactory");

		// connect to the database
		JdbcConnectionPool connectionPool = getConnectionPool(clientName, multiProperties);
		timer.add("pool");
		
		sfConfig.setProperty(PROVIDER_CLASS,HibernateConnectionProvider.class.getName());
		sfConfig.setProperty(CONNECTION_POOL_NAME, connectionPool.getName());
		// only way to get the connection pool to the ConnectionProvider is ThreadLocal or JNDI... using ThreadLocal
		HibernateConnectionProvider.bindDataSourceToThread(connectionPool);
		SessionFactory sessionFactory = sfConfig.buildSessionFactory();
		HibernateConnectionProvider.clearConnectionPoolFromThread();
		timer.add("connection provider");

		HibernateClientImp client = new HibernateClientImp(clientName, connectionPool, sessionFactory);
		timer.add("client");
		
		if(doSchemaUpdate()){
			new ParallelSchemaUpdate(drContext, clientName, connectionPool).call();
			timer.add("schema update");
		}

		logger.warn(timer.toString());
		return client;
	}

}
