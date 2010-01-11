package com.hotpads.datarouter.app.client.parallel.base;

import java.util.Collection;

import javax.persistence.RollbackException;

import com.hotpads.datarouter.app.client.parallel.ParallelTxnApp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.TxnClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.connection.ConnectionHandle;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.ListTool;

public abstract class BaseParallelTxnApp<T>
extends BaseParallelClientApp<T>
implements ParallelTxnApp<T>{

	Isolation isolation = Isolation.repeatableRead;
	
	
	public BaseParallelTxnApp(DataRouter router) {
		super(router);
	}
	
	public BaseParallelTxnApp(DataRouter router, Isolation isolation) {
		super(router);
		this.isolation = isolation;
	}


	/*******************************************************************/

	
	@Override
	public abstract T runInEnvironment();
//	{
//		T onceResult = null;
//		Collection<T> clientResults = ListTool.createLinkedList();
//		Collection<Client> clients = this.getClients();
//		try{
//			reserveConections();
//			beginTxns();
//			
//			//begin abstract user methods
//			onceResult = runOnce();
//			for(Client client : CollectionTool.nullSafe(clients)){  //TODO threading
//				T clientResult = runOncePerClient(client);
//				clientResults.add(clientResult);
//			}
//			//end abstract user methods 
//			
//		}catch(Exception e){
//			logger.warn(ExceptionTool.getStackTraceAsString(e));
//			rollbackTxns();
//			throw new RollbackException(e);
//		}finally{
//			try{
//				commitTxns();
//			}catch(Exception e){
//				//This is an unexpected exception because each individual release is done in a try/catch block
//				logger.warn(ExceptionTool.getStackTraceAsString(e));
//				throw new DataAccessException("EXCEPTION THROWN WHILE COMMITTING TXNS", e);
//			}
//			try{
//				releaseConnections();
//			}catch(Exception e){
//				//This is an unexpected exception because each individual release is done in a try/catch block
//				logger.warn(ExceptionTool.getStackTraceAsString(e));
//				throw new DataAccessException("EXCEPTION THROWN DURING RELEASE OF CONNECTIONS", e);
//			}
//		}
//		T mergedResult = mergeResults(onceResult, clientResults);
//		return mergedResult;
//	}
	
	@Override
	public Isolation getIsolation() {
		return Isolation.repeatableRead;
	}
	
	/********************* txn code **********************************/

	@Override
	public void beginTxns(){
		for(Client client : CollectionTool.nullSafe(this.getClients())){
			if( ! (client instanceof TxnClient) ){ continue; }
			TxnClient txnClient = (TxnClient)client;
			txnClient.beginTxn(this.getIsolation());
//			logger.debug("began txn for "+txnClient.getExistingHandle());
		}
	}
	
	@Override
	public void commitTxns(){
		for(Client client : CollectionTool.nullSafe(this.getClients())){
			if( ! (client instanceof TxnClient) ){ continue; }
			TxnClient txnClient = (TxnClient)client;
			txnClient.commitTxn();
//			logger.debug("committed txn for "+txnClient.getExistingHandle());
		}
	}
	
	@Override
	public void rollbackTxns(){
		for(Client client : CollectionTool.nullSafe(this.getClients())){
			if( ! (client instanceof TxnClient) ){ continue; }
			TxnClient txnClient = (TxnClient)client;
			try{
				txnClient.rollbackTxn();
			}catch(Exception e){
				logger.warn(ExceptionTool.getStackTraceAsString(e));
				throw new DataAccessException("EXCEPTION THROWN DURING ROLLBACK OF SINGLE TXN:"
						+txnClient.getExistingHandle(), e);
			}
		}
	}
	
	
}
