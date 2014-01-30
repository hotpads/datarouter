package com.hotpads.datarouter.op.executor.impl;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.TxnClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.connection.ConnectionHandle;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.op.TxnOp;
import com.hotpads.datarouter.op.executor.TxnExecutor;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;

public abstract class BaseTxnExecutor<T>
extends BaseClientExecutor<T>
implements TxnExecutor<T>{

	private TxnOp<T> parallelTxnOp;
	
	public BaseTxnExecutor(DataRouterContext drContext, TxnOp<T> parallelTxnOp) {
		super(drContext, parallelTxnOp);
		this.parallelTxnOp = parallelTxnOp;
	}
	
	@Override
	public Isolation getIsolation() {
		return parallelTxnOp.getIsolation();
	}
	
	/********************* txn code **********************************/

	@Override
	public void beginTxns(){
		for(Client client : CollectionTool.nullSafe(getClients())){
			if( ! (client instanceof TxnClient) ){ continue; }
			TxnClient txnClient = (TxnClient)client;
			ConnectionHandle connectionHandle = txnClient.getExistingHandle();
			if(connectionHandle.isOutermostHandle()){
				txnClient.beginTxn(this.getIsolation(), parallelTxnOp.isAutoCommit());
			}
//			logger.debug("began txn for "+txnClient.getExistingHandle());
		}
	}
	
	@Override
	public void commitTxns(){
		for(Client client : CollectionTool.nullSafe(getClients())){
			if( ! (client instanceof TxnClient) ){ continue; }
			TxnClient txnClient = (TxnClient)client;
			ConnectionHandle connectionHandle = txnClient.getExistingHandle();
			if(connectionHandle.isOutermostHandle()){
				txnClient.commitTxn();
			}
//			logger.debug("committed txn for "+txnClient.getExistingHandle());
		}
	}
	
	@Override
	public void rollbackTxns(){
		for(Client client : CollectionTool.nullSafe(getClients())){
			if( ! (client instanceof TxnClient) ){ continue; }
			TxnClient txnClient = (TxnClient)client;
			try{
				txnClient.rollbackTxn();
			}catch(Exception e){
				getLogger().warn(ExceptionTool.getStackTraceAsString(e));
				throw new DataAccessException("EXCEPTION THROWN DURING ROLLBACK OF SINGLE TXN:"
						+txnClient.getExistingHandle(), e);
			}
		}
	}
	
	
}
