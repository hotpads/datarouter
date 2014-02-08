package com.hotpads.datarouter.client.imp.hibernate.factory;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.type.JdbcClient;

public interface HibernateClientFactory extends ClientFactory{
	
	@Override
	public JdbcClient getClient();
}
