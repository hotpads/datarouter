package com.hotpads.datarouter.app.client.parallel.jdbc.base;

import java.sql.Connection;
import java.sql.SQLException;
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
	
	public BaseParallelJdbcTxnApp(DataRouter router, Map<String,String> existingConnectionNameByClientName) {
		super(router, existingConnectionNameByClientName);
	}
	
	public BaseParallelJdbcTxnApp(DataRouter router, Map<String,String> existingConnectionNameByClientName, Isolation isolation) {
		super(router, existingConnectionNameByClientName, isolation);
	}

	@Override
	public Connection getConnection(String clientName){
		String connectionName = this.connectionNameByClientName.get(clientName);
		Client client = this.router.getClient(clientName);
		if(client==null){ return null; }
		if(client instanceof JdbcConnectionClient){
			JdbcConnectionClient jdbcConnectionClient = (JdbcConnectionClient)client;
			Connection connection = jdbcConnectionClient.getExistingConnection(connectionName);
			return connection;
		}
		return null;
	}

}
