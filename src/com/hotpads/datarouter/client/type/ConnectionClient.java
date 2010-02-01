package com.hotpads.datarouter.client.type;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.connection.ConnectionHandle;

public interface ConnectionClient 
extends Client{
	
	ConnectionHandle getExistingHandle();
	ConnectionHandle reserveConnection();
	ConnectionHandle releaseConnection();
	
}
