package com.hotpads.datarouter.op.executor.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.ConnectionClient;
import com.hotpads.datarouter.exception.DataAccessException;
import com.hotpads.datarouter.op.ClientOp;
import com.hotpads.datarouter.op.executor.ClientExecutor;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.DRCounters;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public abstract class BaseClientExecutor<T>
implements ClientExecutor{
	private static Logger logger = LoggerFactory.getLogger(BaseClientExecutor.class);

	private Datarouter datarouter;
	private ClientOp<T> parallelClientOp;

	public BaseClientExecutor(Datarouter datarouter, ClientOp<T> parallelClientOp) {
		this.datarouter = datarouter;
		this.parallelClientOp = parallelClientOp;
	}


	@Override
	public List<Client> getClients(){
		return datarouter.getClientPool().getClients(datarouter, parallelClientOp.getClientNames());
	}


	/******************* get /*************************************/

	public Datarouter getDatarouter(){
		return datarouter;
	}

	public ClientOp<T> getParallelClientOp(){
		return parallelClientOp;
	}

	/********************* txn code **********************************/

	@Override
	public void reserveConnections(){
		for(Client client : DrCollectionTool.nullSafe(getClients())){
			if( ! (client instanceof ConnectionClient) ){ continue; }
			ConnectionClient connectionClient = (ConnectionClient)client;
			connectionClient.reserveConnection();
//			logger.warn("reserveConnection "+handle);
			DRCounters.incClient(connectionClient.getType(), "reserveConnection", connectionClient.getName());
		}
	}

	@Override
	public void releaseConnections(){
		for(Client client : DrCollectionTool.nullSafe(getClients())){
			if( ! (client instanceof ConnectionClient) ){ continue; }
			ConnectionClient connectionClient = (ConnectionClient)client;
			try{
				connectionClient.releaseConnection();
//				logger.warn("releaseConnection "+handle);
				DRCounters.incClient(connectionClient.getType(), "releaseConnection", connectionClient.getName());
			}catch(Exception e){
				logger.warn("", e);
				throw new DataAccessException("EXCEPTION THROWN DURING RELEASE OF SINGLE CONNECTION, handle now=:"
						+connectionClient.getExistingHandle(), e);
			}
		}
	}

}
