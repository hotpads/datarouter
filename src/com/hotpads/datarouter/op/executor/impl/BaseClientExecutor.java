package com.hotpads.datarouter.op.executor.impl;

import java.util.List;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.ConnectionClient;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.op.BaseDataRouterOp;
import com.hotpads.datarouter.op.ClientOp;
import com.hotpads.datarouter.op.executor.ClientExecutor;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;

public abstract class BaseClientExecutor<T>
extends BaseDataRouterOp<T>
implements ClientExecutor{
	
	private ClientOp<T> parallelClientOp;
	
	public BaseClientExecutor(DataRouterContext drContext, ClientOp<T> parallelClientOp) {
		super(drContext);
		this.parallelClientOp = parallelClientOp;
	}
	

	@Override
	public List<Client> getClients(){
		return getDataRouterContext().getClientPool().getClients(parallelClientOp.getClientNames());
	}

	public ClientOp<T> getParallelClientOp(){
		return parallelClientOp;
	}
	

	/********************* txn code **********************************/

	@Override
	public void reserveConnections(){
		for(Client client : CollectionTool.nullSafe(getClients())){
			if( ! (client instanceof ConnectionClient) ){ continue; }
			ConnectionClient connectionClient = (ConnectionClient)client;
			connectionClient.reserveConnection();
//			logger.debug("reserved "+handle);
		}
	}

	@Override
	public void releaseConnections(){
		for(Client client : CollectionTool.nullSafe(getClients())){
			if( ! (client instanceof ConnectionClient) ){ continue; }
			ConnectionClient connectionClient = (ConnectionClient)client;
			try{
				connectionClient.releaseConnection();
//				logger.debug("released "+handle);
			}catch(Exception e){
				getLogger().warn(ExceptionTool.getStackTraceAsString(e));
				throw new DataAccessException("EXCEPTION THROWN DURING RELEASE OF SINGLE CONNECTION, handle now=:"
						+connectionClient.getExistingHandle(), e);
			}
		}
	}
	
}
