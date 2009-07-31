package com.hotpads.datarouter.client.type;

import java.sql.SQLException;

import com.hotpads.datarouter.client.Client;

public interface ConnectionClient 
extends Client{

	String reserveConnection(String tryConnectionName) throws SQLException;
	void releaseConnection(String connectionName) throws SQLException;
	
}
