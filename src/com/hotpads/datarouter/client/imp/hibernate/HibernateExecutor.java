package com.hotpads.datarouter.client.imp.hibernate;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.hotpads.datarouter.config.Config;

public class HibernateExecutor {
	Logger logger = Logger.getLogger(HibernateExecutor.class);

	private HibernateClientImp client;
	private Config config;
	private Session existingSession;
	
	public static HibernateExecutor create(
			HibernateClientImp client,
			Config config,
			Session existingSession){
		HibernateExecutor executor = new HibernateExecutor();
		executor.client = client;
		executor.config = config;
		executor.existingSession = existingSession;
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
				Connection connectionSpecifiedByConfig = this.client.getConnection(config);
				if(connectionSpecifiedByConfig==null){	
					session = this.client.sessionFactory.openSession();
				}else{
					session = this.client.sessionFactory.openSession(connectionSpecifiedByConfig);
					logger.debug("found connection "+this.client.getConnectionNameForThisClient(config));
				}
			}
		
			return task.run(session);
			
		}finally{
			if(newSession){
				session.flush();
				session.close();
			}
		}
	}
	
}
