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

	private HibernateClientImp client;
	private Config config;
	private Session existingSession;
	
	public static HibernateExecutor create(
			HibernateClientImp client,
			Config config){
		HibernateExecutor executor = new HibernateExecutor();
		executor.client = client;
		executor.config = Config.nullSafe(config);
		executor.existingSession = client.getExistingSession();
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
				this.client.reserveConnection();
				this.client.beginTxn(
						config.getIsolationOrUse(Config.DEFAULT_ISOLATION), 
						config.getAutoCommitOrUse(true));
				this.client.openSession();
				session = this.client.getExistingSession();
				logger.debug("found connection "+this.client.getExistingHandle());
			}
		
			try{
				///////////////
				//the main purpose of this class, hidden in resource management code
				result = task.run(session);  
				/////////////////
				
			}catch(Exception e){
				try{
					this.client.rollbackTxn();
				}catch(Exception er){
					logger.warn("EXCEPTION DURING ROLLBACK for connection:"+this.client.getExistingHandle());
					logger.warn(ExceptionTool.getStackTraceAsString(er));
					throw new DataAccessException(e);
				}
				throw new RollbackException(e); //don't throw in the try block because it will get caught immediately
			}

			if(newSession){
				this.client.flushSession();
				this.client.commitTxn();
			}
			
			return result;
			
		}finally{
			if(newSession){
				ConnectionHandle handle = null;
				try{
					handle = this.client.releaseConnection();
				}catch(Exception e){
					logger.warn("EXCEPTION THROWN DURING RELEASE CONNECTION");
					logger.warn(ExceptionTool.getStackTraceAsString(e));
				}
				logger.debug("released connection "+handle);
			}
		}
	}
	
}
