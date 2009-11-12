package com.hotpads.datarouter.client.type;

import com.hotpads.datarouter.client.Client;

public interface ConnectionClient 
extends Client{

	String reserveConnection(String tryConnectionName);
	void releaseConnection(String connectionName);
	
}
