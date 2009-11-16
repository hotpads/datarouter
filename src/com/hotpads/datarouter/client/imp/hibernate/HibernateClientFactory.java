package com.hotpads.datarouter.client.imp.hibernate;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import com.hotpads.datarouter.DataRouterFactory;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.Clients;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.profile.PhaseTimer;

public class HibernateClientFactory implements ClientFactory{
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
	
	@Override
	public Client createClient(
			DataRouterFactory<? extends DataRouter> datapus, String clientName, 
			Properties properties, Map<String,Object> params){
		String source = properties.getProperty(Clients.prefixClient+clientName+Clients.paramSource);
		if("params".equals(source)){
			return createFromParams(datapus, clientName, properties, params);
		}
		return createFromScratch(datapus, clientName, properties);
	}
	
	
	public HibernateClientImp createFromScratch(
			DataRouterFactory<? extends DataRouter> datapus, String clientName, Properties properties){
		logger.debug("creating hibernate client "+clientName);
		PhaseTimer timer = new PhaseTimer(clientName);
		
		HibernateClientImp client = new HibernateClientImp(clientName);
		
		AnnotationConfiguration sfConfig = new AnnotationConfiguration();
		
		//base config file for a SessionFactory
		String configFileLocation = properties.getProperty(Clients.prefixClient+clientName+paramConfigLocation);
		if(StringTool.isEmpty(configFileLocation)){ configFileLocation = configLocationDefault; }
		sfConfig.configure(configFileLocation);
		
		//databean config
		@SuppressWarnings("unchecked")
		Collection<Class<? extends Databean>> relevantDatabeanTypes = datapus.getRouter().getNodes().getTypesForClient(clientName);
		for(Class<? extends Databean> databeanClass : CollectionTool.nullSafe(relevantDatabeanTypes)){
//			logger.warn("init node "+databeanClass.getCanonicalName());
			try{
				sfConfig.addClass(databeanClass);
			}catch(org.hibernate.MappingNotFoundException mnfe){
				sfConfig.addAnnotatedClass(databeanClass);
			}
		}
		timer.add("parse");

		//connection pool config
		JdbcConnectionPool connectionPool = this.getConnectionPool(datapus, clientName, properties);
		client.connectionPool = connectionPool;
		sfConfig.setProperty(provider_class, HibernateConnectionProvider.class.getName());
		sfConfig.setProperty(connectionPoolName, connectionPool.getName());
		timer.add("gotPool");
		
		//readOnly?... currently being enforced in the connectionPool, and users should only declare "Reader" nodes
//		String slaveString = properties.getProperty(Clients.prefixClient+clientName+Clients.paramSlave);
//		boolean slave = BooleanTool.isTrue(slaveString);
		//TODO mind whether it's a slave or not
		
		//only way to get the connection pool to the ConnectionProvider is ThreadLocal or JNDI... using ThreadLocal
		HibernateConnectionProvider.bindDataSourceToThread(connectionPool);
		SessionFactory sessionFactory = sfConfig.buildSessionFactory();
		HibernateConnectionProvider.clearConnectionPoolFromThread();
		client.sessionFactory = sessionFactory;
		timer.add("built");
		
		logger.info(timer);
		
		return client;
	}

	
	public HibernateClientImp createFromParams(
			DataRouterFactory<? extends DataRouter> datapus, String clientName, 
			Properties properties, Map<String,Object> params){
		
		String sessionFactoryParamKey = properties.getProperty(Clients.prefixClient+clientName+nestedParamSessionFactory);
		SessionFactory sessionFactory = (SessionFactory)params.get(sessionFactoryParamKey);

		HibernateClientImp client = new HibernateClientImp(clientName);
		client.sessionFactory = sessionFactory;
		
		client.connectionPool = this.getConnectionPool(datapus, clientName, properties);
		
		return client;
	}
	
	protected JdbcConnectionPool getConnectionPool(
			DataRouterFactory<? extends DataRouter> datapus, String clientName, Properties properties){
		String connectionPoolName = properties.getProperty(Clients.prefixClient+clientName+Clients.paramConnectionPool);
		if(StringTool.isEmpty(connectionPoolName)){ connectionPoolName = clientName; }
		JdbcConnectionPool connectionPool = datapus.getConnectionPools().getConnectionPool(connectionPoolName);
		return connectionPool;
	}
	
	
}
