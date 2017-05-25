package com.hotpads.datarouter.client.imp.jdbc.web;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.listener.DatarouterAppListener;
import com.mysql.jdbc.AbandonedConnectionCleanupThread;

@Singleton
public class JdbcAppListener extends DatarouterAppListener{
	private static final Logger logger = LoggerFactory.getLogger(JdbcAppListener.class);

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
		while(drivers.hasMoreElements()){
			Driver driver = drivers.nextElement();
			try{
				DriverManager.deregisterDriver(driver);
				logger.info("Driver unregistered : " + driver);
			}catch(SQLException e){
				logger.error("Error while unregistering driver " + driver, e);
			}
		}
	}

	private static void cleanupAbandonedConnections(){
		try{
			AbandonedConnectionCleanupThread.shutdown();
		}catch(InterruptedException e){
			logger.error("Could not shutdown AbandonedConnectionCleanupThread", e);
		}
	}
}
