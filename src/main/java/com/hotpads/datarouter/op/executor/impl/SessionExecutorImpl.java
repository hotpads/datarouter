package com.hotpads.datarouter.op.executor.impl;

import java.util.Collection;
import java.util.concurrent.Callable;

import javax.persistence.RollbackException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.SessionClient;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.op.TxnOp;
import com.hotpads.datarouter.op.executor.SessionExecutor;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.trace.TraceContext;

public class SessionExecutorImpl<T>
extends BaseTxnExecutor<T>
implements SessionExecutor<T>, Callable<T>{
	private static Logger logger = LoggerFactory.getLogger(SessionExecutorImpl.class);

	public static final boolean EAGER_SESSION_FLUSH = true;
		
	private TxnOp<T> parallelTxnOp;
	private String traceName;
	
	public SessionExecutorImpl(TxnOp<T> parallelTxnOp) {
		super(parallelTxnOp.getDatarouterContext(), parallelTxnOp);
		this.parallelTxnOp = parallelTxnOp;
	}
	
	public SessionExecutorImpl(TxnOp<T> parallelTxnOp, String traceName) {
		this(parallelTxnOp);
		this.traceName = traceName;
	}


	/*******************************************************************/

	
	@Override
	public T call(){
		T onceResult = null;
		Collection<T> clientResults = DrListTool.createLinkedList();
		Collection<Client> clients = getClients();
		try{
			startTrace();
			reserveConnections();
			beginTxns();
			openSessions();
			
			//begin user code
			onceResult = parallelTxnOp.runOnce();
			for(Client client : DrCollectionTool.nullSafe(clients)){  //TODO threading
				T clientResult = parallelTxnOp.runOncePerClient(client);
				clientResults.add(clientResult);
			}
			//end user code
			
			flushSessions();
			commitTxns();
			
		}catch(Exception e){
			//logger.warn("", e));
			try{
				rollbackTxns();
			}catch(Exception exceptionDuringRollback){
				logger.warn("EXCEPTION THROWN DURING TXN ROLL-BACK");
				logger.warn("", exceptionDuringRollback);
				throw new DataAccessException(e);
			}
			throw new RollbackException(e);//don't throw in the try block because it will get caught immediately
		}finally{
			finishTrace();
			//i believe this is commented out until we figure out how to handle nested sessions
//			try{
//				cleanupSessions();
//			}catch(Exception e){
//				logger.warn("EXCEPTION THROWN DURING CLEANUP OF SESSIONS", e);
//				logger.warn("", e));
//			}
			try{
				releaseConnections();
			}catch(Exception e){
				//This is an unexpected exception because each individual release is done in a try/catch block
				logger.warn("EXCEPTION THROWN DURING RELEASE OF CONNECTIONS", e);
				logger.warn("", e);
			}
		}
		T mergedResult = parallelTxnOp.mergeResults(onceResult, clientResults);
		return mergedResult;
	}
	

	
	/********************* session code **********************************/
	
	@Override
	public void openSessions(){
		for(Client client : DrCollectionTool.nullSafe(getClients())){
			if( ! (client instanceof SessionClient) ){ continue; }
			SessionClient sessionClient = (SessionClient)client;
			sessionClient.openSession();
//			logger.warn("opened session on "+sessionClient.getExistingHandle());
			DRCounters.incClient(sessionClient.getType(), "openSession", sessionClient.getName());
		}
	}
	
	@Override
	public void flushSessions(){
		for(Client client : DrCollectionTool.nullSafe(getClients())){
			if( ! (client instanceof SessionClient) ){ continue; }
			SessionClient sessionClient = (SessionClient)client;
			sessionClient.flushSession();
//			logger.warn("flushSession on "+sessionClient.getExistingHandle());
			DRCounters.incClient(sessionClient.getType(), "flushSession", sessionClient.getName());
		}
	}
	
	@Override
	public void cleanupSessions(){
		for(Client client : DrCollectionTool.nullSafe(getClients())){
			if( ! (client instanceof SessionClient) ){ continue; }
			SessionClient sessionClient = (SessionClient)client;
			sessionClient.cleanupSession();
//			logger.warn("cleanupSession on "+sessionClient.getExistingHandle());
			DRCounters.incClient(sessionClient.getType(), "cleanupSession", sessionClient.getName());
		}
	}
	
	
	/********************** helper ******************************/
	
	private boolean shouldTrace(){
		return DrStringTool.notEmpty(traceName);
	}
	
	private void startTrace(){
		if(shouldTrace()){ TraceContext.startSpan(traceName); }
	}
	
	private void finishTrace(){
		if(shouldTrace()){ TraceContext.finishSpan(); }
	}
	
}
