package com.hotpads.datarouter.app.client.parallel.jdbc.base;

import java.sql.Connection;
import java.util.List;

import com.hotpads.datarouter.app.ConnectionOp;
import com.hotpads.datarouter.app.client.parallel.base.BaseParallelTxnOp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.JdbcConnectionClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.routing.DataRouterContext;

public abstract class BaseParallelJdbcTxnOp<T>
extends BaseParallelTxnOp<T> 
implements ConnectionOp<T> {

	public BaseParallelJdbcTxnOp(DataRouterContext drContext, List<String> clientNames, Isolation isolation,
			boolean autoCommit) {
		super(drContext, clientNames, isolation, autoCommit);
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
