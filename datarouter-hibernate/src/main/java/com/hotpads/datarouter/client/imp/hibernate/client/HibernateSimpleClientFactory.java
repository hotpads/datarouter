package com.hotpads.datarouter.client.imp.hibernate.client;

import java.util.Collection;

import org.hibernate.MappingNotFoundException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.jdbc.ddl.execute.ParallelSchemaUpdate;
import com.hotpads.datarouter.client.imp.jdbc.factory.JdbcSimpleClientFactory;
import com.hotpads.datarouter.client.imp.jdbc.field.codec.factory.JdbcFieldCodecFactory;
import com.hotpads.datarouter.connection.JdbcConnectionPool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.profile.PhaseTimer;

public class HibernateSimpleClientFactory
extends JdbcSimpleClientFactory{
	private static final Logger logger = LoggerFactory.getLogger(HibernateSimpleClientFactory.class);

	private static final String
			HIBERNATE_CONNECTION_PREFIX = "hibernate.connection.",
			// from org.hibernate.cfg.Environment.CONNECTION_PROVIDER
			PROVIDER_CLASS = HIBERNATE_CONNECTION_PREFIX + "provider_class",
			// any name... SessionFactory simply passes them through
			CONNECTION_POOL_NAME = HIBERNATE_CONNECTION_PREFIX + "connectionPoolName",
			PARAM_configLocation = ".configLocation",
			CONFIG_LOCATION_DEFAULT = "hib-default.cfg.xml",
			PARAM_show_sql = ".hibernate.show_sql",
			PARAM_hbm2ddl_auto = ".hibernate.hibernate.hbm2ddl.auto";//the double-hibernate is intentional


	public HibernateSimpleClientFactory(Datarouter datarouter, JdbcFieldCodecFactory fieldCodecFactory,
			String clientName, ClientAvailabilitySettings clientAvailabilitySettings){
		super(datarouter, fieldCodecFactory, clientName, clientAvailabilitySettings);
	}

	@Override
	public HibernateClientImp call(){
		logger.info("HibernateSimpleClientFactory called for " + clientName, new Exception());
		PhaseTimer timer = new PhaseTimer(clientName);

		// base config file for a SessionFactory
		String configFileLocation = DrPropertiesTool.getFirstOccurrence(multiProperties,
				DatarouterClients.PREFIX_client	+ clientName + PARAM_configLocation);
		if(DrStringTool.isEmpty(configFileLocation)){
			configFileLocation = CONFIG_LOCATION_DEFAULT;
		}
		AnnotationConfiguration sfConfig = new AnnotationConfiguration();

		//add all databeanClasses until we're sure that none are using hibernate code (like GetJobletForProcessing)
		Collection<Class<?>> relevantDatabeanTypes = datarouter.getNodes().getTypesForClient(clientName);
		for(Class<?> databeanClass : DrCollectionTool.nullSafe(relevantDatabeanTypes)){
			try{
				sfConfig.addClass(databeanClass);
			}catch(MappingNotFoundException mnfe){
				sfConfig.addAnnotatedClass(databeanClass);
			}
		}
		sfConfig.configure(configFileLocation);
		timer.add("SessionFactory");

		addShowSqlSetting(sfConfig);
		addHbm2DdlSetting(sfConfig);

		// connect to the database
		JdbcConnectionPool connectionPool = initConnectionPool();
		timer.add("pool");

		sfConfig.setProperty(PROVIDER_CLASS, HibernateConnectionProvider.class.getName());
		sfConfig.setProperty(CONNECTION_POOL_NAME, connectionPool.getName());
		// only way to get the connection pool to the ConnectionProvider is ThreadLocal or JNDI... using ThreadLocal
		HibernateConnectionProvider.bindDataSourceToThread(connectionPool);
		SessionFactory sessionFactory = sfConfig.buildSessionFactory();
		HibernateConnectionProvider.clearConnectionPoolFromThread();
		timer.add("connection provider");

		HibernateClientImp client = new HibernateClientImp(clientName, connectionPool, sessionFactory,
				clientAvailabilitySettings);
		timer.add("client");

		if(doSchemaUpdate()){
			new ParallelSchemaUpdate(datarouter, fieldCodecFactory, clientName, connectionPool,
					schemaUpdatePrintOptions, schemaUpdateExecuteOptions).call();
			timer.add("schema update");
		}

		logger.warn(timer.toString());
		return client;
	}

	//this one doesn't wanna take
	private void addShowSqlSetting(AnnotationConfiguration sfConfig){
		 String showSqlPropertyKey = DatarouterClients.PREFIX_client + clientName + PARAM_show_sql;
		 String showSql = DrPropertiesTool.getFirstOccurrence(multiProperties, showSqlPropertyKey);
		 if(DrStringTool.notEmpty(showSql)){
			 sfConfig.setProperty("show_sql", showSql);
		 }
	}

	private void addHbm2DdlSetting(AnnotationConfiguration sfConfig){
		 String hbm2ddlPropertyKey = DatarouterClients.PREFIX_client + clientName + PARAM_hbm2ddl_auto;
		 String hbm2ddl = DrPropertiesTool.getFirstOccurrence(multiProperties, hbm2ddlPropertyKey);
		 if(DrStringTool.notEmpty(hbm2ddl)){
			 sfConfig.setProperty("hibernate.hbm2ddl.auto", hbm2ddl);
		 }
	}

}
