package com.hotpads.datarouter.app.client.parallel.base;

import java.util.List;

import com.hotpads.datarouter.app.parallel.ParallelTxnOp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.TxnClient;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.connection.ConnectionHandle;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;

public abstract class BaseParallelTxnOp<T>
extends BaseParallelClientOp<T>
implements ParallelTxnOp<T>{

	private Isolation isolation = Config.DEFAULT_ISOLATION;
	private boolean autoCommit;
	
	public BaseParallelTxnOp(DataRouterContext drContext, List<String> clientNames, Isolation isolation,
			boolean autoCommit) {
		super(drContext, clientNames);
		this.isolation = isolation;
		this.autoCommit = autoCommit;
	}
	
	@Override
	public Isolation getIsolation() {
		return isolation;
	}
	
	/********************* txn code **********************************/

	@Override
	public void beginTxns(){
		for(Client client : CollectionTool.nullSafe(getClients())){
			if( ! (client instanceof TxnClient) ){ continue; }
			TxnClient txnClient = (TxnClient)client;
			ConnectionHandle connectionHandle = txnClient.getExistingHandle();
			if(connectionHandle.isOutermostHandle()){
				txnClient.beginTxn(this.getIsolation(), autoCommit);
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
