package com.hotpads.datarouter.client.imp.hibernate;

import javax.persistence.RollbackException;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.connection.ConnectionHandle;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.util.core.ExceptionTool;

public class HibernateExecutor {
	Logger logger = Logger.getLogger(HibernateExecutor.class);

	public static final boolean EAGER_SESSION_FLUSH = true;

	private HibernateClientImp client;
	private Config config;
	private Session existingSession;
	private boolean disableAutoCommit = true;  //default to true to be safe
	
	public static HibernateExecutor create(
			HibernateClientImp client,
			Config config,
			boolean disableAutoCommit){
		HibernateExecutor executor = new HibernateExecutor();
		executor.client = client;
		executor.config = Config.nullSafe(config);
		executor.existingSession = client.getExistingSession();
		executor.disableAutoCommit = disableAutoCommit;
		return executor;
	}
	
	public Object executeTask(HibernateTask task){
		return executeTaskInSession(task);
	}
	
	public Object executeTaskInSession(HibernateTask task){
		Session session = existingSession;
		boolean newSession = session==null;
		Object result;
		
		try{
			if(newSession){
				client.reserveConnection();
				client.beginTxn(
						config.getIsolationOrUse(Config.DEFAULT_ISOLATION), 
						disableAutoCommit);
				client.openSession();
				session = client.getExistingSession();
				logger.debug("found connection "+client.getExistingHandle());
			}
		
			try{
				///////////////
				//the main purpose of this class, hidden in resource management code
				result = task.run(session);  
				if(EAGER_SESSION_FLUSH){
					client.flushSession();
				}
				/////////////////
				
			}catch(Exception e){
				try{
					client.rollbackTxn();
				}catch(Exception er){
					logger.warn("EXCEPTION DURING ROLLBACK for connection:"+client.getExistingHandle());
					logger.warn(ExceptionTool.getStackTraceAsString(er));
					throw new DataAccessException(e);
				}
				throw new RollbackException(e); //don't throw in the try block because it will get caught immediately
			}

			if(newSession){
				if(!EAGER_SESSION_FLUSH){
					client.flushSession();
				}
				client.commitTxn();
			}
			
			return result;
			
		}finally{
			if(newSession){
				ConnectionHandle handle = null;
				try{
					handle = client.releaseConnection();
				}catch(Exception e){
					logger.warn("EXCEPTION THROWN DURING RELEASE CONNECTION");
					logger.warn(ExceptionTool.getStackTraceAsString(e));
				}
				logger.debug("released connection "+handle);
			}
		}
	}
	
}
