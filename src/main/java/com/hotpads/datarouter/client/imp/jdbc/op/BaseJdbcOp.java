package com.hotpads.datarouter.client.imp.jdbc.op;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.JdbcConnectionClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.TxnOp;
import com.hotpads.datarouter.op.aware.ConnectionAware;
import com.hotpads.datarouter.routing.DatarouterContext;

public abstract class BaseJdbcOp<T>
implements TxnOp<T>, ConnectionAware {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private DatarouterContext drContext;
	private List<String> clientNames;
	private Isolation isolation;
	private boolean autoCommit;
	
	public BaseJdbcOp(DatarouterContext drContext, List<String> clientNames, Isolation isolation,
			boolean autoCommit) {
		this.drContext = drContext;
		this.clientNames = clientNames;
		this.isolation = isolation;
		this.autoCommit = autoCommit;
	}
	
	public BaseJdbcOp(DatarouterContext drContext, List<String> clientNames) {
		this(drContext, clientNames, Isolation.DEFAULT, false);
	}

	@Override
	public Connection getConnection(String clientName){
		Client client = getDatarouterContext().getClientPool().getClient(clientName);
		if(client==null){
			return null;
		}
		if(client instanceof JdbcConnectionClient){
			JdbcConnectionClient jdbcConnectionClient = (JdbcConnectionClient)client;
			Connection connection = jdbcConnectionClient.getExistingConnection();
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
	public DatarouterContext getDatarouterContext(){
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
