package com.hotpads.datarouter.client.type;

import java.sql.Connection;
import java.sql.SQLException;

public interface JdbcConnectionClient
extends ConnectionClient{

	Connection getExistingConnection(String connectionName) throws SQLException;
	
}
