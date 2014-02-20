package com.hotpads.datarouter.op.executor.impl;

import java.util.Collection;
import java.util.concurrent.Callable;

import javax.persistence.RollbackException;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.JdbcClient;
import com.hotpads.datarouter.client.type.SessionClient;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.op.TxnOp;
import com.hotpads.datarouter.op.executor.SessionExecutor;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;

public class SessionExecutorImpl<T>
extends BaseTxnExecutor<T>
implements SessionExecutor<T>, Callable<T>{
	private static Logger logger = Logger.getLogger(SessionExecutorImpl.class);

	public static final boolean EAGER_SESSION_FLUSH = true;
		
	private TxnOp<T> parallelTxnOp;
	
	public SessionExecutorImpl(TxnOp<T> parallelTxnOp) {
		super(parallelTxnOp.getDataRouterContext(), parallelTxnOp);
		this.parallelTxnOp = parallelTxnOp;
	}


	/*******************************************************************/

	
	@Override
	public T call(){
		T onceResult = null;
		Collection<T> clientResults = ListTool.createLinkedList();
		Collection<Client> clients = getClients();
		try{
			reserveConnections();
			beginTxns();
			openSessions();
			
			//begin user code
			onceResult = parallelTxnOp.runOnce();
			for(Client client : CollectionTool.nullSafe(clients)){  //TODO threading
				T clientResult = parallelTxnOp.runOncePerClient(client);
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
		T mergedResult = parallelTxnOp.mergeResults(onceResult, clientResults);
		return mergedResult;
	}
	

	
	/********************* session code **********************************/
	
	@Override
	public void openSessions(){
		for(Client client : CollectionTool.nullSafe(getClients())){
			if( ! (client instanceof SessionClient) ){ continue; }
			SessionClient sessionClient = (SessionClient)client;
			sessionClient.openSession();
			logger.warn("opened session on "+sessionClient.getExistingHandle());
			DRCounters.incSuffixClient(sessionClient.getType(), "openSession", sessionClient.getName());
		}
	}
	
	@Override
	public void flushSessions(){
		for(Client client : CollectionTool.nullSafe(getClients())){
			if( ! (client instanceof SessionClient) ){ continue; }
			SessionClient sessionClient = (SessionClient)client;
			sessionClient.flushSession();
			logger.warn("flushSession on "+sessionClient.getExistingHandle());
			DRCounters.incSuffixClient(sessionClient.getType(), "flushSession", sessionClient.getName());
		}
	}
	
	@Override
	public void cleanupSessions(){
		for(Client client : CollectionTool.nullSafe(getClients())){
			if( ! (client instanceof SessionClient) ){ continue; }
			SessionClient sessionClient = (SessionClient)client;
			sessionClient.cleanupSession();
			logger.warn("cleanupSession on "+sessionClient.getExistingHandle());
			DRCounters.incSuffixClient(sessionClient.getType(), "cleanupSession", sessionClient.getName());
		}
	}
	
}
