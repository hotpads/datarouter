package com.hotpads.datarouter.client.type;

import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.connection.ConnectionHandle;

public interface TxnClient 
extends ConnectionClient{

	ConnectionHandle beginTxn(Isolation isolation);
	ConnectionHandle commitTxn();
	ConnectionHandle rollbackTxn();
	
}
