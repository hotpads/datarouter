package com.hotpads.datarouter.op.executor.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.ConnectionClient;
import com.hotpads.datarouter.connection.ConnectionHandle;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.op.ClientOp;
import com.hotpads.datarouter.op.executor.ClientExecutor;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;

public abstract class BaseClientExecutor<T>
implements ClientExecutor{
	private static Logger logger = LoggerFactory.getLogger(BaseClientExecutor.class);

	private DataRouterContext drContext;
	private ClientOp<T> parallelClientOp;
	
	public BaseClientExecutor(DataRouterContext drContext, ClientOp<T> parallelClientOp) {
		this.drContext = drContext;
		this.parallelClientOp = parallelClientOp;
	}
	

	@Override
	public List<Client> getClients(){
		return drContext.getClientPool().getClients(parallelClientOp.getClientNames());
	}
	
	
	/******************* get /*************************************/

	public DataRouterContext getDrContext(){
		return drContext;
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
			ConnectionHandle handle = connectionClient.reserveConnection();
//			logger.warn("reserveConnection "+handle);
			DRCounters.incSuffixClient(connectionClient.getType(), "reserveConnection", connectionClient.getName());
		}
	}

	@Override
	public void releaseConnections(){
		for(Client client : CollectionTool.nullSafe(getClients())){
			if( ! (client instanceof ConnectionClient) ){ continue; }
			ConnectionClient connectionClient = (ConnectionClient)client;
			try{
				ConnectionHandle handle = connectionClient.releaseConnection();
//				logger.warn("releaseConnection "+handle);
				DRCounters.incSuffixClient(connectionClient.getType(), "releaseConnection", connectionClient.getName());
			}catch(Exception e){
				logger.warn(ExceptionTool.getStackTraceAsString(e));
				throw new DataAccessException("EXCEPTION THROWN DURING RELEASE OF SINGLE CONNECTION, handle now=:"
						+connectionClient.getExistingHandle(), e);
			}
		}
	}
	
}
