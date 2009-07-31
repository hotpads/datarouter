package com.hotpads.datarouter.client.type;

import java.sql.SQLException;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.config.Isolation;

public interface TxnClient 
extends Client{

	String beginTxn(String connectionName, Isolation isolation) throws SQLException;
	void commitTxn(String connectionName) throws SQLException;
	void rollbackTxn(String connectionName) throws SQLException;
	
}
