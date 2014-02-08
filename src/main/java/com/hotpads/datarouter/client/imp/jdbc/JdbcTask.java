package com.hotpads.datarouter.client.imp.jdbc;

import org.hibernate.Session;

public interface JdbcTask {

	Object run(Session session);
	
}
