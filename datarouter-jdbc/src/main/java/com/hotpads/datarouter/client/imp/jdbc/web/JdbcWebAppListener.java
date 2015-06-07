package com.hotpads.datarouter.client.imp.jdbc.web;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.DatarouterWebAppListener;
import com.mysql.jdbc.AbandonedConnectionCleanupThread;

@Singleton
public class JdbcWebAppListener extends DatarouterWebAppListener{
	private static final Logger logger = LoggerFactory.getLogger(JdbcWebAppListener.class);
	
	
	@Override
	protected void onStartUp(){
	}

	
	@Override
	protected void onShutDown(){
		unregisterDrivers();
		cleanupAbandonedConnections();
	}

	
	/******************** private **********************/
	
	private static void unregisterDrivers(){
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()){
			Driver d = drivers.nextElement();
			try{
				DriverManager.deregisterDriver(d);
				logger.info("Driver unregistered : " + d);
			}catch (SQLException e){
				logger.error("Error while unregistering driver " + d, e);
			}
		}
	}
	
	private static void cleanupAbandonedConnections(){
		try{
			AbandonedConnectionCleanupThread.shutdown();
		}catch (InterruptedException e){
			logger.error("Could not shutdown AbandonedConnectionCleanupThread", e);
		}
	}
}
