package com.hotpads.datarouter.client.type;

import java.sql.Connection;

public interface JdbcConnectionClient
extends ConnectionClient{

	Connection getExistingConnection(String connectionName);
	
}
