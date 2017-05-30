package com.hotpads.datarouter.connection;

import java.sql.Connection;

import com.mchange.v2.c3p0.AbstractConnectionCustomizer;

public class Utf8mb4ConnectionCustomizer extends AbstractConnectionCustomizer{

	@Override
	public void onAcquire(Connection connection, String parentDataSourceIdentityToken) throws Exception{
		connection.createStatement().executeQuery("set character_set_client=" + JdbcConnectionPool.UTF8MB4_CHARSET);
	}

}
