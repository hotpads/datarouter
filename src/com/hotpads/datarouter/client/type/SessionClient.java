package com.hotpads.datarouter.client.type;

import java.sql.SQLException;

import com.hotpads.datarouter.client.Client;

public interface SessionClient
extends Client{

	String openSession(String tryConnectionName) throws SQLException;
	void closeSession(String connectionName) throws SQLException;

}
