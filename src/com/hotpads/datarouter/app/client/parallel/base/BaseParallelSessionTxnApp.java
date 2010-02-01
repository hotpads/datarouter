package com.hotpads.datarouter.app.client.parallel.base;

import java.util.Collection;

import javax.persistence.RollbackException;

import com.hotpads.datarouter.app.client.parallel.ParallelSessionApp;
import com.hotpads.datarouter.app.client.parallel.ParallelTxnApp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.HibernateClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;

public abstract class BaseParallelSessionTxnApp<T>
extends BaseParallelTxnApp<T>
implements ParallelTxnApp<T>, ParallelSessionApp<T>{
	
	
	public BaseParallelSessionTxnApp(DataRouter router) {
		super(router);
	}
	
	public BaseParallelSessionTxnApp(DataRouter router, Isolation isolation) {
		super(router);
		this.isolation = isolation;
	}


	/*******************************************************************/

	
	@Override
	public T runInEnvironment(){
		T onceResult = null;
		Collection<T> clientResults = ListTool.createLinkedList();
		Collection<Client> clients = this.getClients();
		try{
			reserveConections();
			beginTxns();
			openSessions();
			
			//begin user code
			onceResult = runOnce();
			for(Client client : CollectionTool.nullSafe(clients)){  //TODO threading
				T clientResult = runOncePerClient(client);
				clientResults.add(clientResult);
			}
			//end user code
			
			flushSessions();
			commitTxns();
			
		}catch(Exception e){
			logger.warn(ExceptionTool.getStackTraceAsString(e));
			try{
				rollbackTxns();
			}catch(Exception exceptionDuringRollback){
				logger.warn("EXCEPTION THROWN DURING TXN ROLL-BACK");
				logger.warn(ExceptionTool.getStackTraceAsString(exceptionDuringRollback));
				throw new DataAccessException(e);
			}
			throw new RollbackException(e);//don't throw in the try block because it will get caught immediately
		}finally{
//			try{
//				cleanupSessions();
//			}catch(Exception e){
//				logger.warn("EXCEPTION THROWN DURING CLEANUP OF SESSIONS", e);
//				logger.warn(ExceptionTool.getStackTraceAsString(e));
//			}
			try{
				releaseConnections();
			}catch(Exception e){
				//This is an unexpected exception because each individual release is done in a try/catch block
				logger.warn("EXCEPTION THROWN DURING RELEASE OF CONNECTIONS", e);
				logger.warn(ExceptionTool.getStackTraceAsString(e));
			}
		}
		T mergedResult = mergeResults(onceResult, clientResults);
		return mergedResult;
	}
	

	
	/********************* session code **********************************/
	
	@Override
	public void openSessions(){
		for(Client client : CollectionTool.nullSafe(this.getClients())){
			if( ! (client instanceof HibernateClient) ){ continue; }
			HibernateClient sessionClient = (HibernateClient)client;
			sessionClient.openSession();
//			logger.debug("opened session on "+sessionClient.getExistingHandle());
		}
	}
	
	@Override
	public void flushSessions(){
		for(Client client : CollectionTool.nullSafe(this.getClients())){
			if( ! (client instanceof HibernateClient) ){ continue; }
			HibernateClient sessionClient = (HibernateClient)client;
			sessionClient.flushSession();
		}
	}
	
	@Override
	public void cleanupSessions(){
		for(Client client : CollectionTool.nullSafe(this.getClients())){
			if( ! (client instanceof HibernateClient) ){ continue; }
			HibernateClient sessionClient = (HibernateClient)client;
			sessionClient.cleanupSession();
		}
	}
	
}
