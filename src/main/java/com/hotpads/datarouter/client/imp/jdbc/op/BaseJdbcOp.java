package com.hotpads.datarouter.client.imp.jdbc.op;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.JdbcConnectionClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.TxnOp;
import com.hotpads.datarouter.op.aware.ConnectionAware;
import com.hotpads.datarouter.routing.DataRouterContext;

public abstract class BaseJdbcOp<T>
implements TxnOp<T>, ConnectionAware<T> {
	private Logger logger = Logger.getLogger(getClass());

	private DataRouterContext drContext;
	private List<String> clientNames;
	private Isolation isolation;
	private boolean autoCommit;
	
	public BaseJdbcOp(DataRouterContext drContext, List<String> clientNames, Isolation isolation,
			boolean autoCommit) {
		this.drContext = drContext;
		this.clientNames = clientNames;
		this.isolation = isolation;
		this.autoCommit = autoCommit;
	}

	@Override
	public Connection getConnection(String clientName){
		Client client = getDataRouterContext().getClientPool().getClient(clientName);
		if(client==null){ return null; }
		if(client instanceof JdbcConnectionClient){
			JdbcConnectionClient jdbcConnectionClient = (JdbcConnectionClient)client;
			Connection connection = jdbcConnectionClient.getExistingConnection();
			if(connection == null){
				connection = jdbcConnectionClient.acquireConnection();
			}
			return connection;
		}
		return null;
	}

	
	
	
	
	public Logger getLogger(){
		return logger;
	}

	/****************** abstract methods default to no-op ******************/
	
	@Override
	public T runOnce(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public T runOncePerClient(Client client){
		// TODO Auto-generated method stub
		return null;
	}
	
	public T mergeResults(T fromOnce, Collection<T> fromEachClient){
		return fromOnce;
	}
	
	
	/**************** get *******************************************/
	
	@Override
	public DataRouterContext getDataRouterContext(){
		return drContext;
	}

	@Override
	public List<String> getClientNames(){
		return clientNames;
	}

	@Override
	public Isolation getIsolation(){
		return isolation;
	}

	@Override
	public boolean isAutoCommit(){
		return autoCommit;
	}

}
