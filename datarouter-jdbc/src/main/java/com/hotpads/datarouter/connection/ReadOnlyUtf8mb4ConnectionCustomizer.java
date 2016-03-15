package com.hotpads.datarouter.connection;

import java.sql.Connection;

public class ReadOnlyUtf8mb4ConnectionCustomizer extends Utf8mb4ConnectionCustomizer{

	@Override
	public void onAcquire(Connection connection, String parentDataSourceIdentityToken) throws Exception{
		super.onAcquire(connection, parentDataSourceIdentityToken);
		connection.setReadOnly(true);
	}

}
