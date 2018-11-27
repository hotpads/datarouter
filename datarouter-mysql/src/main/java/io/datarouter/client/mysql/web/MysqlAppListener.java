/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.client.mysql.web;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import io.datarouter.web.listener.DatarouterAppListener;

@Singleton
public class MysqlAppListener implements DatarouterAppListener{
	private static final Logger logger = LoggerFactory.getLogger(MysqlAppListener.class);

	@Override
	public void onShutDown(){
		unregisterDrivers();
		cleanupAbandonedConnections();
	}

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
		AbandonedConnectionCleanupThread.checkedShutdown();
	}

}
