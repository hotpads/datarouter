package com.hotpads.datarouter.client.type;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.config.Isolation;

public interface TxnClient 
extends Client{

	String beginTxn(String connectionName, Isolation isolation);
	void commitTxn(String connectionName);
	void rollbackTxn(String connectionName);
	
}
