package com.hotpads.datarouter.client.imp.hibernate.factory;

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
import com.hotpads.datarouter.client.type.HibernateClient;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.node.Nodes;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.util.core.CollectionTool;
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
	
	
	public HibernateSimpleClientFactory(
			DataRouterContext drContext,
			String clientName, 
			ExecutorService executorService){
		this.drContext = drContext;
		this.clientName = clientName;
		this.executorService = executorService;

		this.configFilePaths = drContext.getConfigFilePaths();
		this.multiProperties = PropertiesTool.fromFiles(configFilePaths);
	}
	
	
	@Override
	public HibernateClient getClient(){
		if(client!=null){ return client; }
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
				this.client = future.get();
			}catch(InterruptedException e){
				throw new RuntimeException(e);
			}catch(ExecutionException e){
				throw new RuntimeException(e);
			}
		}
		return this.client;
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
		
		logger.warn(timer);
		
		return client;
	}
	
	@Override
	public boolean isInitialized(){
		return client!=null;
	}
	
	protected JdbcConnectionPool getConnectionPool(String clientName, List<Properties> multiProperties){
		boolean writable = ClientId.getWritableNames(drContext.getClientPool().getClientIds())
				.contains(connectionPoolName);
		JdbcConnectionPool connectionPool = new JdbcConnectionPool(connectionPoolName, multiProperties, writable);
		return connectionPool;
	}
	
	
}