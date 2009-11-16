package com.hotpads.datarouter.app.client.parallel.jdbc.base;

import java.sql.Connection;
import java.util.Map;

import com.hotpads.datarouter.app.JdbcTxnApp;
import com.hotpads.datarouter.app.client.parallel.base.BaseParallelTxnApp;
import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.type.JdbcConnectionClient;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.routing.DataRouter;

public abstract class BaseParallelJdbcTxnApp<T>
extends BaseParallelTxnApp<T> 
implements JdbcTxnApp<T> {

	public BaseParallelJdbcTxnApp(DataRouter router) {
		super(null);
		this.router = router;
	}
	
	public BaseParallelJdbcTxnApp(DataRouter router, Isolation isolation) {
		super(router, isolation);
	}

	@Override
	public Connection getConnection(String clientName){
		Client client = this.router.getClient(clientName);
		if(client==null){ return null; }
		if(client instanceof JdbcConnectionClient){
			JdbcConnectionClient jdbcConnectionClient = (JdbcConnectionClient)client;
			Connection connection = jdbcConnectionClient.getExistingConnection();
			return connection;
		}
		return null;
	}

}
