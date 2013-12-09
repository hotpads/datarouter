package com.hotpads.datarouter.app.client.parallel.base;

import java.util.Collection;
import java.util.List;

import javax.persistence.RollbackException;

import com.hotpads.datarouter.app.parallel.ParallelSessionOp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.HibernateClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;

public abstract class BaseParallelSessionOp<T>
extends BaseParallelTxnOp<T>
implements ParallelSessionOp<T>{
		
	public BaseParallelSessionOp(DataRouterContext drContext, List<String> clientNames, Isolation isolation,
			boolean autoCommit) {
		super(drContext, clientNames, isolation, autoCommit);
	}


	/*******************************************************************/

	
	@Override
	public T call(){
		T onceResult = null;
		Collection<T> clientResults = ListTool.createLinkedList();
		Collection<Client> clients = this.getClients();
		try{
			reserveConnections();
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
			getLogger().warn(ExceptionTool.getStackTraceAsString(e));
			try{
				rollbackTxns();
			}catch(Exception exceptionDuringRollback){
				getLogger().warn("EXCEPTION THROWN DURING TXN ROLL-BACK");
				getLogger().warn(ExceptionTool.getStackTraceAsString(exceptionDuringRollback));
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
				getLogger().warn("EXCEPTION THROWN DURING RELEASE OF CONNECTIONS", e);
				getLogger().warn(ExceptionTool.getStackTraceAsString(e));
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
