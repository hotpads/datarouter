package com.hotpads.datarouter.op.executor.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.SessionClient;
import com.hotpads.datarouter.op.TxnOp;
import com.hotpads.datarouter.op.executor.SessionExecutor;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.trace.TracerTool;
import com.hotpads.trace.TracerThreadLocal;

public class SessionExecutorImpl<T>
extends BaseTxnExecutor<T>
implements SessionExecutor, Callable<T>{
	private static Logger logger = LoggerFactory.getLogger(SessionExecutorImpl.class);

	public static final boolean EAGER_SESSION_FLUSH = true;
	
	//TODO have custom SessionExecutors for each module so we can compile-check this
	public static final Set<String> ROLLED_BACK_EXCEPTION_SIMPLE_NAMES = ImmutableSet.of(
			"MySQLTransactionRollbackException");
		
	private final TxnOp<T> parallelTxnOp;
	private String traceName;
	
	public SessionExecutorImpl(TxnOp<T> parallelTxnOp) {
		super(parallelTxnOp.getDatarouter(), parallelTxnOp);
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
		Collection<T> clientResults = new LinkedList<>();
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
			if(wasRolledBackAndShouldRetry(e)){
				//make sure JdbcRollbackRetryingCallable catches this particular exception
				throw new SessionExecutorPleaseRetryException(e);
			}else{
				try{
					rollbackTxns();
				}catch(RuntimeException exceptionDuringRollback){
					logger.warn("EXCEPTION THROWN DURING TXN ROLL-BACK", exceptionDuringRollback);
					throw e;
				}
				throw e;
			}
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
			}
		}
		T mergedResult = parallelTxnOp.mergeResults(onceResult, clientResults);
		return mergedResult;
	}
	

	
	/********************* session code **********************************/
	
	@Override
	public void openSessions(){
		for(Client client : DrCollectionTool.nullSafe(getClients())){
			if( ! (client instanceof SessionClient) ){
				continue;
			}
			SessionClient sessionClient = (SessionClient)client;
			sessionClient.openSession();
//			logger.warn("opened session on "+sessionClient.getExistingHandle());
			DRCounters.incClient(sessionClient.getType(), "openSession", sessionClient.getName());
		}
	}
	
	@Override
	public void flushSessions(){
		for(Client client : DrCollectionTool.nullSafe(getClients())){
			if( ! (client instanceof SessionClient) ){
				continue;
			}
			SessionClient sessionClient = (SessionClient)client;
			sessionClient.flushSession();
//			logger.warn("flushSession on "+sessionClient.getExistingHandle());
			DRCounters.incClient(sessionClient.getType(), "flushSession", sessionClient.getName());
		}
	}
	
	@Override
	public void cleanupSessions(){
		for(Client client : DrCollectionTool.nullSafe(getClients())){
			if( ! (client instanceof SessionClient) ){
				continue;
			}
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
		if(shouldTrace()){
			TracerTool.startSpan(TracerThreadLocal.get(), traceName);
		}
	}
	
	private void finishTrace(){
		if(shouldTrace()){
			TracerTool.finishSpan(TracerThreadLocal.get());
		}
	}
	
	private boolean wasRolledBackAndShouldRetry(Exception exception){
		if(exception == null){
			return false;
		}
		if(ROLLED_BACK_EXCEPTION_SIMPLE_NAMES.contains(exception.getClass().getSimpleName())){
			return true;
		}
		Throwable cause = exception.getCause();//unwrap hibernate exception
		if(cause == null){
			return false;
		}
		if(ROLLED_BACK_EXCEPTION_SIMPLE_NAMES.contains(cause.getClass().getSimpleName())){
			return true;
		}
		return false;
	}
	
}
