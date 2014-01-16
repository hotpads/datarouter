package com.hotpads.datarouter.client.imp.jdbc.factory;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.type.JdbcClient;

public interface JdbcClientFactory extends ClientFactory{
	
	@Override
	public JdbcClient getClient();
}
