package com.hotpads.datarouter.op.executor.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.ConnectionClient;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.op.ClientOp;
import com.hotpads.datarouter.op.executor.ClientExecutor;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ExceptionTool;

public abstract class BaseClientExecutor<T>
implements ClientExecutor{
	private Logger logger = Logger.getLogger(getClass());

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

	public Logger getLogger(){
		return logger;
	}

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
