package com.hotpads.datarouter.client.type;

import com.hotpads.datarouter.client.Client;

public interface SessionClient
extends Client{

	String openSession(String tryConnectionName);
	void closeSession(String connectionName);

}
