package com.hotpads.datarouter.app;

import com.hotpads.datarouter.client.Client;

public interface ClientOp<T>{

	Client getClient();

	void reserveConection();
	void releaseConnection();
	
}
