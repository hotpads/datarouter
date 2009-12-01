package com.hotpads.datarouter.client.imp.hibernate;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.connection.ConnectionHandle;

public class HibernateExecutor {
	Logger logger = Logger.getLogger(HibernateExecutor.class);

	private HibernateClientImp client;
	private Config config;
	private Session existingSession;
	
	public static HibernateExecutor create(
			HibernateClientImp client,
			Config config){
		HibernateExecutor executor = new HibernateExecutor();
		executor.client = client;
		executor.config = config;
		executor.existingSession = client.getExistingSession();
		return executor;
	}
	
	public Object executeTask(HibernateTask task){
		return executeTaskInSession(task);
	}
	
	public Object executeTaskInSession(HibernateTask task){
		Session session = existingSession;
		boolean newSession = session==null;
		try{
			if(newSession){
				this.client.reserveConnection();
				this.client.openSession();
				session = this.client.getExistingSession();
				logger.debug("found connection "+this.client.getExistingHandle());
			}
		
			return task.run(session);
			
		}finally{
			if(newSession){
				this.client.closeSession();
				ConnectionHandle handle = this.client.releaseConnection();
				logger.debug("released connection "+handle);
			}
		}
	}
	
}
