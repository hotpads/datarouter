package com.hotpads.datarouter.client.type;

import com.hotpads.datarouter.connection.ConnectionHandle;

public interface SessionClient
extends ConnectionClient{

	ConnectionHandle openSession();
	ConnectionHandle closeSession();

}
