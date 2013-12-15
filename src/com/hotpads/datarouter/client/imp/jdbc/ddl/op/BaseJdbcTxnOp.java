package com.hotpads.datarouter.client.imp.jdbc.ddl.op;

import java.sql.Connection;
import java.util.List;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.JdbcConnectionClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.op.BaseDataRouterOp;
import com.hotpads.datarouter.op.aware.ConnectionAware;
import com.hotpads.datarouter.routing.DataRouterContext;

//TODO move to jdbc module
public abstract class BaseJdbcTxnOp<T>
extends BaseDataRouterOp<T> 
implements ConnectionAware<T> {
	
	public BaseJdbcTxnOp(DataRouterContext drContext, List<String> clientNames, Isolation isolation,
			boolean autoCommit) {
		super(drContext);
	}

	@Override
	public Connection getConnection(String clientName){
		Client client = getDataRouterContext().getClientPool().getClient(clientName);
		if(client==null){ return null; }
		if(client instanceof JdbcConnectionClient){
			JdbcConnectionClient jdbcConnectionClient = (JdbcConnectionClient)client;
			Connection connection = jdbcConnectionClient.getExistingConnection();
			return connection;
		}
		return null;
	}

}
