package com.hotpads.datarouter.op.executor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.TxnClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.connection.ConnectionHandle;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.op.TxnOp;
import com.hotpads.datarouter.op.executor.TxnExecutor;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrExceptionTool;

public abstract class BaseTxnExecutor<T>
extends BaseClientExecutor<T>
implements TxnExecutor<T>{
	private static Logger logger = LoggerFactory.getLogger(BaseTxnExecutor.class);

	private TxnOp<T> parallelTxnOp;
	
	public BaseTxnExecutor(DatarouterContext drContext, TxnOp<T> parallelTxnOp) {
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
		for(Client client : DrCollectionTool.nullSafe(getClients())){
			if( ! (client instanceof TxnClient) ){ continue; }
			TxnClient txnClient = (TxnClient)client;
			ConnectionHandle connectionHandle = txnClient.getExistingHandle();
			if(connectionHandle.isOutermostHandle()){
				txnClient.beginTxn(getIsolation(), parallelTxnOp.isAutoCommit());
			}
//			logger.warn("beginTxn for "+txnClient.getExistingHandle());
			DRCounters.incClient(txnClient.getType(), "beginTxn", txnClient.getName());
		}
	}
	
	@Override
	public void commitTxns(){
		for(Client client : DrCollectionTool.nullSafe(getClients())){
			if( ! (client instanceof TxnClient) ){ continue; }
			TxnClient txnClient = (TxnClient)client;
			ConnectionHandle connectionHandle = txnClient.getExistingHandle();
			if(connectionHandle.isOutermostHandle()){
				txnClient.commitTxn();
			}
//			logger.warn("commitTxn for "+txnClient.getExistingHandle());
			DRCounters.incClient(txnClient.getType(), "commitTxn", txnClient.getName());
		}
	}
	
	@Override
	public void rollbackTxns(){
		for(Client client : DrCollectionTool.nullSafe(getClients())){
			if( ! (client instanceof TxnClient) ){ continue; }
			TxnClient txnClient = (TxnClient)client;
			try{
				txnClient.rollbackTxn();
//				logger.warn("rollbackTxn for "+txnClient.getExistingHandle());
				DRCounters.incClient(txnClient.getType(), "rollbackTxn", txnClient.getName());
			}catch(Exception e){
				logger.warn("", e);
				throw new DataAccessException("EXCEPTION THROWN DURING ROLLBACK OF SINGLE TXN:"
						+txnClient.getExistingHandle(), e);
			}
		}
	}
	
	
}
